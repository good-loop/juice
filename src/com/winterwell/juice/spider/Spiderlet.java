package com.winterwell.juice.spider;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import winterwell.utils.StrUtils;
import winterwell.utils.Utils;
import winterwell.utils.reporting.Log;
import winterwell.utils.time.Dt;
import winterwell.utils.time.TUnit;
import winterwell.utils.web.WebUtils;
import winterwell.utils.web.WebUtils2;
import winterwell.web.FakeBrowser;
import winterwell.web.WebEx;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Item;
import com.winterwell.juice.KMsgType;
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
	
	/**
	 * @return Just for debugging!
	 */
	@Override
	protected Item run() throws Exception {
		if (spider.delay > 0) {
			Utils.sleep(Utils.getRandom().nextInt(spider.delay));
		}
		Log.d("spider", "\tFetching "+url+"...");
		// Fetch
		String html = fetchPage();
		// ...failed? e.g. a 404
		if (html==null) return null;
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
			// In case some numpty has &amp; in their url (it seems to be a common enough mistake).
			link = WebUtils2.htmlDecode(link);
			try {
				URI link2 = WebUtils2.resolveUri(url, link);
				assert ! link2.toString().contains("www.bikeradar.com/viewforum.php") 
					: link2+" from "+url+" + "+link+" in "+StrUtils.compactWhitespace(StrUtils.substring(html, m.start()-20, m.end()+30));
				// TODO: SHould we strip out "known boring" parameters? E.g. google tracking codes? referrer?
				// Or common session-id markers, like "sid"?? 
				links.add(link2.toString());
			} catch(Exception ex) { 
				// Bad URI syntax :( It happens. We could try to correct -- but sod it. 
				Log.d(SiteSpider.LOGTAG, "Bad href in "+url+": '"+link+"' in "+StrUtils.compactWhitespace(StrUtils.substring(html, m.start()-20, m.end()+30)));
			}
		}
		return links;
	}

	protected String fetchPage() {
		try {
			FakeBrowser fb = new FakeBrowser();			
			String page = fb.getPage(url);
			// Was there a redirect?
			String locn = fb.getLocation();
			if ( ! url.equals(locn)) {
				boolean ok = spider.reportRedirect(url, locn);
				this.url = locn;
				if ( ! ok) {
					return null; // _this_ url has already been handled
				}
			}
			return page;
		} catch(WebEx ex) {
			// oh well
			handleError(url, ex);
			return null;
		}
	}

	protected void handleError(String url2, WebEx ex) {
		Item item = new Item(ex, null, url2);		
		spider.reportAnalysis(url, item);
	}
	
	
}
