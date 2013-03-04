package com.winterwell.juice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import winterwell.utils.NotUniqueException;
import winterwell.utils.reporting.Log;
import winterwell.utils.time.Time;

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

	private static final String LOGTAG = "WordPressJuicer";

	@Override
	boolean juice(JuiceMe document) {
		// Fail fast for non-WordPress
		String blog = new BlogSniffer().sniff(document.getHTML());
		if ( ! BlogSniffer.WORDPRESS.equals(blog)) {
			return false;
		}
		
		Elements postElements = document.getDoc().getElementsByClass("post");
		
		for (Element postElement : postElements) {
			Item postItem = new Item(postElement);
			postItem.put(anno(AJuicer.MSG_TYPE, KMsgType.POST, postElement));
			
			extractTags(postItem);
			extractRating(postItem);
			extractPostBody(postItem);
			extractMetadata(postItem);
			extractTitle(postItem);
			
			document.addItem(postItem);
		}
		
		Elements commentElements = getCommentElements(document.getDoc());
		
		if (commentElements != null) {
			Map<Item, Item> prevMap = new HashMap<Item, Item>();
			
			extractComments(document, commentElements, null, prevMap);
			
			WordPressCommentsJuicer commentsJuicer = new WordPressCommentsJuicer();
			commentsJuicer.juice(document);
			savePrevRelations(prevMap);
		}
		return true;
	}
	
	private void extractTags(Item post) {
		Elements tagElements = post.getDoc().getElementsByAttributeValueEnding("rel",
				"tag");
		List<String> tags = new ArrayList();
		
		for (Element tagElement : tagElements) {
			String tagName = tagElement.text();
			tags.add(tagName);
		}
				
		post.put(anno(AJuicer.TAGS, tags, null));

	}

	private void extractRating(Item post) {
		// TODO: Rating is set not in the markup but through JavaScript code.
		// Value of rating is requested from PollDaddy service.
	}

	/** Extract text body of a post */
	private void extractPostBody(Item post) {
		// Get element with article's text
		Element rootDiv = getFirstElementByClass(post.getDoc(), "entry-content", "post-content", "entry");
		if (rootDiv==null) {
			Log.e(LOGTAG, "No post body? "+post.getHTML());
			return;
		}
		
		String text = rootDiv.text();
		text = cleanText(text);
		
		post.put(anno(AJuicer.POST_BODY, text, rootDiv));
		
		Element firstParagraphElement = Utils.getFirstParagraphElement(rootDiv);
		String firstParagraph = Utils.extractFirstParagraph(firstParagraphElement);
		post.put(anno(AJuicer.POST_BODY_PART, firstParagraph, firstParagraphElement));
	}

	String[] endings = new String[] {"About these ads", "Rate this"};
	
	/** Remove text at the end of text div that is not related to the post */
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
		Element metadataElement = getFirstElementByClass(post.getDoc(), "entry-meta", "post-meta");		
		if (metadataElement==null) {
			Log.d(LOGTAG, "No entry-meta elements");
			return;
		}		
		Element dateElement = getFirstElementByClass(metadataElement, "entry-date", "post-date");
		if (dateElement!=null) {
			try {
				// Extract posting date				
				String dateText = dateElement.text();
				Date date = dateFormat.parse(dateText);						
				GregorianCalendar calendar = new GregorianCalendar(1900 + date.getYear(), date.getMonth(), date.getDate());
				
				// Extract posting time
				try {
					Element timeA = dateElement.parent();
					String timeText = timeA.attr("title");				
					Date time = timeFormat.parse(timeText);
					calendar.set(1900 + date.getYear(), date.getMonth(), date.getDate(), time.getHours(), time.getMinutes());
				} catch(ParseException ex) {
					// oh well -- we have the date
				}

				// We cannot extracting this and zeroizing them makes testing easier
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				
				Time publicationTime = new Time(calendar);
				post.put(anno(AJuicer.PUB_TIME, publicationTime, dateElement));
	
			} catch (ParseException pe) {
				// We caught this exception if we failed to parse date or
				// if we failed to parse time. If we failed to parse date 'calendar'
				// object is null and will not be stored, if we failed to parse time
				// calendar object will contain correct date with time equals to 00:00
			}			
		}
		
		// Extract author's name
		Elements authorSpanElements = Selector.select("span.author.vcard", metadataElement);
		if ( ! authorSpanElements.isEmpty()) {
			Element authorSpan = authorSpanElements.get(0);
			String authorName = authorSpan.text();
			post.put(anno(AJuicer.AUTHOR_NAME, authorName, authorSpan));
			// TODO XId?? img??
			return;
		}
		
		Elements authorLink = post.getDoc().getElementsByAttributeValue("rel", "author");
		Element author = one(authorLink, false); // If a post is by 2 authors -- valid but rare -- this will only get one!!
		if (author!=null) {				
			post.put(anno(AJuicer.AUTHOR_NAME, author.text(), author));
			if ("a".equals(author.tagName())) {
				String url = author.attr("href");
				if ( ! url.isEmpty()) {
					post.put(anno(AJuicer.AUTHOR_XID, url, author));
					post.put(anno(AJuicer.AUTHOR_URL, url, author));
				}
			}
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
		Element entryTitleTag = getFirstElementByClass(post.getDoc(), "entry-title","post-title");
		if (entryTitleTag==null) return;
		String title = entryTitleTag.text();
		
		post.put(anno(AJuicer.TITLE, removeNBSP(title), entryTitleTag));
					
	}
	
	/** 
	 * Remove no-break space from the string
	 * @param str - string to clean 
	 * @return original string without no-break spaces
	 */
	private String removeNBSP(String str) {
		String cleaned = str.replace("\u00a0"," ");
		return cleaned;
	}
	
	
	/**
	 * Extracting comments from a post. Real extracting of metadata is done in
	 * WordPressCommentsJuicer, this method, just finds comments on page and
	 * stores them to document.
	 * 
	 * @param document
	 * @param commentElements - elements of "root" comments that are not replies
	 * to any other comments
	 * @param prevItem - item with the previously extracted comment
	 * @param prevMap - store previous relations between comments. Key is a reply,
	 * value is a previous comment.
	 */
	private void extractComments(JuiceMe document, Elements commentElements, Item prevItem, Map<Item, Item> prevMap) {		
			
		for (Element commentElement : commentElements) {
			
			Item comment = new Item(commentElement);
			comment.put(anno(AJuicer.MSG_TYPE, KMsgType.COMMENT, commentElement));
			document.addItem(comment);							
			
			if (prevItem != null) {
				prevMap.put(comment, prevItem);				
			}
		
			// Adding replies
			if (hasReply(commentElement)) {
				
				Elements replyCommentElements = getReplyCommentElement(comment.getDoc());
				// Add replies for this comment
				if (replyCommentElements != null) {
					extractComments(document, replyCommentElements, comment, prevMap);
				}
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
	private Elements getCommentElements(Element element) {
		Elements commentListParents = element.getElementsByClass("commentlist");
				
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
	
	private void savePrevRelations(Map<Item, Item> prevMap) {
		for (Item comment : prevMap.keySet()) {
			Item prevComment = prevMap.get(comment);
			
			String prevCommentURL = prevComment.getAnnotation(AJuicer.URL).value;
			comment.put(anno(AJuicer.PREVIOUS, prevCommentURL, prevComment.doc.parent()));
		}
		
	}
}
