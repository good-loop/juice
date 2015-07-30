package com.winterwell.juice.juicers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import winterwell.utils.Key;
import winterwell.utils.reporting.Log;
import winterwell.utils.web.WebUtils;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Anno;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.utils.web.WebUtils2;

import creole.data.XId;

/**
 * Special case support for LinkedIn profiles
 * @author daniel
 *
 */
public class LinkedInJuicer extends AJuicer {

	public static final Key<String> AUTHOR_INDUSTRY = new Key("author.industry");

	@Override
	protected boolean juice(JuiceMe doc) {
//		<link rel="canonical" href="https://uk.linkedin.com/in/grusev"> already done??
		// Is it a profile page?
		Elements elements = doc.getDoc().select(".full-name");
//		<meta property="og:image" content="http://m.c.lnkd.licdn.com/mpr/mpr/shrink_200_200/p/1/000/03c/21f/3f22b7b.jpg">
		if (elements.size()==0) return false;
		if (elements.size() > 1) {			
			return false;
		}		
		Item item = doc.getMainItem();
		String aName = elements.get(0).text();
		item.put(anno(AUTHOR_NAME, aName, elements.get(0)));
		String url = item.getUrl();
		item.put(AUTHOR_URL, url);
		// Use the url as the ID, because we cant get useful cross-app IDs from LinkedIn
		// Let the LinkedInPlugin do any extra canonicalisation we want
		String url_wo_protocol = url.replaceFirst("https?://", "");
		item.put(AUTHOR_XID, XId.WART_P+url_wo_protocol+"@linkedin");		
		// More info
		String headline = findAnno(doc, item, AUTHOR_JOB, "#headline");
		Elements hs = doc.getDoc().select(".member-connections");
		if ( ! hs.isEmpty()) {
			String text = hs.get(0).text();
			Matcher m = Pattern.compile("\\d+").matcher(text);
			if (m.find()) {
				String cnt = m.group();
				item.put(anno(AUTHOR_FAN_COUNT, Integer.valueOf(cnt), hs.get(0)));
			}
		}
//		TODO Elements elements = doc.getDoc().select("#overview-recommendation-count");
		String locn = findAnno(doc, item, AUTHOR_LOCN, ".locality");
		String ind = findAnno(doc, item, AUTHOR_INDUSTRY, ".industry");
		String desc = findAnno(doc, item, AUTHOR_DESC, "p.description");
		return true;
	}


}
