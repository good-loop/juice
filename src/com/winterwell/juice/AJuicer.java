package com.winterwell.juice;

import java.util.ArrayList;
import java.util.List;

import winterwell.utils.Key;
import winterwell.utils.TodoException;
import winterwell.utils.time.Time;

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
	public static final Key<String> TITLE = new Key("title");
	public static final Key<String> AUTHOR_NAME = new Key("author.name");
	/**
	 * Avatar image for the author.
	 */
	public static final Key<String> AUTHOR_IMG = new Key("author.img");
	/**
	 * Author contact details. You might find... email, twitter, facebook, youtube, phone-number.
	 * Format: (canonical external ID)@(service)	<br>
	 * For blogs, service="blogs"
	 * For forums / message-boards, service="forums"
	 * For other web-pages, service="web"
	 * <p> 
	 * This ID must be unique across the web! 
	 * <p>
	 * On a blog, use one of:<br>
	 *  (a) email@domain-name@blogs	(preferred if known)<br>
	 *  (b) screen-name@domain-name@blogs	(if this is reliable, i.e. it can reliably be found for posts & comments & is unlikely to change)<br>
	 *  (c) url-to-profile-page@blogs	<br>
	 *  (d) name@domain-name@blogs
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
	 * The previous post in the thread, if there is one.
	 * E.g. in a comment thread, this might be the preceding comment (if the comment is a 
	 * reply to that one), or it might be the top-level post (if the comment is a response
	 * to the top level post -- this can be used as the default guess).
	 */
	public static final Key<String> PREVIOUS = new Key("prev");
	
	/**
	 * What type of post is this?	<br>
	 * Valid values are: post, comment, page, video, image, event, misc 
	 */
	public static final Key<String> MSG_TYPE = new Key("msgtype");	
	
	/**
	 * Does it have a star rating or other score?<br>
	 * If so, convert that into a [0, 1] score.
	 * E.g. "4 stars out of 5" would be 0.8
	 */
	public static final Key<Double> RATING = new Key("rating");
	
	/**
	 * Is the post labelled with tags or a category?<br> 
	 */
	public static final Key<String> TAGS = new Key("tags");
	
	/**
	 * The "best" image to show as an illustration for this post.
	 */
	public static final Key<String> IMAGE_URL = new Key("img");
	/**
	 * A short summary description of the post.
	 */
	public static final Key<String> DESC = new Key("desc");	
	
	/**
	 * @param url
	 * @param html
	 * @param pages The extractions made by this juicer.
	 */
	abstract List<Anno> juice(JuiceMe doc);

	protected <X> void put(JuiceMe doc, int start, int end, Key<X> type, X value) {
		assert type != null;
		if (value==null) return;
		Anno<X> anno = new Anno<X>(start, end, type, value);
		anno.juicer = this;
		doc.type2annotation.putOne(type, anno);
	}
	
	protected <X> void putIfAbsent(JuiceMe doc, Key<X> type, X value) {
		if (value==null) return;
		if (doc.type2annotation.containsKey(type)) return;
		put(doc, type, value);
	}
	
	/**
	 * Which annotations (if any) has this class added?
	 * @param doc
	 * @return never null, can be empty
	 */
	protected List<Anno> added(JuiceMe doc) {
		List<Anno> added = new ArrayList(4);
		for(Anno a : doc.type2annotation.getValueSet()) {
			if (a.juicer==this) added.add(a);
		}
		return added;
	}

	
	/**
	 * A document-level annotation (gets given position start=end=0)
	 * @param doc
	 * @param type
	 * @param value
	 */
	protected <X> void put(JuiceMe doc, Key<X> type, X value) {
		put(doc, 0,0,type,value);
	}

}
