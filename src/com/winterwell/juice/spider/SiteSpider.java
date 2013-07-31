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
import winterwell.utils.Printer;
import winterwell.utils.Utils;
import winterwell.utils.reporting.Log;
import winterwell.utils.time.Time;
import winterwell.utils.web.WebUtils;

/**
 * Spider a single website. Holds everything in memory!
 * 
 * @author Daniel
 * @testedby {@link SiteSpiderTest}
 */
public class SiteSpider extends ATask<DiGraph<Item>> {

	public void setUrlFilter(IFilter<String> urlFilter) {
		this.urlFilter = urlFilter;
	}
	
	public IFilter<String> getUrlFilter() {
		return urlFilter;
	}
	
	private static final String LOGTAG = "spider";

	private int maxDepth = 5;

	final ConcurrentHashMap<String, DiNode<Item>> url2node = new ConcurrentHashMap();
//	
//	final ConcurrentLinkedQueue<String> urls = new ConcurrentLinkedQueue<String>();

	DiGraph<Item> web = new DiGraph<Item>();

	private String startUrl;

	
	public SiteSpider(String startUrl) {
		this.startUrl = startUrl;
		 urlFilter = new InSiteFilter(startUrl);
	}
	
	void reportLinks(String url, List<String> links, int depth) {
		// Update the web
		DiNode<Item> s = url2node.get(url);
		assert s != null : url;
		for(String u : links) {
			if (url.equals(u)) {
				continue; // ignore self links
			}
			// Filter?
			if ( ! urlFilter.accept(u)) {
				continue; // ignore
			}		
			DiNode<Item> e = getCreateNode(u);
			getCreateEdge(s,e);
			// Recurse...
			if (depth < maxDepth) {
				DiNode<Item> nu = url2node.get(u);
				// ignore if already doing
				if (nu.getValue()==null || nu.getValue() instanceof DummyItem) {				
					Spiderlet spiderlet = new Spiderlet(this, u, maxDepth+1);					
					runner.submitIfAbsent(spiderlet);
				}
			}
		}
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
	
	TaskRunner runner = new TaskRunner(10);

	@Override
	public DiGraph<Item> run() {
		Spiderlet starter = new Spiderlet(this, startUrl, 0);
		DiNode<Item> root = getCreateNode(startUrl);
		runner.submit(starter);
		while(runner.getQueueSize() != 0) {
			Log.d(LOGTAG, "Queue: "+runner.getQueueSize());
			Utils.sleep(200);			
		}
		return web;
	}

	void reportAnalysis(String url, Item item) {
		DiNode<Item> n = url2node.get(url);
		assert n != null : url;
		n.setValue(item);
	}

	public String getStartUrl() {
		return startUrl;
	}
	
	
}

class InSiteFilter implements IFilter<String> {

	private String domain;
	private Pattern domainP;

	public InSiteFilter(String startUrl) {
		this.domain = WebUtils.getDomain(startUrl);
		domainP = Pattern.compile("^https?://\\w*"+Pattern.quote(domain));
	}

	@Override
	public boolean accept(String url) {
		return domainP.matcher(url).find();
	}
	
}

class DummyItem extends Item {

	public DummyItem(String url) {
		super(null, url);
	}
	
}

