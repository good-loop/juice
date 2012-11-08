package com.winterwell.juice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import winterwell.utils.Key;
import winterwell.utils.containers.ListMap;
import winterwell.utils.time.Time;

/**
 * Internal container for document, plus extractions. 
 *
 */
public class JuiceMe {

	Document doc;
	
	public JuiceMe(String url, String html) {
		assert html != null : url;
		this.url = url;
		this.html = html;
		doc = Jsoup.parse(html, url);
	}
	
	// Protected constructor for Unit-testing where web-pages are read from
	// a file, not from the Internet
	JuiceMe(String html) {
		this.html = html;
		doc = Jsoup.parse(html);
	}
	
	String url;
	String html;
		
	/**
	 * Each type can have several annotations -- e.g. a web-page
	 * might contain a few articles.
	 */
	final ListMap<Key,Anno> type2annotation = new ListMap();

	// Convenience methods. Questionable.
	
	public String getTitle() {
		Anno a = type2annotation.getOne(AJuicer.TITLE);		
		return a==null? null : (String) a.value;		
	}
	
	public String getAuthor() {
		Anno a = type2annotation.getOne(AJuicer.AUTHOR_NAME);
		return a==null? null : (String) a.value;
	}

	public Time getPublishedTime() {
		Anno a = type2annotation.getOne(AJuicer.PUB_TIME);
		return a==null? null : (Time) a.value;
	}
	
	public String getText() {
		Anno a = type2annotation.getOne(AJuicer.POST_BODY);
		return a==null? null : (String) a.value;
	}
	
}
