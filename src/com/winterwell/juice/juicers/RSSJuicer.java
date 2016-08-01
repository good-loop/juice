package com.winterwell.juice.juicers;

import javax.mail.internet.InternetAddress;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import winterwell.utils.Utils;
import winterwell.utils.time.Time;
import com.winterwell.utils.web.WebUtils;
import com.winterwell.utils.web.WebUtils2;
import winterwell.web.fields.DateField;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;

import creole.data.XId;

/**
 * TODO Juice RSS feeds -- really easy :)
 * @author daniel
 *
 */
public class RSSJuicer extends AJuicer {

	@Override
	protected boolean juice(JuiceMe doc) {
		// Is it RSS?
		Element root = doc.getDoc();
		if ( ! root.tagName().equals("rss")) {
			return false;
		}
			
		Elements items = root.getElementsByTag("item");
		for (Element node : items) {
			String author = node.getElementsByTag("author").text();
			String title = node.getElementsByTag("title").text();
			String link = node.getElementsByTag("link").text();
			String contents = node.getElementsByTag("description").text();
			String pubDate = node.getElementsByTag("pubDate").text();
//				TODO List<String> category = WebUtils.xpathExtractStrings("category", node);
			if (title==null || contents==null || link==null) continue;
			Time time = Utils.isBlank(pubDate)? null 
						: DateField.parse(pubDate);
			Item page = new Item(node, doc.getURL());
			doc.addItem(page);
			// link, title, contents, time);
			page.put(anno(URL, link, null));
			page.put(anno(TITLE, title, null));
			page.put(anno(DESC, contents, null));
			if (pubDate != null) {
				Time t = DateField.parse(pubDate);
				page.put(anno(PUB_TIME, t, null));
			}
			
			String authr = null;
			if (WebUtils2.isValidEmail(author)) {
				InternetAddress email = WebUtils2.internetAddress(author);
				authr = new XId(WebUtils2.canonicalEmail(email), "Email").toString();
			} else if ( ! Utils.isBlank(author)) {
				String service = WebUtils.getDomain(doc.getURL());
				authr = author+"@"+service;
			}
			if (authr!=null) page.put(anno(AUTHOR_XID, authr, null));									
			
			// TODO tag category?? (no -- security issues)
			// TODO add tags if we can identify that the author is a user
//				if ( ! category.isEmpty()) {
//					for(String cat : category) {
//						text.addTag(cat);
//					}
//				}							
		}
		return true;
	}

}
