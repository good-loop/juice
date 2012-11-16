package com.winterwell.juice;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import winterwell.utils.Key;
import winterwell.utils.NotUniqueException;
import winterwell.utils.TodoException;
import winterwell.utils.time.Time;

/**
 * Base for building juicers. Also defines the standard annotations. 
 * @author Daniel, Ivan
 *
 */
public abstract class AJuicer {

	/**
	 * Marks the borders of a post.
	 * If a page contains several posts, then they should create multiple POST annotations.
	 */
	public static final Key<String> POST = new Key("post");
	/**
	 * The text of an article.
	 */
	public static final Key<String> POST_BODY = new Key("postBody");
	
	/**
	 * Part of the text of an article. Use case: If we're juicing the home-page of a
	 * blog, it will often have several article snippets, giving the first 1 or two paragraphs
	 * but not the whole body-text. 
	 */
	public static final Key<String> POST_BODY_PART = new Key("postBodyPart");
	
	public static final Key<String> TITLE = new Key("title");
	public static final Key<String> AUTHOR_NAME = new Key("author.name");
	/**
	 * Avatar image for the author.
	 */
	public static final Key<String> AUTHOR_IMG = new Key("author.img");
	
	/**
	 * Author id. You might find... email, twitter, facebook, youtube, phone-number.
	 * <p> 
	 * This ID must be stable (the same author will always get the same id) and unique across the web! 
	 * <p>
	 * On a blog, use one of:<br>
	 *  (a) email@domain-name	(preferred if known)<br>
	 *  (b) screen-name@domain-name	(if this is reliable, i.e. it can reliably be found for posts & comments & is unlikely to change)<br>
	 *  (c) url-to-profile-page	<br>
	 *  (d) name@domain-name
	 *  
	 * @see XId in Creole
	 */
	public static final Key<String> AUTHOR_XID = new Key("oxid");
	
	/**
	 * Facebook ID, if this page has one.
	 */
	public static final Key<Time> FACEBOOK_ID = new Key("fbid");
	
	/**
	 * Published time.
	 */
	public static final Key<Time> PUB_TIME = new Key("time");
	/**
	 * The canonical url for this post (which may be different from the url used to read this page).
	 */
	public static final Key<String> URL = new Key("url");
	
	/**
	 * The XId for the previous post in the thread, if there is one.
	 * E.g. in a comment thread, this might be the preceding comment (if the comment is a 
	 * reply to that one), or it might be the top-level post (if the comment is a response
	 * to the top level post -- this can be used as the default guess).
	 */
	public static final Key<String> PREVIOUS = new Key("prev");
	
	/**
	 * What type of post is this?	<br>
	 * Valid values are: post, comment, page, video, image, event, misc 
	 */
	public static final Key<KMsgType> MSG_TYPE = new Key("msgtype");	
	
	/**
	 * Does it have a star rating or other score?<br>
	 * If so, convert that into a [0, 1] score.
	 * E.g. "4 stars out of 5" would be 0.8
	 */
	public static final Key<Double> RATING = new Key("rating");
	
	/**
	 * Is the post labelled with tags or a category?<br> 
	 */
	public static final Key<List<String>> TAGS = new Key("tags");
	
	/**
	 * The "best" image to show as an illustration for this post.
	 */
	public static final Key<String> IMAGE_URL = new Key("img");
	/**
	 * A short summary description of the post.
	 */
	public static final Key<String> DESC = new Key("desc");	
	
	/**
	 * Unique & stable id for the Item. Every Item should have one!
	 * <p>
	 * The url is often a good choice for the canonical external ID -- especially the canonical url. 
	 * <br>
	 * If there are multiple items on a page, then only the main item (if there is one) can use the url.
	 * The other items (e.g. comments) may have to use a generated XId<br>
	 * Similarly, if the url is not a stable identifier (e.g. it's for the frontpage of the site), then use
	 * a generated XId.
	 * <p>
	 * Generated XIds must follow a predictable pattern, such that the same item will always
	 * generate the same XId. I suggest using:<br>
	 *  domain + published-time + md5hash-of-text.
	 *   
	 * @see XId in Creole
	 */
	public static final Key<String> XID = new Key("xid");
	
	/**
	 * @param url
	 * @param html
	 * @param pages The extractions made by this juicer.
	 */
	abstract void juice(JuiceMe doc);

	/**
	 * Convenience method for new Anno (this will infer the generic parameter for you).
	 * @param key
	 * @param value
	 * @param element Can be null. See {@link Anno#Anno(Key, Object, Element)}
	 * @return new Anno(key, value, element)
	 */
	protected <V> Anno<V> anno(Key<V> key, V value, Element element) {
		return new Anno<V>(key, value, element);
	}
	

	/**
	 * @param elements
	 * @return the first element, if there is one, or null if not.
	 * @throws NotUniqueException
	 */
	protected Element one(Elements elements) {
		return one(elements, true);
	}
	
	/**
	 * @param elements
	 * @param strict If true, 2 or more elements will create an exception.
	 * If false, just returns the first element!
	 * @return the first element, if there is one, or null if not.
	 * @throws NotUniqueException
	 */
	protected Element one(Elements elements, boolean strict) {
		if (elements==null || elements.isEmpty()) return null;
		if (strict && elements.size() > 1) throw new NotUniqueException(elements.outerHtml());
		return elements.get(0);
	}


}
