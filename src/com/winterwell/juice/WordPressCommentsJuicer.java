package com.winterwell.juice;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

import com.winterwell.utils.time.Time;

import winterwell.utils.reporting.Log;


/**
 * Juicer that extract metadata from a comment for a WordPress post.
 * 
 * @author ivan
 *
 */
public class WordPressCommentsJuicer extends AJuicer {

	private static final String LOGTAG = WordPressJuicer.LOGTAG;

	@Override
	public boolean juice(JuiceMe commentDoc) {
		Collection<Item> commentItems = commentDoc.getItemsOfType(KMsgType.COMMENT);
		
		for (Item commentItem : commentItems) {		
			extractText(commentItem);
			extractAuthorMetadata(commentItem);
			extractPostMetadata(commentItem, commentDoc);
		}
		
		return true;
	}

	
	private void extractText(Item comment) {		
		Element commentElement = getFirstElementByClass(comment.getDoc(), "comment-body",
				"comment-content");
		if (commentElement==null) {
			Log.w(LOGTAG, "No comment element for "+comment);
			return;
		}
		String commentText = commentElement.text();
		comment.put(anno(AJuicer.POST_BODY, commentText, commentElement));

		// 1st paragraph for e.g. summary views
		setPostBodyPartFromFirstParagraph(comment, commentElement);
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
		Element authorElement = getFirstElementByClass(comment.getDoc(), 
				"comment-author", "vcard");		
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
	 * TODO Handle threaded comments -- as seen here: 
	 * http://canadiansportsfan.wordpress.com/2012/07/29/olympic-broadcast-schedule-day-2-sunday-july-29/
	 * 
	 * Seen from 
	 * @param parentDoc 
	 */
	private void extractPostMetadata(Item comment, JuiceMe parentDoc) {
		Element commentMetaElement = getFirstElementByClass(comment.getDoc(), "comment-meta");		
		if (commentMetaElement== null) {
			// Make an XId from the id number
			Element container = getFirstElementByClass(comment.getDoc(), "comment");
			if (container==null) {
				return;
			}
			String id = container.attr("id");
			if (id==null) {
				return;				
			}
			Pattern idnum = Pattern.compile(".*comment[-_]?(\\d+)");
			Matcher m = idnum.matcher(id);
			if (m.matches()) {
				String num = m.group(1);
				String url = parentDoc.getURL();
				// TODO Is this ever different??
				String mu = parentDoc.getMainItem().get(AJuicer.URL);
				if (url==null) url = mu;
				if (url==null) return;
				if (mu !=null && ! url.equals(mu)) {
					Log.e(LOGTAG, "Doc url: "+url+" != main-item-url: "+mu);
				}
				String xid = url+"#comment-"+m.group(1);
				comment.put(anno(AJuicer.XID, xid, container));
			}
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
