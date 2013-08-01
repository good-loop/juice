package com.winterwell.juice.spider;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.winterwell.juice.Item;
import com.winterwell.juice.Juice;
import com.winterwell.juice.JuiceMe;

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
	
	ConcurrentHashMap<Item,Object> items = new ConcurrentHashMap();
	
	public Set<Item> getItems() {
		return items.keySet();
	}
	
	@Override
	void reportAnalysis(String url, Item item) {
		if (item != null) {
			items.put(item, "X");
		}
		
		if (buildWeb) {
			if (url!=null) {
				super.reportAnalysis(url, item);
			}
		}
	}
	
	boolean buildWeb;
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
			spider.reportAnalysis(null, item);
		}
		return null;
	}
	
}
