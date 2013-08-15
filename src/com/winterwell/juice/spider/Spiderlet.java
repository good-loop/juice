package com.winterwell.juice.spider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import winterwell.utils.StrUtils;
import winterwell.utils.reporting.Log;
import winterwell.utils.time.Dt;
import winterwell.utils.time.TUnit;
import winterwell.utils.web.WebUtils;
import winterwell.utils.web.WebUtils2;
import winterwell.web.FakeBrowser;

import com.winterwell.juice.Item;
import com.winterwell.utils.threads.ATask;

public class Spiderlet extends ATask<Item> {

	@Override
	public String toString() {	
		return getClass().getSimpleName()+"["+url+"]";
	}
	
	int depth; 	
	String url;

	SiteSpider spider;
	List<String> links;
	
	protected Spiderlet(SiteSpider spider, String url, int depth) {
		this.spider = spider;
		this.url = url;
		this.depth = depth;
		// don't wait too long
		setMaxTime(new Dt(10, TUnit.SECOND));
	}
	
	static Pattern aLink = Pattern.compile("href=['\"]?([^ '\">]+)['\"]?");
	
	@Override
	protected Item run() throws Exception {
		Log.d("spider", "\tFetching "+url+"...");
		// Fetch
		String html = fetchPage();
		// analyse it
		Item item = analyse(html);
		spider.reportAnalysis(url, item);
		// Extract links in the web
		links = extractLinks(html, item);
		spider.reportLinks(url, links, depth);
		return item;
	}

	/**
	 * Analyse the page. Over-ride to actually do anything.
	 * @param html
	 * @return Can return null for "boring"
	 */
	protected Item analyse(String html) {
		Item dummy = new Item(null, url);
		return dummy;
	}

	protected List<String> extractLinks(String html, Item itemIsIgnored) {
		Matcher m = aLink.matcher(html);
		List<String> links = new ArrayList(); 
		while(m.find()) {
			String link = m.group(1);
			if (link.isEmpty() || link.startsWith("\\")) {
				// Possibly a javascript embed, e.g. s += '<a href=\"' + google_info.feedback_url + '\" ...				continue;
				Log.d(SiteSpider.LOGTAG, "Empty href in "+url+": '"+link+"' in "+StrUtils.compactWhitespace(StrUtils.substring(html, m.start()-20, m.end()+30)));
				continue;
			}
			try {
				URI link2 = WebUtils2.resolveUri(url, link);
				// TODO: SHould we strip out "known boring" parameters? E.g. google tracking codes?
				links.add(link2.toString());
			} catch(Exception ex) { 
				// Bad URI syntax :( 
				Log.w(SiteSpider.LOGTAG, "Bad href in "+url+": '"+link+"' in "+StrUtils.compactWhitespace(StrUtils.substring(html, m.start()-20, m.end()+30)));
			}
		}
		return links;
	}

	protected String fetchPage() {
		FakeBrowser fb = new FakeBrowser();
		return fb.getPage(url);
	}
	
	
}
