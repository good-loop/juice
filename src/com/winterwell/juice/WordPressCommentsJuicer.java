package com.winterwell.juice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import winterwell.utils.reporting.Log;
import winterwell.utils.time.Time;
import winterwell.utils.web.WebUtils;
import winterwell.utils.web.WebUtils2;


/**
 * Juicer that extract metadata from a comment for a WordPress post.
 * 
 * @author ivan
 *
 */
public class WordPressCommentsJuicer extends AJuicer {

	private static final String LOGTAG = WordPressJuicer.LOGTAG;

	@Override
	boolean juice(JuiceMe commentDoc) {
		Collection<Item> commentItems = commentDoc.getItemsOfType(KMsgType.COMMENT);
		
		for (Item commentItem : commentItems) {		
			extractText(commentItem);
			extractAuthorMetadata(commentItem);
			extractPostMetadata(commentItem);
		}
		
		return true;
	}

	
	private void extractText(Item comment) {
		Elements commentContentElements = comment.getDoc().getElementsByClass("comment-body");
		
		if (commentContentElements.isEmpty()) {
			commentContentElements = comment.getDoc().getElementsByClass("comment-content");
		}
		
		Element commentElement = commentContentElements.first();
		if (commentElement==null) {
			Log.w(LOGTAG, "No comment element for "+comment);
			return;
		}
		String commentText = commentElement.text();
		comment.put(anno(AJuicer.POST_BODY, commentText, commentElement));
		
		Element firstParagraphElement = Utils.getFirstParagraphElement(commentElement);
		String firstParagraph = Utils.extractFirstParagraph(firstParagraphElement);
		comment.put(anno(AJuicer.POST_BODY_PART, firstParagraph, firstParagraphElement));
		
	}
	
	/**
	 * Extract name of the author and URL of the avatar from the following markup:
	 * <div class="comment-author vcard">
	 * 		<img id="grav-101920090538cd5c1e7fa7516ff2da5a-0" 
	 *           alt="" 
	 *           src="sampleWordPressPage_files/101920090538cd5c1e7fa7516ff2da5a.png" 
	 *           class="avatar avatar-68 grav-hashed grav-hijack" 
	 *           height="68" 
	 *           width="68">
	 *      <span class="fn">Omri</span> on 
	 *      <a href="http://greenido.wordpress.com/2012/11/07/gdl-il-on-chrome-extensions-and-backbone-js-in-the-real-world/#comment-2840">
	 * 		   <time pubdate="" 
	 *               datetime="2012-11-07T15:11:32+00:00">
	 *            November 7, 2012 at 3:11 pm
	 *         </time>
	 *      </a> 
	 *      <span class="says">said:</span>
	 * </div><!-- .comment-author .vcard -->
	 * 
	 * @param comment
	 */
	private void extractAuthorMetadata(Item comment) {
		Elements commentAuthorElements = comment.getDoc().getElementsByClass("comment-author");		
		if (commentAuthorElements.isEmpty()) {
			return;
		}		
		Element authorElement = commentAuthorElements.first();
		if (authorElement==null) {
			return;
		}
		Element nameElement = authorElement.getElementsByClass("fn").first();
		if (nameElement != null) {
			String authorName = nameElement.text();
			comment.put(anno(AJuicer.AUTHOR_NAME, authorName, authorElement));
		}		
		Element imageElement = authorElement.getElementsByTag("img").first();
		if (imageElement!=null) {
			String avatarURL = imageElement.attr("src");
			comment.put(anno(AJuicer.AUTHOR_IMG, avatarURL, imageElement));
		}				
	}
	
	static SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a");	
	
	/**
	 * Extracting comment's URL and comment's publication time from the following markup:
	 * <a href="http://greenido.wordpress.com/2012/11/07/gdl-il-on-chrome-extensions-and-backbone-js-in-the-real-world/#comment-2840">
	 * 		<time pubdate="" datetime="2012-11-07T15:11:32+00:00">
	 * 			November 7, 2012 at 3:11 pm
	 * 		</time>
	 * </a>
	 * 
	 * or from the following markup:
	 * <a href="http://akrzemi1.wordpress.com/2012/05/27/constant-initialization/#comment-364">
	 * 		June 12, 2012 at 11:36 am
	 * </a>	
	 * 
	 */
	private void extractPostMetadata(Item comment) {
		Element commentMetaElement = getFirstElementByClass(comment.getDoc(), "comment-meta");		
		if (commentMetaElement== null) {
			return;
		}
		
		Element commentURL = one(commentMetaElement.getElementsByTag("a"), false);
		if (commentURL==null) {
			Log.e(LOGTAG, "No comment url in "+commentMetaElement.html());
			return;
		}
		String commentURLStr = commentURL.attr("href");
		comment.put(anno(AJuicer.URL, commentURLStr, commentURL));
	
		try {		
			String timeText = commentURL.text();
			Date parsedDate = dateFormat.parse(timeText);
			Time time = new Time(parsedDate);
			comment.put(anno(AJuicer.PUB_TIME, time, commentURL));			
		} catch (Exception pe) {
			// We failed to parse date, so simply ignore it
			Log.e(LOGTAG, pe);
		}			
	}

}
