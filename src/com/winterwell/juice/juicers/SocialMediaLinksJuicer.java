package com.winterwell.juice.juicers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Anno;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.utils.Key;
import com.winterwell.utils.containers.ArrayMap;
import com.winterwell.utils.containers.ListMap;

public class SocialMediaLinksJuicer extends AJuicer {
	
	final static Pattern SOCIAL_LINK = Pattern.compile("https://(www.)?(twitter|facebook|instagram|pinterest|youtube).com/([a-zA-Z0-9_]+)"); 
	
	@Override
	protected boolean juice(JuiceMe doc) {
		String html = doc.getHTML();
		Matcher m = SOCIAL_LINK.matcher(html);
		ListMap<String,String> link4network = new ListMap();
		while(m.find()) {
			String network = m.group(2);
			String profile = m.group(3);		
			link4network.addOnce(network, profile);
		}
		Item item = doc.getMainItem();
		for(Map.Entry<String, List<String>> me : link4network.entrySet()) {
			if (me.getValue().size() > 1) {
				// ambiguous
				continue;
			}					
			Key<String> key = new Key(me.getKey());
			Anno<String> anno1 = new Anno<>(key, me.getValue().get(0), null);
			item.put(anno1);			
		}
		return false;
	}

}
