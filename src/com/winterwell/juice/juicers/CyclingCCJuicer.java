package com.winterwell.juice.juicers;

import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import winterwell.utils.Utils;
import winterwell.utils.reporting.Log;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.spider.SiteSpider;

import creole.data.XId;

public class CyclingCCJuicer extends AJuicer {

//	http://www.cyclingweekly.cc/forum/all
	
//	<li class="forum i156301"><div class="forum teaseritem"><div class="col2"><div><div><div><a href="/profile/view/65171" title="buzz"><img class="avatar" width="85" height="85" src="http://www.cyclingweekly.cc/images/view/282310602512843db302a5efc5e574cc.85x85.jpg" alt="avatar"></a><div class="caption" style="text-align: center; width: 85px"><a href="/profile/view/65171" title="View Profile">buzz</a></div></div><p class="caption" style="width: 85px">5499 Posts</p></div></div><div><div style="clear: both">
	// <h3><a href="/forum/news-315/always-the-good-guys.-bad.-156301">Always the good guys. Bad.</a></h3>
	// <div class="topic i156301" style="overflow: auto"><p>Scottish Cycling lost one of their good guys today. A cyclist was killed when hit by a car from behind on the A91 between the villages of Strathmiglo and Gateside.
//			The man concerned was around 50, he ...</p></div><p class="byline">Started by <a href="/profile/view/65171">buzz</a> 6 hours ago in <a href="/forum/news-315">News</a>.</p></div></div></div></div></li>
			
//	http://www.cyclingweekly.cc/forum/racing-314/vuelta-a-espaa-chat-may-contain-spoilers-155467?page=3
	
	@Override
	protected boolean juice(JuiceMe doc) {
		Elements elements = doc.getDoc().select("li.forum");
		if (elements.size() > 1) {
			juiceIndex(elements, doc);
		}
		return false;
	}

	private void juiceIndex(Elements lis, JuiceMe doc) {
		for (Element li : lis) {
			// Text
			String threadId = null; // <li class="forum i156301">
			Set<String> classes = li.classNames();
			for (String c : classes) {
				if (c.startsWith("i")) {
					threadId = c;
				}
			}
			Elements hs = null;
			for(int i=2; i<5; i++) {
				hs = li.getElementsByTag("h"+i);
				if ( ! hs.isEmpty()) break;
			}
			if (hs==null || hs.isEmpty()) {
				Log.w("juicer.cc", "Skip no-title "+li.html());
				continue;
			}
			String title = hs.text();			
			Elements topics = li.getElementsByClass("topic");			
			String text = topics.text();
			
			Item item = new Item(li, doc.getURL());
			if ( ! Utils.isBlank(threadId)) {
				item.put(anno(THREAD_XID, new XId(threadId+"@"+doc.getDomain(), SiteSpider.SERVICE_WEB), li));
			}
			if ( ! Utils.isBlank(title)) item.put(anno(TITLE, title, hs.get(0)));
			item.put(anno(DESC, text, topics.get(0)));
			Elements as = hs!=null && ! hs.isEmpty()? hs.get(0).getElementsByClass("a") : null;
			if (as!=null && ! as.isEmpty()) {
				String href = as.attr("href");
				item.put(anno(URL, href, as.get(0)));
			}
			// XID -- will have to be semantic :(
			// Author
			as = li.getElementsByTag("a");
			for (Element a : as) {
				String href = a.attr("href");
				if (href.contains("/profile")) {
					String authName = a.text();
					if ( ! authName.isEmpty()) {
						item.put(anno(AUTHOR_NAME, authName, a));
						item.put(anno(AUTHOR_URL, href, a));
						item.put(anno(AUTHOR_XID, authName.toLowerCase()+"@"+doc.getDomain(), a));
					}
				}
			}
			Elements img = li.getElementsByTag("img");
			if ( ! img.isEmpty()) {
				item.put(anno(AUTHOR_IMG, img.attr("src"), img.get(0)));
			}
			// DOne
			doc.addItem(item);
		}
	}

}
