package com.winterwell.juice.juicers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import winterwell.utils.Key;
import winterwell.utils.Utils;
import winterwell.utils.reporting.Log;
import winterwell.utils.web.WebUtils;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Anno;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.MetaDataJuicer;
import com.winterwell.utils.web.WebUtils2;

import creole.data.XId;

/**
 * Special case support for LinkedIn profiles. Assumes: {@link MetaDataJuicer} has already run for canonical url and image.
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
		item.put(AUTHOR_XID, xid(url));
		// image -- from og:image on the page (assuming: MetaDataJuicer has already run!)
		String imgUrl = item.get(IMAGE_URL);
		item.put(AUTHOR_IMG, imgUrl);		
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
		String currentSummary = findAnno(doc, item, new Key("current-overview"), "#overview-summary-current");		
		// Increase the description with extra info
		
		// Current roles??
		Elements cRoles = doc.getDoc().select(".current-position");
		String currentRoles = "";
		for (Element crole : cRoles) {
			// <a href="https://www.linkedin.com/title/member-of-the-board-of-trustees?trk=pprofile_title" title="Learn more about this title">Member of the Board of Trustees</a>
			currentRoles += " - "+crole.text()+"\n"; 
		}
		// Set a loonngg description
		StringBuilder longDesc = new StringBuilder();
		if (desc!=null) {longDesc.append(desc); longDesc.append("\n");}
		if (ind!=null) longDesc.append("Industry: "+ind+"\n");
		if (currentSummary!=null) {
			if (currentSummary.startsWith("Current")) currentSummary = currentSummary.substring(7);
			longDesc.append("Current (summary): "+currentSummary+"\n");
		}
		if ( ! currentRoles.isEmpty()) longDesc.append("\n"+currentRoles);
		item.put(anno(AUTHOR_DESC, longDesc.toString(), null));
		
		return true;
	}

	public static String xid(String url) {
		// Use the url as the ID, because we cant get useful cross-app IDs from LinkedIn
		// Let the LinkedInPlugin do any extra canonicalisation we want
		String url_wo_protocol = url.replaceFirst("https?://", "");
		return XId.WART_P+url_wo_protocol+"@linkedin";
	}


}
