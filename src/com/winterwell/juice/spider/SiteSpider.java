package com.winterwell.juice.spider;

import java.net.URI;
import java.nio.file.DirectoryNotEmptyException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.winterwell.juice.Item;
import com.winterwell.utils.threads.ATask;
import com.winterwell.utils.threads.TaskRunner;

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

	private int maxPages = 1000;
	
	private int maxDepth = 5;

	final ConcurrentHashMap<String, DiNode<Item>> url2node = new ConcurrentHashMap();
//	
//	final ConcurrentLinkedQueue<String> urls = new ConcurrentLinkedQueue<String>();

	DiGraph<Item> web = new DiGraph<Item>();

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
	
	void reportLinks(String url, List<String> links, int depth) {
		// Update the web
		DiNode<Item> s = url2node.get(url);
		assert s != null : url;
		List<String> recurse = new ArrayList(links.size());
		for(String u : links) {
			if (url.equals(u)) {
				continue; // ignore self links
			}
			// Filter?
			if ( ! urlFilter.accept(u)) {
//				Log.d(LOGTAG, "	filter out "+u);
				continue; // ignore (and don't recurse on this)
			}		
			DiNode<Item> e = getCreateNode(u);
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
			// ignore if already done
			if (isDoneAlready(u)) {
				continue;
			}
			Spiderlet spiderlet = newSpiderlet(u, maxDepth+1);					
			runner.submitIfAbsent(spiderlet);
		}		
	}
	
	/**
	 * @param u
	 * @return true if u has already been spidered
	 */
	private boolean isDoneAlready(String u) {
		DiNode<Item> nu = getCreateNode(u);
		return nu.getValue()!=null && ( ! (nu.getValue() instanceof DummyItem));				
	}

	synchronized DiEdge getCreateEdge(DiNode<Item> s, DiNode<Item> e) {
		DiEdge edge = web.getEdge(s, e);
		if (edge!=null) return edge;
		edge = web.addEdge(s, e, null); //??store time of finding?
		return edge;
	}

	private synchronized DiNode<Item> getCreateNode(String u) {
		DiNode<Item> n = url2node.get(u);
		if (n != null) return n;
		n = web.addNode(new DummyItem(u));	
		url2node.put(u, n);
		return n;
	}

	IFilter<String> urlFilter;
	
	TaskRunner runner = new TaskRunner(10) {
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
		Spiderlet starter = newSpiderlet(startUrl, 0);
		DiNode<Item> root = getCreateNode(startUrl);
		runner.submit(starter);
		while(runner.getQueueSize() != 0) {
			Log.d(LOGTAG, "Queue: "+runner.getQueueSize());
			Utils.sleep(200);			
		}
		return web;
	}

	protected Spiderlet newSpiderlet(String url, int depth) {
		return new Spiderlet(this, url, depth);
	}

	/**
	 * @param url
	 * @param item Can be null
	 */
	void reportAnalysis(String url, Item item) {
		assert url != null;
		if (item==null) item = new DummyItem(url); // store a dummy
		DiNode<Item> n = url2node.get(url);
		assert n != null : url;
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
	boolean reportRedirect(String fromUrl, String toUrl) {
		assert ! fromUrl.equals(toUrl);
		assert toUrl != null : fromUrl;
		if (urlFilter!=null && ! urlFilter.accept(toUrl)) {
			return false;
		}
		// Update the web
		DiNode<Item> s = url2node.get(fromUrl);
		DiNode<Item> e = getCreateNode(toUrl);
		DiEdge edge = getCreateEdge(s,e);
		edge.setValue(REDIRECT);
		// Is toUrl known?
		return ! isDoneAlready(toUrl);
		
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

class DummyItem extends Item {

	public DummyItem(String url) {
		super(null, url);
	}
	
}

