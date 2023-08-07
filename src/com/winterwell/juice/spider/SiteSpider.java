package com.winterwell.juice.spider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.winterwell.maths.graph.DiEdge;
import com.winterwell.maths.graph.DiGraph;
import com.winterwell.maths.graph.DiNode;
import com.winterwell.utils.IFilter;
import com.winterwell.utils.Utils;

import com.winterwell.juice.Item;
import com.winterwell.utils.MathUtils;
import com.winterwell.utils.log.Log;
import com.winterwell.utils.threads.ATask;
import com.winterwell.utils.threads.TaskRunner;
import com.winterwell.utils.web.WebUtils;

import com.winterwell.web.data.XId;

/**
 * Spider a single website. Holds everything in memory!
 * 
 * @author Daniel
 * @testedby  SiteSpiderTest
 */
public class SiteSpider extends ATask<DiGraph<Item>> {

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}
	
	public void setUrlFilter(IFilter<String> urlFilter) {
		this.urlFilter = urlFilter;
	}
	
	public IFilter<String> getUrlFilter() {
		return urlFilter;
	}
	
	static final String LOGTAG = "spider";

	private static final Object REDIRECT = "redirect";

	public static final String SERVICE_WEB = "web";

	private int maxPages = 1000;
	
	/**
	 * if depth > maxDepth - stop. So e.g. maxDepth=1 means "starting page and one-deep from there"
	 */
	private int maxDepth = 5;

	ConcurrentHashMap<XId,DiNode<Item>> xid2node = new ConcurrentHashMap();
	
	/**
	 * Note: Olde Spiderlets are NOT removed
	 */
	ConcurrentHashMap<String,Spiderlet> url2spiderlet = new ConcurrentHashMap();
	

	DiGraph<Item> _web = new DiGraph<Item>();

	private String startUrl;

	private double randomSkip;
	
	/**
	 * 0 by default. Sets a probability that a link will _not_ be followed.
	 * @param randomSkip [0,1]
	 */
	public void setRandomSkip(double randomSkip) {
		this.randomSkip = randomSkip;
		assert MathUtils.isProb(randomSkip);
	}

	
	public SiteSpider(String startUrl) {
		this.startUrl = startUrl;
		 urlFilter = new InSiteFilter(startUrl);
	}
	
	void reportLinks(XId xid, List<String> links, int depth) {
		// Update the web
		DiNode<Item> s = xid2node.get(xid);
		assert s != null : xid;
		List<String> recurse = new ArrayList(links.size());
		for(String u : links) {
			if (xid.getName().equals(u)) {
				continue; // ignore self links
			}
			// Filter?
			if ( ! urlFilter.accept(u)) {
//				Log.d(LOGTAG, "	filter out "+u);
				continue; // ignore (and don't recurse on this)
			}		
			DiNode<Item> e = getCreateNode(url2xid(u));
			getCreateEdge(s,e);
			recurse.add(u);
		}
		if (depth >= maxDepth) {
			return; // stop here
		}
		// Recurse...
		// TODO pick random/best links from within a page??
		for(String u : recurse) {			
			// Random stop?
			if (Utils.getRandomChoice(randomSkip)) {
				Log.d(LOGTAG, "	random skip "+u);
				continue;
			}			
			Spiderlet spiderlet = newSpiderlet(u, maxDepth+1);
			Spiderlet old = url2spiderlet.putIfAbsent(u, spiderlet);
			if (old==null) _runner.submitIfAbsent(spiderlet);
			else {
				// already in hand
			}
		}		
	}

	synchronized DiEdge getCreateEdge(DiNode<Item> s, DiNode<Item> e) {
		DiEdge edge = _web.getEdge(s, e);
		if (edge!=null) return edge;
		edge = _web.addEdge(s, e, null); //??store time of finding?
		return edge;
	}

	private synchronized DiNode<Item> getCreateNode(XId xid) {
		assert xid != null;
		DiNode<Item> n = xid2node.get(xid);
		if (n != null) return n;
		n = _web.addNode(new DummyItem(xid));	
		DiNode<Item> n2 = xid2node.putIfAbsent(xid, n);
		if (n2==null) {
			return n;
		}
		// We lost a race
		assert n2 != n : xid;
		_web.removeNode(n);
		return n2;
	}

	IFilter<String> urlFilter;
	
	static final TaskRunner _runner = new TaskRunner(15) {
		public void report(Object runnableOrCallable, Throwable e) {
			super.report(runnableOrCallable, e);
		};
	};

	static final long PAGE_FETCH_TIMEOUT = 5000;
	 
	/**
	 * Delay to put in between requests. This is used as a uniform delay range (so the average delay is half this).
	 */
	int delay = 50;

	@Override
	public DiGraph<Item> run() {
		assert startUrl!=null;
		Spiderlet starter = newSpiderlet(startUrl, 0);
		DiNode<Item> root = getCreateNode(url2xid(startUrl));
		url2spiderlet.put(startUrl, starter);
		_runner.submit(starter);
		
		while(_runner.getQueueSize() != 0) {
			Log.d(LOGTAG, "Queue: "+_runner.getQueueSize());
			Utils.sleep(100);			
		}
		return _web;
	}

	protected Spiderlet newSpiderlet(String url, int depth) {
		return new Spiderlet(this, url, depth);
	}

	/**
	 * @param url Cannot be null
	 * @param item Can be null
	 */
	void reportAnalysis(XId xid, Item item) {
		assert xid != null;		
		DiNode<Item> n = getCreateNode(xid);
		assert n != null : xid;
		if (item==null) {
			if (n.getValue() != null) {
				return; // already done
			}			
			// store a dummy
			item = new DummyItem(xid); 
		} else if (n.getValue() !=null) { 
			// Merge with existing item??
			Item v = n.getValue();
			if (v instanceof DummyItem || v.isStub()) {
				// carry on and over-write v
				if (v.isStub()) {
					Log.d(LOGTAG, "replace stub for "+xid);
				}
			} else {
				Log.d(LOGTAG, "Merge items for "+xid);
				v.extend(item);
				return;
			}
		}
		
		n.setValue(item);
	}

	public String getStartUrl() {
		return startUrl;
	}

	/**
	 * 
	 * @param fromUrl
	 * @param toUrl
	 * @return true if the spiderlet should carry on to toUrl.
	 * false => toUrl is excluded by the filter, or already spidered. 
	 */
	boolean reportRedirect(String fromUrl, String toUrl, Spiderlet spiderlet) {
		assert ! fromUrl.equals(toUrl);
		assert toUrl != null : fromUrl;
		if (urlFilter!=null && ! urlFilter.accept(toUrl)) {
			return false;
		}
		// Update the web
		DiNode<Item> s = xid2node.get(url2xid(fromUrl));
		DiNode<Item> e = getCreateNode(url2xid(toUrl));
		DiEdge edge = getCreateEdge(s,e);
		edge.setValue(REDIRECT);
		// Is toUrl known?
		Spiderlet old = url2spiderlet.putIfAbsent(toUrl, spiderlet);
		return old==null;
		
	}

	protected XId url2xid(String url) {
		return new XId(url, SiteSpider.SERVICE_WEB);
	}
	
		
	
}

final class InSiteFilter implements IFilter<String> {

	private static final String[] BLAH = ".css,.doc,.js,.json,.pdf,.png,.gif,.jpg,.rss".split(",");
	private String domain;
	private Pattern domainP;
	
	public String toString() {
		return "InSiteFilter["+domain+"]";
	}

	public InSiteFilter(String startUrl) {
		this.domain = WebUtils.getDomain(startUrl);
		domainP = Pattern.compile("^https?://(\\w+\\.)?"+Pattern.quote(domain));
	}

	@Override
	public boolean accept(String url) {
		// exclude non-page file types
		String type = WebUtils.getType(url);
		if (type!=null) {
			type = type.toLowerCase();
			for(String blah : BLAH) {
				if (type.equals(blah)) {
					return false;
				}
			}
		}
		
		boolean ok = domainP.matcher(url).find();
		return ok;
	}
	
}

/**
 * Holds space while we fetch the page...
 */
class DummyItem extends Item {

	Spiderlet spiderlet;
	public DummyItem(XId url) {
		super(null, url.getName());
	}
	
}

