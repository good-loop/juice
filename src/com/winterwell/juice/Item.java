package com.winterwell.juice;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;

import com.winterwell.utils.containers.SetMap;
import com.winterwell.utils.time.Time;
import com.winterwell.web.WebEx;

import com.winterwell.utils.web.WebUtils2;
import com.winterwell.utils.Key;
import creole.data.XId;

/**
 * Item extracted from a web-page.
 * 
 * The data is all stored in a map of {@link Anno}s. This class provides convenience
 * methods for getting & setting common properties.
 * 
 * @author ivan
 * 
 */
public class Item {

	/**
	 * true => the url can (kind-of) be trusted to mark the resource.
	 * false => the url cannot be used to identify the resource.
	 * E.g. there are many items on the page, or it is a front-page which
	 * is likely to change. 
	 * null => nothing
	 */
	public Boolean stable1ItemUrl;
	
	protected final Map<Key, Anno> type2annotation = new HashMap<Key, Anno>();
	/**
	 * What is the url that this Item was fetched from?
	 * This may not be permanent -- e.g. homepage contents change, as do "the comments on page 2"
	 * It should never be null.
	 */
	private String urlNow;

	private Item() {}

	/**
	 * @param urlNow What is the url that this Item was fetched from?
	 * This may not be permanent -- e.g. homepage contents change, as do "the comments on page 2"
	 * It should never be null.
	 */
	public Item(Element doc, String urlNow) {
		this.doc = doc;
		this.urlNow = urlNow;
	}
	
	/**
	 * For logging errors
	 * @param ex
	 * @param urlNow
	 */
	public Item(Exception ex, Element doc, String urlNow) {
		this(doc, urlNow);
		setType(KMsgType.ERROR);
		put(new Anno(AJuicer.TITLE, ex.getClass().getName(), null));
		put(new Anno(AJuicer.POST_BODY, ex.getMessage(), null));
	}

	/**	 
	 * What is the url that this Item was fetched from?
	 * This may not be permanent -- e.g. homepage contents change, as do "the comments on page 2"
	 * It should never be null.
	 * <p>
	 * See <code>get(AJuicer.URL)</code> (which is used, if set).
	 */
	public String getUrl() {
		Anno<String> a = type2annotation.get(AJuicer.URL);
		if (a==null) {
			return urlNow;
		}
		return (String) a.value;
	}
	
	Element doc;
	
	/**
	 * true => This item is created from a search or index listing -- not from the actual item.
	 */
	boolean stub;
	
	public boolean isStub() {
		return stub;
	}
	
	public void setStub(boolean stub) {
		this.stub = stub;
	}
		
	public <X> void put(Anno<X> anno) {
		type2annotation.put(anno.name, anno);		
	}
	
	public <X> boolean putIfAbsent(Anno<X> value) {
		if (value==null) return false;
		if (type2annotation.containsKey(value.name)) return false;
		
		put(value);
		return true;
	}
	
	
	Element getDoc() {
		return doc;
	}

	// Convenience methods. Questionable.

	public String getTitle() {
		Anno a = type2annotation.get(AJuicer.TITLE);
		return a == null ? null : (String) a.value;
	}
	
	@Override
	public String toString() {
		return "Item["+type2annotation+"]";
	}

	public String getAuthor() {
		Anno a = type2annotation.get(AJuicer.AUTHOR_NAME);
		if (a!=null) return (String) a.value;
		a = type2annotation.get(AJuicer.AUTHOR_XID);
		if (a!=null) return a.value.toString();
		return null;
	}

	public Time getPublishedTime() {
		Anno a = type2annotation.get(AJuicer.PUB_TIME);
		return a == null ? null : (Time) a.value;
	}

	public String getText() {
		Anno a = type2annotation.get(AJuicer.POST_BODY);
		return a == null ? null : (String) a.value;
	}
	
	public KMsgType getType() {
		Anno a = type2annotation.get(AJuicer.MSG_TYPE);
		return a == null ? null : (KMsgType) a.value;
	}
	
	public void setType(KMsgType type) {
		put(new Anno(AJuicer.MSG_TYPE, type, null));
	}
	
	/**
	 * Get all extracted annotaions.
	 * @return collection of all extracted annotation for this item. If no annotations
	 * were extracted returns empty collection.
	 */
	public Collection<Anno> getAnnotations() {		
		return type2annotation.values();
	}

	/**
	 * @return Can be null (though rare) if doc is null.
	 */
	public String getHTML() {
		return doc==null? null : doc.html();
	}

	/**
	 * Return list of annotaion with a specified key
	 * @param key
	 * @return annotation with a specified key (if key was found) or,
	 * null otherwise
	 */
	public <X> Anno<X> getAnnotation(Key<X> key) {
		return type2annotation.get(key);
	}
	

	/**
	 * @param key
	 * @return annotation value, if one is set
	 */
	public <X> X get(Key<X> key) {
		Anno<X> a = type2annotation.get(key);
		return a==null? null : a.value;
	}

	/**
	 * The XId annotation
	 * @return Can be null!
	 */
	public String getXId() {
		Anno a = type2annotation.get(AJuicer.XID);
		return a == null ? null : (String) a.value;
	}

	/**
	 * Convenience for adding a tag to the {@link AJuicer#TAGS} list.
	 * @param tag
	 */
	public void addTag(String tag) {
		Anno<List<String>> tags = type2annotation.get(AJuicer.TAGS);
		if (tags==null) {
			tags = new Anno<List<String>>(AJuicer.TAGS, new ArrayList(), null);
			put(tags);
		}
		tags.value.add(tag);
	}

	/**
	 * @return {@link #getXId()}@web BUT falls back to using the url!
	 */
	public XId getXId2() {
		String xid = getXId();
		if (xid==null) {
			String url = getUrl();
			// resolve relative
			URI _uri2 = WebUtils2.resolveUri(urlNow, url);
			xid = _uri2.toString();
		}
		return new XId(xid, "web");
	}

	/**
	 * Convenience for {@link #putIfAbsent(Anno)} with element=null
	 * @param key
	 * @param value
	 * @return true if put
	 */
	public <X> boolean putIfAbsent(Key<X> key, X value) {
		return putIfAbsent(new Anno(key, value, null));		
	}

	/**
	 * Add all of item's properties to this -- as un-anchored annotations!
	 * @param item
	 */
	public void extend(Item item) {
		boolean anchor = item.doc.equals(doc);
		Collection<Anno> annos = item.getAnnotations();
		for (Anno anno : annos) {
			if (anchor) {
				putIfAbsent(anno);
			} else {
				putIfAbsent(anno.name, anno.value);
			}
		}
		// doc (usually already set)
		if (doc==null) doc = item.doc;
		// non-stub fills out stub
		this.stub = this.stub && item.stub;
	}

	/**
	 * @deprecated Use with care!
	 * 
	 * What is the url that this Item was fetched from?
	 * This may not be permanent -- e.g. homepage contents change, as do "the comments on page 2"
	 * It should never be null.
	 */
	public String getUrlNow() {
		return urlNow;
	}
	
}
