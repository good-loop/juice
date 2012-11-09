package com.winterwell.juice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import winterwell.utils.time.Time;

/**
 * Class for extracting metadata from WordPress posts. It can extract post's tags,
 * text body of a post, title, link to a previous post (if exists), post creation
 * date/time and name of the author of the post. 
 * 
 * @author ivan
 *
 */
public class WordPressJuicer extends AJuicer {

	@Override
	List<Anno> juice(JuiceMe doc) {
		// Fail fast for non-WordPress
		String blog = new BlogSniffer().sniff(doc.html);
		if (!BlogSniffer.WORDPRESS.equals(blog))
			return Collections.EMPTY_LIST;

		extractTags(doc);
		extractRating(doc);
		extractPostBody(doc);
		extractMetadata(doc);
		extractTitle(doc);
		extractPrevPost(doc);

		return added(doc);
	}

	private void extractTags(JuiceMe doc) {
		List<Anno<String>> tagAnnotations = new ArrayList<Anno<String>>();

		Elements tagElements = doc.doc.getElementsByAttributeValueEnding("rel",
				"tag");

		for (Element tagElement : tagElements) {
			String tagName = tagElement.text();
			Anno<String> annotation = new Anno<String>(0, 0, AJuicer.TAGS,
					tagName);
			tagAnnotations.add(annotation);
			annotation.juicer = this;
		}

		doc.type2annotation.addAll(AJuicer.TAGS, tagAnnotations);

	}

	private void extractRating(JuiceMe doc) {
		// TODO: Rating is set not in the markup but through JavaScript code.
		// Value of rating is requested from PollDaddy service.
	}

	private void extractType(JuiceMe doc) {
		put(doc, AJuicer.MSG_TYPE, KMsgType.POST);
	}

	// Extract text body of a post
	private void extractPostBody(JuiceMe doc) {
		// Get element with article's text
		Elements elements = doc.doc.getElementsByClass("entry-content");

		Element rootDiv = elements.get(0);
		String text = rootDiv.text();
		text = cleanText(text);
		
		put(doc, AJuicer.POST_BODY, text);
	}

	String[] endings = new String[] {"About these ads", "Rate this"};
	
	// Remove text at the end of text div that is not related to the post
	private String cleanText(String text) {
		
		for (String ending : endings) {
			int endingPos = text.indexOf(ending);
			if (endingPos > 0) {
				return text.substring(0, endingPos);
			}
		}
		
		return text;		
	}
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
	SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
	
	/**
	 * Extracting metadata from the following markup:
	 * <div class="entry-meta">
	 *     <span class="meta-prep meta-prep-author">Posted on</span> 
	 *     <a href="http://www.soda.sh/static/blog/?p=36" 
	 *        title="9:55 am" 
	 *        rel="bookmark">
	 *         <span class="entry-date">August 5, 2011</span>
	 *     </a> 
	 *     <span class="meta-sep">by</span>
	 *     <span class="author vcard">
	 *         <a class="url fn n" 
	 *            href="http://www.soda.sh/static/blog/?author=2" 
	 *            title="View all posts by winterstein">winterstein
	 *         </a>
	 *     </span>
	 */
	private void extractMetadata(JuiceMe doc) {
		Element metadataElement = doc.doc.getElementsByClass("entry-meta").get(0);
		
		Calendar calendar = null;
		try {
			// Extract posting date
			Element dateElement = metadataElement.getElementsByClass("entry-date").get(0);
			String dateText = dateElement.text();
			Date date = dateFormat.parse(dateText);
			
			calendar = new GregorianCalendar(1900 + date.getYear(), date.getMonth(), date.getDate());
			
			// Extract posting time
			Element timeA = dateElement.parent();
			String timeText = timeA.attr("title");
			Date time = timeFormat.parse(timeText);
			
			calendar.set(1900 + date.getYear(), date.getMonth(), date.getDate(), time.getHours(), time.getMinutes());
			
		} catch (ParseException pe) {
			// We catched this exception if we failed to parse date of
			// if we failed to parse time. If we failed to parse date 'calendar'
			// object is null and will not be stored, if we failed to parse time
			// calendar object will contain correct date with time equals to 00:00
		}
		
		if (calendar != null) {
			// We cannot extracting this and zeroizing them makes testing easier
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			
			Time publicationTime = new Time(calendar);
			put(doc, AJuicer.PUB_TIME, publicationTime);
		}
		
		// Extract author's name
		Elements authorSpanElements = Selector.select("span.author.vcard", metadataElement);
		if (!authorSpanElements.isEmpty()) {
			Element authorSpan = authorSpanElements.get(0);
			String authorName = authorSpan.text();
			put(doc, AJuicer.AUTHOR_NAME, authorName);
		}		
		
	}
	
	/**
	 * 
	 * Extracting title from the following markup:
	 * <h1 class="entry-title">SoDash nominated for Best Advertising or Marketing Tech Startup Award</h1>
	 * 
	 */	
	private void extractTitle(JuiceMe doc) {
		Element entryTitleTag = doc.doc.getElementsByClass("entry-title").get(0);
		String title = entryTitleTag.text();
		
		put(doc, AJuicer.TITLE, removeNBSP(title));
					
	}
	
	private String removeNBSP(String str) {
		String cleaned = str.replace("\u00a0"," ");
		return cleaned;
	}
	
	/**
	 * Extracting URL to previous post from the following markup:
	 * <div id="nav-below" class="navigation">
	 *     <div class="nav-previous">
	 *     		<a href="http://www.soda.sh/static/blog/?p=33" rel="prev">
	 *     			<span class="meta-nav">←</span> 
	 *     			A strange bug in the Twitter search API: OR + location = fail
	 *     		</a>
	 *     </div>
	 *     <div class="nav-next">
	 *     		<a href="http://www.soda.sh/static/blog/?p=47" rel="next">
	 *     			More tweets than time? SoDash investment to develop smarter handling of social media 
	 *     			<span class="meta-nav">→</span>
	 *     		</a>
	 *     </div>
	 * </div><!-- #nav-below -->
	 */
	private void extractPrevPost(JuiceMe doc) {
		// Search for navigation bar
		Elements prevNavElements =doc.doc.getElementsByClass("nav-previous");
		if (!prevNavElements.isEmpty()) {
			Element divElement = prevNavElements.first();
			// Search for link on the previous post 
			Elements prevLinks = divElement.getElementsByAttributeValue("rel", "prev");
			if (!prevLinks.isEmpty()) {
				Element prevLink = prevLinks.first();
				String prevPostURL = prevLink.attr("href");
				
				put(doc, AJuicer.PREVIOUS, prevPostURL);
			}
		}
	}


}
