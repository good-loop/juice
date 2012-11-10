package com.winterwell.juice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import winterwell.utils.time.Time;


/**
 * Juicer that extract metadata from a comment for a WordPress post.
 * 
 * @author ivan
 *
 */
public class WordPressCommentsJuicer extends AJuicer {

	@Override
	void juice(Item comment) {
		extractText(comment);
		extractAuthorMetadata(comment);
		extractPostMetadata(comment);
	}

	
	private void extractText(Item comment) {
		Elements commentContentElements = comment.getDoc().getElementsByClass("comment-content");
		if (!commentContentElements.isEmpty()) {
			String commentText = commentContentElements.first().text();
			comment.put(AJuicer.POST_BODY, commentText);
		}
		
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
		Element nameElement = authorElement.getElementsByClass("fn").first();
		
		String authorName = nameElement.text();
		comment.put(AJuicer.AUTHOR_NAME, authorName);
		
		Element imageElement = authorElement.getElementsByTag("img").first();
		String avatarURL = imageElement.attr("src");
		comment.put(AJuicer.AUTHOR_IMG, avatarURL);
		
		
		
	}
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a");	
	
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
		Elements commentMetaElements = comment.getDoc().getElementsByClass("comment-meta");
		
		if (commentMetaElements.isEmpty()) {
			return;
		}
		
		Element commentMetaElement = commentMetaElements.first();
		
		Element commentURL = commentMetaElement.getElementsByTag("a").first();		
		String commentURLStr = commentURL.attr("href");
		comment.put(AJuicer.URL, commentURLStr);
		
		try {
		
			String timeText = commentURL.text();
			Date parsedDate = dateFormat.parse(timeText);
			Time time = new Time(parsedDate);
			comment.put(AJuicer.PUB_TIME, time);
			
		} catch (ParseException pe) {
			
		}
		
	}

}
