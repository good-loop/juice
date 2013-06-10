package com.winterwell.juice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;

import winterwell.utils.Key;
import winterwell.utils.time.Time;

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

	protected final Map<Key, Anno> type2annotation = new HashMap<Key, Anno>();

	protected Item() {}
	
	public Item(Element doc) {
		this.doc = doc;
	}
	
	
	Element doc;
		
	public <X> void put(Anno<X> anno) {
		type2annotation.put(anno.name, anno);
	}
	
	public <X> void putIfAbsent(Anno<X> value) {
		if (value==null) return;
		if (type2annotation.containsKey(value.name)) return;
		
		put(value);
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

	public String getHTML() {
		return doc.html();
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
	
}
