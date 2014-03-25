package com.winterwell.juice;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import winterwell.utils.Key;
import winterwell.utils.StrUtils;
import winterwell.utils.web.WebUtils;
import winterwell.utils.web.WebUtils2;

/**
 * Internal container for document. Uses JSoup as a lenient parser.
 * <p>
 * To store metadata
 * for a specific part of a document an Item with extracted metadata should
 * be added to an object of this class.
 * 
 * <p>
 *  JSoup provides some handy selection methods via .select():
 *  http://jsoup.org/cookbook/extracting-data/selector-syntax
 *  
 * @author ivan 
 */
public class JuiceMe 

{

	public String toString() {
		if (url!=null) return "JuiceMe["+url+"]"; 
		if (html!=null) return "JuiceMe["+StrUtils.ellipsize(WebUtils.stripTags(html), 140)+"]";
		return super.toString();
	}
	
	String html;
	String url;
	Element doc;
	
	private final List<Item> extractedItems = new ArrayList<Item>();

	private String domain;	
	
	public JuiceMe(String url, String html) {
		assert html != null : url;
		this.url = url;
		this.html = html;
		this.doc = Jsoup.parse(html, url);
	}
	
	// Protected constructor for Unit-testing where web-pages are read from
	// a file, not from the Internet
	JuiceMe(String html) {
		this.html = html;
		this.doc = Jsoup.parse(html);
	}
	
	// Don't expose JSoup
//	JuiceMe(Document doc) {
//		this.doc = doc;
//	}

	public JuiceMe(String url, Element element) {
		this.url = url;
		this.doc = element;
		
	}

	/**
	 * 
	 * @return the items found in this page. Never empty (a blank item will be created via {@link #getMainItem()} if need be)
	 */
	public List<Item> getExtractedItems() {
		if (extractedItems.isEmpty()) {
			getMainItem();
		}
		return extractedItems;
	}
	
	public String getHTML() {
		if (html != null) {
			return html;
		}
		
		return doc.html();
	}

	/**
	 * Add item extracted from a document
	 * @param item
	 */
	public void addItem(Item item) {
		extractedItems.add(item);		
	}

	public Element getDoc() {
		return doc;
	}

	public String getURL() {
		return url;
	}

	/**
	 * Get all items of a specified type.
	 * @param requiredType
	 * @return list of extracted items of a specified types or empty list
	 */
	public List<Item> getItemsOfType(KMsgType requiredType) {
		List<Item> itemsOfType = new ArrayList<Item>();
		
		for (Item item : extractedItems) {
			if (item.getType() == requiredType) {			
				itemsOfType.add(item);
			}
		}
		
		return itemsOfType;
	}

	/**
	 * Convenience to access the first Item, which should be the main part of the page.
	 * @return Item. Never null (the item will be created if need be)
	 */
	public Item getMainItem() {
		if (extractedItems.isEmpty()) {
			Item item = new Item(getDoc(), url);		
			addItem(item);
		}
		return extractedItems.get(0);
	}

	/**
	 * @return e.g. google.com. Can be null if the url is relative.
	 */
	public String getDomain() {
		if (domain==null) {
			if (getURL()==null) return null;
			domain = WebUtils.getDomain(getURL());
		}
		return domain;			
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return Can be empty, never null
	 */
	public <V> List<Item> getItemsMatching(Key<V> key, V value) {
		List<Item> items = getExtractedItems();
		List<Item> matched = new ArrayList(4);
		for (Item item : items) {
			V v = item.get(key);
			if (value.equals(v)) {
				matched.add(item);
			}
		}
		return matched;
	}

	public boolean removeItem(Item item) {
		return extractedItems.remove(item);
	}
	
}
