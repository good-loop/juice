package com.winterwell.juice.juicers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import winterwell.utils.Key;
import winterwell.utils.Utils;
import winterwell.utils.reporting.Log;
import winterwell.utils.web.WebUtils;
import winterwell.utils.web.WebUtils2;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Anno;
import com.winterwell.juice.DateFinder;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;

import creole.data.XId;

/**
 * TODO a juicer for phpBB bulletin boards.
 * E.g. bikeradar.com/forums
 * @author daniel
 * @testedby {@link PhpBBJuicerTest}
 */
public class PhpBBJuicer extends AJuicer {

	private static final String LOGTAG = "PhpBBJuicer";

	@Override
	protected boolean juice(JuiceMe doc) {
		if ( ! doc.getHTML().contains("phpBB")) {
			return false;
		}	
		
		// Is it an index page?? Get the linked-to forums
		juiceIndexPage(doc);
		
		// A thread??
		juiceThread(doc);
		
		// Clean-up: remove a blank main-item?
		List<Item> items = doc.getExtractedItems();
		if (items.size() < 2) return false;
		
		Item main = doc.getMainItem();
		if (main.getAnnotations().isEmpty()) {
			Log.d("Removing blank main: "+main);
			doc.removeItem(main);
		}
		
		// TODO Auto-generated method stub
		return false;
	}

	private void juiceThread(JuiceMe doc) {
		Elements posts = doc.getDoc().getElementsByClass("post");
		String domain = WebUtils2.getDomain(doc.getURL());
		for (Element post : posts) {
			String id = post.attr("id");
			Item ref = new Item(post, doc.getURL());
//			ref.put(anno(URL, href, post)); TODO #id
			ref.put(anno(XID, id+"@"+domain, post));
			Element content = getFirstElementByClass(post, "content");
			if (content==null) {
				continue;
			}
			String ctext = content.html();
			ref.put(anno(POST_BODY, ctext, content));
			
			// Title
			Elements h3 = post.getElementsByTag("h3");
			if ( ! h3.isEmpty()) {
				ref.put(anno(TITLE, h3.get(0).text(), h3.get(0)));
			}
			
			// Pub-date
			Elements authorInfos = post.getElementsByClass("author");
			if ( ! authorInfos.isEmpty()) {
				DateFinder df = new DateFinder();
				List<Anno> dates = df.findDates(authorInfos.get(0));
				if ( ! dates.isEmpty()) {
					ref.put(dates.get(0));
				}
			}
			
			// Add to the doc!
			doc.addItem(ref);
			
			Element pp = getFirstElementByClass(post, "postProfile");			
			if (pp==null) continue;
			String pph = pp.html();
			Elements img = pp.getElementsByTag("img");
			if ( ! img.isEmpty()) {
				String src = img.attr("src");
				ref.put(anno(AUTHOR_IMG, src, img.get(0)));
			}
			Elements ppas = pp.getElementsByTag("a");
			for (Element ppa : ppas) {
				String ppat = ppa.text();
				if (Utils.isBlank(ppat)) continue;
				ref.put(anno(AUTHOR_NAME, ppat, ppa));
				String href = ppa.attr("href");
				if (href!=null) {
					href = url(href, doc);
					ref.put(anno(AUTHOR_URL, href, ppa));
					String uid = WebUtils2.getQueryParameter(href, "u");
					if ( ! Utils.isBlank(uid)) {
						ref.put(anno(AUTHOR_XID, XId.WART_P+uid+"@"+domain, ppa));	
					}
				}
				break;
			}
			
			// Extra info
			String ppt = WebUtils.stripTags(pp.html());			
//			Posts: 3840
//			Joined: Thu Mar 25, 2010 5:40 pm
//			Location: Cider country
			Matcher m = KEY_VALUE.matcher(ppt);
			while(m.find()) {
				String k = m.group(1).toLowerCase();
				String v = m.group(2);
				if ("location".equals(k)) {
					ref.put(anno(AUTHOR_LOCN, v, pp));
				} else if ("posts".equals(k)) {
					ref.put(anno(new Key("posts"), Integer.valueOf(v), pp));
				} else if ("joined".equals(k)) {
//					?? ref.put(anno(AUTHOR_REGISTERED_DATA, Integer.valueOf(v), pp));
				}
			}
		}
	}
	
	static Pattern KEY_VALUE = Pattern.compile("^([a-zA-Z]+):\\s(.+)$", Pattern.MULTILINE);

	void juiceIndexPage(JuiceMe doc) {
		Elements as = doc.getDoc().getElementsByTag("a");
		for (Element element : as) {
			String href = element.attr("href");
			if (href==null) continue;
//			<a href="./viewforum.php?f=40041" class="subforum read" title="No unread posts">Amateur Race</a>
			if (href.contains("viewforum.php?") || element.hasClass("subforum") || element.hasClass("forumtitle")) {
				List<Item> _items = doc.getItemsMatching(URL, href);
				// Strip session ID, if present
				href = url(href, doc);				
				// Already stored??
				List<Item> items = doc.getItemsMatching(URL, href);
				if ( ! items.isEmpty()) {
					continue;
				}
				// A link! Store it
				Item ref = new Item(element, doc.getURL());
				ref.put(anno(URL, href, element));
				ref.put(anno(TITLE, element.text(), element));
				ref.setStub(true);
//				ref.setType(type); ??
				doc.addItem(ref);
			}
		}
	}

	/**
	 * Resolve & strip session ID, if present
	 * @param href
	 * @param doc
	 * @return
	 */
	private String url(String href, JuiceMe doc) {
		href = WebUtils2.removeQueryParameter(href, "sid");
		assert ! href.contains("sid=") : href;
		href = WebUtils2.resolveUri(doc.getURL(), href).toString();
		return href;
	}

}
