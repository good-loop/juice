package com.winterwell.juice.juicers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;

import winterwell.utils.Key;
import winterwell.utils.Utils;
import winterwell.web.email.SimpleMessage;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Anno;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;

/**
 * TODO Emails, phone numbers, addresses
 * @author daniel
 *
 */
public class ContactDetailsJuicer extends AJuicer {

	@Override
	public boolean juice(JuiceMe doc) {
		List<Item> items = doc.getExtractedItems();
		for (Item item : items) {
			List<Anno> emails = findEmails(item, item.getDoc());
			if (emails.size()==1) {
				item.putIfAbsent(emails.get(0));
			} else if (! emails.isEmpty()) {
				// TODO ??
				item.putIfAbsent(emails.get(0));
			}
		}
		return false;
	}

	private List<Anno> findEmails(Item item, Element doc) {
		ArrayList emails = new ArrayList();
		// mailto links
		Elements links = doc.getElementsByTag("a");
		for (Element link : links) {
			String href = link.attr("href");
			if (href==null || ! href.startsWith("mailto:")) continue;
			String m = href.substring("mailto:".length()).trim();
			if (Utils.isBlank(m)) continue;
			Anno<String> anno = anno(EMAIL, m, link);
			emails.add(anno);	
		}
		// regex
		String text = doc.text();
		if (text==null) return emails;
		Matcher m = SimpleMessage.EMAIL_REGEX2.matcher(text);
		while (m.find()) {
			Anno<String> anno = anno(EMAIL, m.group(), doc);
			emails.add(anno);
		}
		return emails;
	}

}
