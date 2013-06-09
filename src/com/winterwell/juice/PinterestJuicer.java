package com.winterwell.juice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import winterwell.utils.Utils;

/**
 * Leaves most of the work to the {@link MetaDataJuicer}
 * @testedby {@link PinterestJuicerTest}
 * @author daniel
 *
 */
public class PinterestJuicer extends AJuicer {

	MetaDataJuicer mdj = new MetaDataJuicer();
	
	@Override
	public boolean juice(JuiceMe doc) {
		if (doc.url==null || ! doc.url.contains("pinterest.com/pin/")) {
			return false;
		}
		
		// It's a pin!
//		 <meta property="pinterestapp:pinboard" content="http://pinterest.com/skyscanner/travel-inspiration/"/>
//		    <meta property="pinterestapp:pinner" content="http://pinterest.com/skyscanner/"/>
//		    <meta property="pinterestapp:source" content="http://www.skyscanner.net/news/10-worlds-craziest-carnivals"/>
//		    <meta property="pinterestapp:likes" content="0"/>
//		    <meta property="pinterestapp:repins" content="5"/>
//		    <meta property="pinterestapp:comments" content="0"/>
//		    <meta property="pinterestapp:actions" content="5"/>
		Elements metaTags = doc.getDoc().getElementsByTag("meta");
		Item item = doc.getMainItem();
		for (Element metaTag : metaTags) {
			String propertyVal = metaTag.attr("property");			
			if (propertyVal.startsWith("pinterestapp:pinner")) {
				String pinner = metaTag.attr("content");
				if (pinner==null || pinner.isEmpty()) continue;
				Pattern p = Pattern.compile("https?://pinterest.com/(\\w+)/?");
				Matcher m = p.matcher(pinner);
				if (m.matches()) pinner = m.group(1);
				item.put(anno(AUTHOR_XID, pinner+"@pinterest", metaTag));
				continue;
			}
			if (propertyVal.startsWith("pinterestapp:source")) {
				String src = metaTag.attr("content");
				if (src==null || src.isEmpty()) continue;
				item.put(anno(LINK, src, metaTag)); // TODO ?? Where to store the source link?
				continue;
			}
			if (propertyVal.startsWith("pinterestapp:pinboard")) {
				
			}
			if (propertyVal.startsWith("pinterestapp:likes")) {
				
			}
			if (propertyVal.startsWith("pinterestapp:repins")) {
				
			}
		}
		
		// Do the rest with MetaDataJuicer		
		mdj.juice(doc);
		
		// But alter the title
		String title = item.getTitle();
		String desc = item.get(DESC);
		if (title!=null && desc != null && desc.length() < 140) {
			title = title+": "+desc;
			item.put(anno(TITLE, title, null));
		}
		
		// XID = pin number if we can
		String _url = Utils.or(item.get(AJuicer.URL), doc.url);
		if (_url!=null) {
			Matcher m = PIN_NUMBER.matcher(_url);
			if (m.find()) {
				String pin = m.group(1);
				item.put(anno(AJuicer.XID, pin+"@pinterest", null));
			}
		}
		
		return true;
	}
	
	static Pattern PIN_NUMBER = Pattern.compile("pin/(\\d+)");
	
	

}
