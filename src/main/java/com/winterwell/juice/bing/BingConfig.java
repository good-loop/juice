package com.winterwell.juice.bing;

import com.winterwell.utils.StrUtils;
import com.winterwell.utils.io.Option;
import com.winterwell.web.data.XId;

public class BingConfig {
	
	@Option
	String subscriptionKey;
	
	/**
	 * 
	 * @return hash of the subscription key, or null
	 */
	public XId getLogin() {
		if (subscriptionKey==null) return null;
		return new XId(StrUtils.md5(subscriptionKey), "bing");
	}
}
