package com.winterwell.juice.spider;

import java.util.Collection;
import java.util.HashSet;
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

	Juice juicer;

	public void setJuicer(Juice juicer) {
		this.juicer = juicer;
	}
	
	public JuicingSiteSpider(String startUrl) {
		super(startUrl);
		juicer = new Juice();
	}

	@Override
	protected Spiderlet newSpiderlet(String url, int depth) {	
		return new JuiceSpiderlet(this, url, depth);
	}
	
//	ConcurrentHashMap<Item,Object> items = new ConcurrentHashMap();
	
	public Set<Item> getItems() {
		Collection<DiNode<Item>> nodes = web.getNodes();
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
//		if (item != null) {
//			items.put(item, "X");
//		}
//		
//		if (buildWeb) {
		super.reportAnalysis(xid, item);			
//		}
	}
	
//	boolean buildWeb;
}

class JuiceSpiderlet extends Spiderlet {

	private Juice juicer;

	public JuiceSpiderlet(JuicingSiteSpider juicingSiteSpider, String url, int depth) 
	{
		super(juicingSiteSpider, url, depth);
		this.juicer = juicingSiteSpider.juicer;
	}
	
	@Override
	protected Item analyse(String html) {
		JuiceMe juiced = juicer.juice(url, html);
		for(Item item : juiced.getExtractedItems()) {
			spider.reportAnalysis(item.getXId2(), item);
		}
		return null;
	}
	
}
