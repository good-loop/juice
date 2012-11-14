package com.winterwell.juice;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import winterwell.utils.StrUtils;
import winterwell.utils.web.WebUtils;

/**
 * Internal container for document.
 * 
 * It extends Item, to store document-level metadata. To store metadata
 * for a specific part of a document an Item with extracted metadata should
 * be added to an object of this class.
 * 
 * @author ivan
 */
public class JuiceMe 

// extends Item 
// Dan: I think this is a mistake, because it is unclear whether to look at the top-level JuiceMe
// object (as MetaDataJuicer does), or the contained items (as WordPressJuicer does).
// Also, it is 1 level (but no deeper) of a tree structure, which is a bit odd.
// We are doing tree-structure instead via the PREVIOUS annotation (because it allows for cross-page threading).

{

	// Dan: toString methods are nice for debugging
	public String toString() {
		if (url!=null) return "JuiceMe["+url+"]"; 
		if (html!=null) return "JuiceMe["+StrUtils.ellipsize(WebUtils.stripTags(html), 140)+"]";
		return super.toString();
	}
	
	String html;
	String url;
	Element doc;
	
	private final List<Item> extractedItems = new ArrayList<Item>();	
	
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
	 * @return the items found in this page
	 */
	public List<Item> getExtractedItems() {
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
	void addItem(Item item) {
		extractedItems.add(item);		
	}

	public Element getDoc() {
		return doc;
	}

	public String getURL() {
		return url;
	}

//	/**
//	 * Get all items of a specified type.
//	 * @param requiredType
//	 * @return list of extracted items of a specified types or empty list
//	 */
//	public List<Item> getItemsOfType(KMsgType requiredType) {
//		List<Item> itemsOfType = new ArrayList<Item>();
//		
//		for (Item item : extractedItems) {
//			if (item.getType() == requiredType) {			
//				itemsOfType.add(item);
//			}
//		}
//		
//		return itemsOfType;
//	}
	
}
