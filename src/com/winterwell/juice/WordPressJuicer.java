package com.winterwell.juice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import winterwell.utils.NotUniqueException;
import winterwell.utils.time.Time;
import winterwell.utils.web.WebUtils;
import winterwell.utils.web.WebUtils2;

/**
 * Class for extracting metadata from WordPress posts. It can extract post's tags,
 * text body of a post, title, link to a previous post (if exists), post creation
 * date/time and name of the author of the post. 
 * <p>
 * 
 * Dan: TODO what happens if we have the home page of a blog, which has several posts on it?
 * 
 * @author ivan
 *
 */
public class WordPressJuicer extends AJuicer {

	@Override
	void juice(JuiceMe item) {
		// Fail fast for non-WordPress
		String blog = new BlogSniffer().sniff(item.getHTML());
		if (!BlogSniffer.WORDPRESS.equals(blog)) {
			return;
		}
		
		// Dan: This looks inconsistent with what's done in MetaDataJuicer
		// Also, we should not assume one post per page. A blog page often has several posts on it.
		Item post = new Item(
//				KMsgType.POST, 
				item.getDoc());

		extractTags(post);
		extractRating(post);
		extractPostBody(post);
		extractMetadata(post);
		extractTitle(post);
		extractPrevPost(post);

		JuiceMe juiceMe = (JuiceMe) item;
		juiceMe.addItem(post);
		
		Elements commentElements = getCommentElements(item);
		
		extractComments(juiceMe, commentElements, null);
	}

	

	private void extractTags(Item post) {
		List<Anno<String>> tagAnnotations = new ArrayList<Anno<String>>();

		Elements tagElements = post.getDoc().getElementsByAttributeValueEnding("rel",
				"tag");
		List<String> tags = new ArrayList();
		
		for (Element tagElement : tagElements) {
			String tagName = tagElement.text();
//			Anno<String> annotation = new Anno<String>(AJuicer.TAGS,
//					tagName);
//			tagAnnotations.add(annotation);
			tags.add(tagName);
		}
				
		post.put(anno(AJuicer.TAGS, tags, null));

	}

	private void extractRating(Item post) {
		// TODO: Rating is set not in the markup but through JavaScript code.
		// Value of rating is requested from PollDaddy service.
	}

	private void extractType(Item post) {
		post.put(anno(AJuicer.MSG_TYPE, KMsgType.POST, post.doc));
	}

	// Extract text body of a post
	private void extractPostBody(Item post) {
		// Get element with article's text
		Elements elements = post.getDoc().getElementsByClass("entry-content");

		Element rootDiv = elements.get(0);
		String text = rootDiv.text();
		text = cleanText(text);
		
		post.put(anno(AJuicer.POST_BODY, text, rootDiv));
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
	private void extractMetadata(Item post) {
		Element metadataElement = post.getDoc().getElementsByClass("entry-meta").get(0);
		
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
			post.put(AJuicer.PUB_TIME, publicationTime);
		}
		
		// Extract author's name
		Elements authorSpanElements = Selector.select("span.author.vcard", metadataElement);
		if (!authorSpanElements.isEmpty()) {
			Element authorSpan = authorSpanElements.get(0);
			String authorName = authorSpan.text();
			post.put(anno(AJuicer.AUTHOR_NAME, authorName, authorSpan));
		}		
		
	}
	
	/**
	 * 
	 * Extracting title from the following markup:
	 * <h1 class="entry-title">SoDash nominated for Best Advertising or Marketing Tech Startup Award</h1>
	 * 
	 */	
	private void extractTitle(Item post) {
		// Dan: WordPress tags aren't enforced -- we have to handle the "no such tag" case
		// TODO use a more defensive approach throughout.
		Element entryTitleTag = one(post.getDoc().getElementsByClass("entry-title"));
		if (entryTitleTag==null) return;
		String title = entryTitleTag.text();
		
		post.put(anno(AJuicer.TITLE, removeNBSP(title), entryTitleTag));
					
	}
	


	/** 
	 * @param str
	 * @return
	 */
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
	private void extractPrevPost(Item post) {
		// Search for navigation bar
		Elements prevNavElements =post.getDoc().getElementsByClass("nav-previous");
		if (!prevNavElements.isEmpty()) {
			Element divElement = prevNavElements.first();
			// Search for link on the previous post 
			Elements prevLinks = divElement.getElementsByAttributeValue("rel", "prev");
			if (!prevLinks.isEmpty()) {
				Element prevLink = prevLinks.first();
				String prevPostURL = prevLink.attr("href");
				
				post.put(AJuicer.PREVIOUS, prevPostURL);
			}
		}
	}

	WordPressCommentsJuicer commentsJuicer = new WordPressCommentsJuicer();
	
	/**
	 * Dan: TODO please document
	 * 
	 * @param juiceMe
	 * @param commentElements
	 * @param prevURL
	 */
	private void extractComments(JuiceMe juiceMe, Elements commentElements, String prevURL) {
		if (commentElements == null) return; // Dan: avoid nesting when you can		
			
		for (Element commentElement : commentElements) {
			Item comment = new Item(
					//KMsgType.COMMENT, 
					commentElement);
			commentsJuicer.juice(comment);
			juiceMe.addItem(comment);							
			
			if (prevURL != null) {
				comment.put(AJuicer.PREVIOUS, prevURL);
			}
		
			if (hasReply(commentElement)) {
				String currentCommentURL = comment.getAnnotation(AJuicer.URL).value;
				
				Elements replyCommentElements = getReplyCommentElement(comment.getDoc());
				
				extractComments(juiceMe, replyCommentElements, currentCommentURL);			
			}
						
		}		
	}

	/**
	 * Extract list of parent elements of comments from the following markup:
	 * <ol class="commentlist">
	 * 		<li class="comment byuser comment-author-mpusz even thread-even depth-1 highlander-comment" 
	 *          id="li-comment-364">
	 *         ...
	 *      </li>
	 *      <li class="comment byuser comment-author-mpusz even depth-3 highlander-comment" 
	 *          id="li-comment-366">
	 *         ...
	 *      </li>
	 * </ol>
	 */
	private Elements getCommentElements(Item item) {
		Elements commentListParents = item.getDoc().getElementsByClass("commentlist");
				
		if (!commentListParents.isEmpty()) {
			Element commentList = commentListParents.first();
			return commentList.children();
		}
		
		return null;
	}
	
	
	/**
	 * Check if comment element has a reply by examining the following markup:
	 * <li class="comment byuser comment-author-mpusz even thread-even depth-1 highlander-comment" id="li-comment-364">
	 * 
	 * 		...
	 * 
	 * 		<ul class="children">
	 * 			<li class="comment byuser comment-author-akrzemi1 bypostauthor odd alt depth-2 highlander-comment" id="li-comment-365">
	 * 				...
	 * 			</li>
	 * 		</ul>
	 * </li>
	 * @param commentElement
	 * @return
	 */
	private boolean hasReply(Element commentElement) {
		Elements commentReplyElements = commentElement.getElementsByClass("children");
		
		return (! commentReplyElements.isEmpty());
	}
	
	private Elements getReplyCommentElement(Element commentElement) {
		Element childrenParent = commentElement.getElementsByClass("children").first();
		return childrenParent.children();
	}
}
