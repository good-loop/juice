package com.winterwell.juice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
//	protected KMsgType type;
	
	// Just a Map is better here
	protected final Map<Key, Anno> type2annotation = new HashMap<Key, Anno>();

//	protected String url;

	protected Item() {}
	
//	Dan: type is an annotation too.
	public Item(Element doc) {
//		this.type = type;
		this.doc = doc;
	}
	
	
	Element doc;
	
//	Dan: type is an annotation too.
//	public KMsgType getType() {
//		return type;
//	}
	
	<X> void put(Anno<X> anno) {
//		if (value==null) return;
		
//		Anno<X> anno = new Anno<X>(type, value);
		type2annotation.put(anno.name, anno);
	}
	
	<X> void putIfAbsent(Anno<X> value) {
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

	public String getAuthor() {
		Anno a = type2annotation.get(AJuicer.AUTHOR_NAME);
		return a == null ? null : (String) a.value;
	}

	public Time getPublishedTime() {
		Anno a = type2annotation.get(AJuicer.PUB_TIME);
		return a == null ? null : (Time) a.value;
	}

	public String getText() {
		Anno a = type2annotation.get(AJuicer.POST_BODY);
		return a == null ? null : (String) a.value;
	}
	
	/**
	 * Get all extracted annotaions.
	 * @return list of all extracted annotation for this item. If no annotations
	 * were extracted returns empty list.
	 */
	public Collection<Anno> getAnnotations() {
//		List<Anno> resultAnnotations = new ArrayList<Anno>();
//		for (List<Anno> value : type2annotation.values()) {
//			resultAnnotations.addAll(value);
//		}
		
		return type2annotation.values(); // resultAnnotations;
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
//		List<Anno> list = type2annotation.get(key);
//		
//		if (list == null) {
//			list = new ArrayList<Anno>();
//		}
//		
//		return list;
	}
	
//	/**
//	 * Get a single (first) annotation of a specified type.
//	 * @param key
//	 * @return returns single (first) annotation of a specified type. If no
//	 * annotation of a specified type exists, returns null.
//	 */
//	public <X> Anno<X> getSingleAnnotation(Key<X> key) {
//		return type2annotation.getOne(key);
//	}

	// Use the url annotation
//	public String getURL() {
//		return url;
//	}
	
}
