package com.winterwell.juice.juicers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.winterwell.utils.Utils;
import com.winterwell.web.email.SimpleMessage;
import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Anno;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.utils.web.WebUtils2;

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
				// TODO ?? we could remove emails which occur in several items
				item.putIfAbsent(emails.get(0));
			}
		}
		for (Item item : items) {
			List<Anno> phones = findPhoneNumbers(item, item.getDoc());
			if (phones.isEmpty()) continue;
			item.putIfAbsent(phones.get(0));
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
			// screen out e.g. "mailto:?subject=Hello&body=ref"
			if (m.indexOf('?') != -1) {
				m = m.substring(0, m.indexOf('?'));
			}
			if (Utils.isBlank(m)) continue;
			if (WebUtils2.isValidEmail(m)) {
				Anno<String> anno = anno(EMAIL, m, link);
				emails.add(anno);
			} else {
				// skip invalid email address 
			}
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
	

	private List<Anno> findPhoneNumbers(Item item, Element doc) {
		// TODO
		ArrayList emails = new ArrayList();
		return emails;
	}


}
