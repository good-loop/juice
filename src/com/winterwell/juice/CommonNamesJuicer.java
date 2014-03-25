/**
 * 
 */
package com.winterwell.juice;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.winterwell.utils.Utils;
import com.winterwell.utils.reporting.Log;

/**
 * Crude hack! Guess stuff from commonly used css class names.
 * @author daniel
 *
 */
public class CommonNamesJuicer extends AJuicer {

	static final String LOGTAG = "CommonNamesJuicer";

	@Override
	public boolean juice(JuiceMe doc) {
		// Main body?
		Item item = doc.getMainItem();
		String body = item.get(POST_BODY);
		if (body==null) {
			juice2_body(doc);
		}
		// Title?
		if (Utils.isBlank(item.getTitle())) {
			juice2_title(doc);
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

	private void juice2_title(JuiceMe doc) {
		Elements ts = doc.getDoc().getElementsByTag("title");
		if (ts==null || ts.isEmpty()) return;
		Element te = ts.get(0);
		Item item = doc.getMainItem();
		String text = te.text();
		// TODO should we throw away the blog/site identifier? E.g. stuff before/after a |??
		if ( ! Utils.isBlank(text)) {
			item.putIfAbsent(anno(TITLE, text, te));
		}
	}

	
	private void juice2_author(JuiceMe doc) {
		// TODO we'd be better scanning every div, p and span for author class
		Element e = getFirstElementByClass(doc.getDoc(), 
				"author-details", "author", "post-author-details", "article-author-details");
		if (e!=null) {
			Item item = doc.getMainItem();
			String text = e.text();
			// TODO
			if ( ! Utils.isBlank(text)) {
				Log.d(LOGTAG, "TODO oxid from "+text);
			}
			//item.putIfAbsent(anno(AUTHOR_XID, text, e));
		}
	}

}
