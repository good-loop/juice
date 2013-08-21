package com.winterwell.juice.spider;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import winterwell.maths.graph.DiNode;

import com.winterwell.juice.Item;
import com.winterwell.juice.Juice;
import com.winterwell.juice.JuiceMe;

import creole.data.XId;

/**
 * Spider a site, piping the pages through Juice.
 * 
 * @testedby {@link JuicingSiteSpiderTest}
 * @author daniel
 *
 */
public class JuicingSiteSpider extends SiteSpider {

	transient Juice juicer;
	
	public void setOnlyFollowStubs(boolean onlyFollowStubs) {
		this.onlyFollowStubs = onlyFollowStubs;
	}
	
	/**
	 * If true, only follow urls from stub Items.
	 * Use-case: start with a search page, and follow the results.
	 * 
	 * TODO true seems to be buggy!
	 */
	boolean onlyFollowStubs;
	
	public JuicingSiteSpider(String startUrl) {
		super(startUrl);		
	}

	@Override
	protected Spiderlet newSpiderlet(String url, int depth) {	
		if (juicer==null) juicer = new Juice();
		return new JuiceSpiderlet(this, url, depth);
	}
	
//	ConcurrentHashMap<Item,Object> items = new ConcurrentHashMap();
	
	public Set<Item> getItems() {
		Collection<DiNode<Item>> nodes = _web.getNodes();
		Set<Item> items = new HashSet();
		for (DiNode<Item> diNode : nodes) {
			if (diNode.getValue() instanceof DummyItem) {
				continue;
			}
			items.add(diNode.getValue());
		}
		return items;
	}
	
	@Override
	void reportAnalysis(XId xid, Item item) {
		super.reportAnalysis(xid, item);			
	}
	
}

class JuiceSpiderlet extends Spiderlet {


	public JuiceSpiderlet(JuicingSiteSpider juicingSiteSpider, String url, int depth) 
	{
		super(juicingSiteSpider, url, depth);
	}
	
	
	@Override
	protected void extractLinks(Item item, List<String> links) {
		if (((JuicingSiteSpider)spider).onlyFollowStubs) {
			if (item.isStub()) {
				links.add(item.getUrl());
			}
		} else {
			super.extractLinks(item, links);
		}
	}
	
	@Override
	protected void extractLinks(String html, List<String> links) {
		if (((JuicingSiteSpider)spider).onlyFollowStubs) {
			return; // The Item based method gets them!
		} else {
			super.extractLinks(html, links);
		}
	}
	
	@Override
	protected List<Item> analyse(String html) {
		JuiceMe juiced = ((JuicingSiteSpider)spider).juicer.juice(url, html);
		List<Item> items = juiced.getExtractedItems();
		return items;
	}
	
	
	
}
