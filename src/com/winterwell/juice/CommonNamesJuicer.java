/**
 * 
 */
package com.winterwell.juice;

import org.jsoup.nodes.Element;

/**
 * Crude hack! Guess stuff from commonly used css class names.
 * @author daniel
 *
 */
public class CommonNamesJuicer extends AJuicer {

	static final String LOGTAG = "CommonNamesJuicer";

	@Override
	boolean juice(JuiceMe doc) {
		// Main body?
		Item item = doc.getMainItem();
		String body = item.get(POST_BODY);
		if (body==null) {
			juice2_body(doc);
		}
		// Author?
		String oxid = item.get(AUTHOR_XID);
		if (oxid==null || oxid.startsWith("anon@")) {
			juice2_author(doc);
		}
		return false;
	}

	private void juice2_body(JuiceMe doc) {
		Element e = getFirstElementByClass(doc.getDoc(), 
				"main-content", "post-body", "post-content", "main");
		if (e!=null) {
			Item item = doc.getMainItem();
			String text = e.text();
			item.putIfAbsent(anno(POST_BODY, text, e));
		}
	}

	private void juice2_author(JuiceMe doc) {
		if (true) return; // TODO more work
		// TODO we'd be better scanning every div, p and span for author class
		Element e = getFirstElementByClass(doc.getDoc(), 
				"author-details", "author", "post-author-details", "article-author-details");
		if (e!=null) {
			Item item = doc.getMainItem();
			String text = e.text();
			// TODO
			//item.putIfAbsent(anno(AUTHOR_XID, text, e));
		}
	}

}
