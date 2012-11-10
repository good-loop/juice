package com.winterwell.juice;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import winterwell.utils.Key;
import winterwell.utils.containers.ListMap;
import winterwell.utils.time.Time;

/**
 * Item extracted from a web-page.
 * 
 * @author ivan
 * 
 */
public class Item {
	protected KMsgType type;
	protected ListMap<Key, Anno> type2annotation = new ListMap<Key, Anno>();
	protected String url;

	protected Item() {}
	
	public Item(KMsgType type, Element doc) {
		this.type = type;
		this.doc = doc;
	}
	
	
	Element doc;
	
	public KMsgType getType() {
		return type;
	}
	
	<X> void put(Key<X> type, X value) {
		assert type != null;
		if (value==null) return;
		
		Anno<X> anno = new Anno<X>(type, value);
		type2annotation.putOne(type, anno);
	}
	
	<X> void putIfAbsent(Key<X> type, X value) {
		if (value==null) return;
		if (type2annotation.containsKey(type)) return;
		
		put(type, value);
	}
	
	
	Element getDoc() {
		return doc;
	}

	// Convenience methods. Questionable.

	public String getTitle() {
		Anno a = type2annotation.getOne(AJuicer.TITLE);
		return a == null ? null : (String) a.value;
	}

	public String getAuthor() {
		Anno a = type2annotation.getOne(AJuicer.AUTHOR_NAME);
		return a == null ? null : (String) a.value;
	}

	public Time getPublishedTime() {
		Anno a = type2annotation.getOne(AJuicer.PUB_TIME);
		return a == null ? null : (Time) a.value;
	}

	public String getText() {
		Anno a = type2annotation.getOne(AJuicer.POST_BODY);
		return a == null ? null : (String) a.value;
	}
	
	/**
	 * Get all extracted annotaions.
	 * @return list of all extracted annotation for this item. If no annotations
	 * were extracted returns empty list.
	 */
	public List<Anno> getAnnotations() {
		List<Anno> resultAnnotations = new ArrayList<Anno>();
		for (List<Anno> value : type2annotation.values()) {
			resultAnnotations.addAll(value);
		}
		
		return resultAnnotations;
	}

	public String getHTML() {
		return doc.html();
	}

	/**
	 * Return list of annotaion with a specified key
	 * @param key
	 * @return list of annotations with a specified key (if key was found) or,
	 * empty list otherwise
	 */
	public <X> List<Anno>  getAnnotations(Key<X> key) {
		List<Anno> list = type2annotation.get(key);
		
		if (list == null) {
			list = new ArrayList<Anno>();
		}
		
		return list;
	}
	
	/**
	 * Get a single (first) annotation of a specified type.
	 * @param key
	 * @return returns single (first) annotation of a specified type. If no
	 * annotation of a specified type exists, returns null.
	 */
	public <X> Anno<X> getSingleAnnotation(Key<X> key) {
		return type2annotation.getOne(key);
	}

	public String getURL() {
		return url;
	}
	
}
