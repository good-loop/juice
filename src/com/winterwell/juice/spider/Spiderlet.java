package com.winterwell.juice.spider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import winterwell.web.FakeBrowser;
import com.winterwell.web.WebEx;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Item;
import com.winterwell.juice.KMsgType;

import winterwell.utils.StrUtils;
import winterwell.utils.Utils;
import winterwell.utils.reporting.Log;

import com.winterwell.utils.threads.ATask;
import winterwell.utils.web.WebUtils2;

import winterwell.utils.time.Dt;
import winterwell.utils.time.TUnit;
import winterwell.utils.web.WebUtils;
import creole.data.XId;

public class Spiderlet extends ATask<List<Item>> {

	@Override
	public String toString() {	
		return getClass().getSimpleName()+"["+url+"]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + depth;
		result = prime * result + ((spider == null) ? 0 : spider.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Spiderlet other = (Spiderlet) obj;
		if (depth != other.depth)
			return false;
		if (spider == null) {
			if (other.spider != null)
				return false;
		} else if (!spider.equals(other.spider))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	int depth; 	
	String url;

	SiteSpider spider;
	
	final List<String> links = new ArrayList();
	
	protected Spiderlet(SiteSpider spider, String url, int depth) {
		this.spider = spider;
		this.url = url;
		this.depth = depth;
		// don't wait too long
		setMaxTime(new Dt(10, TUnit.SECOND));
	}
	
	static Pattern aLink = Pattern.compile("href=['\"]?([^ '\">]+)['\"]?");
	
	/**
	 * @return Just for debugging!
	 */
	@Override
	protected List<Item> run() throws Exception {
		if (spider.delay > 0) {
			Utils.sleep(Utils.getRandom().nextInt(spider.delay));
		}
		Log.d("spider", "\tFetching "+url+"...");
		// Fetch
		String html = fetchPage();
		// ...failed? e.g. a 404
		if (html==null) return null;
		// analyse it
		List<Item> items = analyse(html);
		for (Item item : items) {
			spider.reportAnalysis(item.getXId2(), item);
			extractLinks(item, links);
		}		
		// Extract links in the web
		extractLinks(html, links);
		spider.reportLinks(new XId(url, "web"), links, depth);
		return items;
	}

	/**
	 * Extract links from this item. Does nothing by default, as {@link #extractLinks(String, List)}
	 * will get the lot from html.
	 * @param item
	 * @param links
	 */
	protected void extractLinks(Item item, List<String> links) {
		// do nothing
	}

	/**
	 * Analyse the page. Over-ride to actually do anything.
	 * @param html
	 * @return Can return null for "boring"
	 */
	protected List<Item> analyse(String html) {
		Item dummy = new Item(null, url);
		return Collections.singletonList(dummy);
	}

	protected void extractLinks(String html, List<String> links) {
		Matcher m = aLink.matcher(html);
		while(m.find()) {
			String link = m.group(1);			
			if (link.isEmpty() || link.startsWith("\\")) {
				// Possibly a javascript embed, e.g. s += '<a href=\"' + google_info.feedback_url + '\" ...				continue;
//				Log.d(SiteSpider.LOGTAG, "Empty href in "+url+": '"+link+"' in "+StrUtils.compactWhitespace(StrUtils.substring(html, m.start()-20, m.end()+30)));
				continue;
			}
			// In case some numpty has &amp; in their url (it seems to be a common enough mistake).
			link = WebUtils2.htmlDecode(link);
			try {
				URI link2 = WebUtils2.resolveUri(url, link);
				// TODO: SHould we strip out "known boring" parameters? E.g. google tracking codes? referrer?
				// Or common session-id markers, like "sid"?? 
				links.add(link2.toString());
			} catch(Exception ex) { 
				// Bad URI syntax :( It happens. We could try to correct -- but sod it. 
//				Log.d(SiteSpider.LOGTAG, "Bad href in "+url+": '"+link+"' in "+StrUtils.compactWhitespace(StrUtils.substring(html, m.start()-20, m.end()+30)));
			}
		}
	}

	protected String fetchPage() {
		assert spider.url2spiderlet.get(url) == this : spider.url2spiderlet.get(url)+" vs "+this;
		try {
			FakeBrowser fb = new FakeBrowser();
			fb.setTimeOut(spider.PAGE_FETCH_TIMEOUT);
			String page = fb.getPage(url);

			// Was there a redirect?
			String locn = fb.getLocation();
			if ( ! url.equals(locn)) {
				boolean ok = spider.reportRedirect(url, locn, this);
				this.url = locn;
				if ( ! ok) {
					return null; // _this_ url has already been handled
				}				
			}
			return page;	
		} catch(Exception ex) {
			// oh well
			handleError(url, ex);
			return null;
		}	
	}

	protected void handleError(String url2, Exception ex) {
		Item item = new Item(ex, null, url2);		
		spider.reportAnalysis(item.getXId2(), item);
	}
	
	
}
