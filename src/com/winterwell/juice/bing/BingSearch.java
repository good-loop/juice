package com.winterwell.juice.bing;

import java.util.List;
import java.util.Map;

import com.winterwell.utils.Dep;
import com.winterwell.utils.FailureException;
import com.winterwell.utils.containers.ArrayMap;
import com.winterwell.utils.web.SimpleJson;
import com.winterwell.utils.web.WebUtils2;
import com.winterwell.web.FakeBrowser;

/**
 * 
 * Config: https://portal.azure.com/#@danielgoodloop.onmicrosoft.com/resource/subscriptions/6772db6b-5fe9-4105-b60f-8f4b94349742/resourceGroups/GLAuto/providers/Microsoft.Bing/accounts/GLAutoBingSearch/overview
 * 
 * 
 * @testedby BingSearchTest
 * @author daniel
 *
 */
public class BingSearch {
	
	
    static String endpoint = "https://api.bing.microsoft.com/"+ "v7.0/search";
	private boolean debug;
	private BingConfig config;
    
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
 
	public BingSearch() {
		this(Dep.get(BingConfig.class));
	}
	
	public BingSearch(BingConfig bingConfig) {
		this.config = bingConfig;
	}

	/**
	 * 
	 * @param q
	 * @return {url, thumbnailUrl, snippet}
	 */
	public List<Map> search(String q) throws FailureException {
		FakeBrowser fb = new FakeBrowser();
		fb.setDebug(debug);
		fb.setRequestHeader("Ocp-Apim-Subscription-Key", config.subscriptionKey);
		String results = fb.getPage(endpoint, new ArrayMap("q", q));
		Object jobj = WebUtils2.parseJSON(results);
		List pages = SimpleJson.getList(jobj, "webPages", "value");
		if (pages==null) {
			throw new FailureException("No webPages?! "+results);
		}
		return pages;
	}
	
}
