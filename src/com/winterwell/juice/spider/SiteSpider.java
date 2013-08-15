package com.winterwell.juice.spider;

import java.net.URI;
import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.winterwell.juice.Item;
import com.winterwell.utils.threads.ATask;
import com.winterwell.utils.threads.TaskRunner;

import creole.data.XId;
import winterwell.maths.graph.DiEdge;
import winterwell.maths.graph.DiGraph;
import winterwell.maths.graph.DiNode;
import winterwell.utils.IFilter;
import winterwell.utils.MathUtils;
import winterwell.utils.Printer;
import winterwell.utils.Utils;
import winterwell.utils.reporting.Log;
import winterwell.utils.time.Time;
import winterwell.utils.web.WebUtils;
import winterwell.utils.web.WebUtils2;

/**
 * Spider a single website. Holds everything in memory!
 * 
 * @author Daniel
 * @testedby {@link SiteSpiderTest}
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
	
	private int maxDepth = 5;

	ConcurrentHashMap<XId,DiNode<Item>> xid2node = new ConcurrentHashMap();
	
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
		// TODO pick rnadom/best links from within a page??
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
		if (n2==null) return n;
		// We lost a race
		assert n2 != n : xid;
		_web.removeNode(n);
		return n2;
	}

	IFilter<String> urlFilter;
	
	TaskRunner _runner = new TaskRunner(10) {
		public void report(Object runnableOrCallable, Throwable e) {
			super.report(runnableOrCallable, e);
		};
	};
	 
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

