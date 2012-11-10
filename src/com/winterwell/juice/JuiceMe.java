package com.winterwell.juice;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Internal container for document.
 * 
 * It extends Item, to store document-level metadata. To store metadata
 * for a specific part of a document an Item with extracted metadata should
 * be added to an object of this class.
 * 
 * @author ivan
 */
public class JuiceMe extends Item {

	String html;
		
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
	
	JuiceMe(Document doc) {
		this.doc = doc;
	}

	List<Item> getExtractedItems() {
		return extractedItems;
	}
	
	@Override
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
	
}
