package com.winterwell.juice.juicers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import winterwell.utils.Key;
import winterwell.utils.MathUtils;
import winterwell.utils.Utils;
import winterwell.utils.reporting.Log;
import winterwell.utils.time.Time;
import winterwell.utils.web.WebUtils2;

import com.sodash.jlinkedin.model.LIGroup;
import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Anno;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.MetaDataJuicer;

import creole.data.XId;

/**
 * Special case support for LinkedIn profiles. Assumes: {@link MetaDataJuicer} has already run for canonical url and image.
 * @testedby {@link LinkedInJuicerTest}
 * @author daniel
 */
public class LinkedInJuicer extends AJuicer {

	public static final Key<String> AUTHOR_INDUSTRY = new Key("author.industry");

	@Override
	protected boolean juice(JuiceMe doc) {
		boolean ok = juice2_entity(doc);
		// Do we have some feed posts?
		Elements feedItems = doc.getDoc().select(".feed-item");
		if (feedItems.size()!=0) {
			for(Element e : feedItems) {
				doJuiceFeedItem(doc, e);
			}
		}
		return ok;
	}
	

	private void doJuiceFeedItem(JuiceMe doc, Element e) {
		Item item = new Item(e, doc.getURL());
		// ID
		String id = e.attr("data-li-update-id");
		if ( ! Utils.isBlank(id)) {
			item.put(this.XID, "update-"+id+"@linkedin");
		}
		// Time
		String pub = e.attr("data-li-update-date");
		if ( ! Utils.isBlank(pub)) {
			Time pt = new Time(pub);
			item.put(this.PUB_TIME, pt);
		}
		// Link
		Elements link = e.select("a.nus-timestamp");
		if ( ! link.isEmpty()) {
			String xurl = link.get(0).attr("href");
			if ( ! Utils.isBlank(xurl)) {
				Anno<String> anno = anno(URL, xurl, link.get(0));
				item.put(anno);
			}
		}		
		// the post text!
		String fnd = findAnno(e, item, AJuicer.POST_BODY, ".commentary");
		doc.addItem(item);
	}


	private boolean juice2_entity(JuiceMe doc) {
//		<link rel="canonical" href="https://uk.linkedin.com/in/grusev"> already done??
		// Is it a profile page?
		Elements elements = doc.getDoc().select(".full-name");
//		<meta property="og:image" content="http://m.c.lnkd.licdn.com/mpr/mpr/shrink_200_200/p/1/000/03c/21f/3f22b7b.jpg">
		if (elements.size()==0) {
			// Is it a post?
			if (doc.getURL()!=null && doc.getURL().contains("/post/")) {
				return doJuicePost(doc);
			}
			// Is it a group?
			if ((doc.getURL()!=null && doc.getURL().contains("/grp/"))) {
				String gid = WebUtils2.getQueryParameter(doc.getURL(), "gid");
				LIGroup lig = new LIGroup(gid, doc.getHTML());
				anno(AUTHOR_NAME, lig.getName(), null);
				anno(AUTHOR_DESC, lig.getDescription(), null);
				anno(AUTHOR_URL, lig.getPublicUrl(), null);
				anno(AUTHOR_XID,  XId.WART_G+lig.getId()+"@linkedin", null);
				anno(AUTHOR_IMG, lig.getSmallLogoUrl(), null);
				return true;
			}
			// Is it a company page?
			if ((doc.getURL()!=null && doc.getURL().contains("/company/"))) {
				boolean ok = doJuiceCompany(doc);
				return ok;
			}
			// something else TODO investigate
			// TODO handle redirects, e.g. https://www.linkedin.com/groups/School-Social-Political-Science-University-4307735
			Log.d(LOGTAG, doc.getURL()+" unrecognised page type ");
			return false;
		}
		if (elements.size() > 1) {			
			return false;
		}		
		return doJuiceProfile(doc);

	}


	private boolean doJuiceProfile(JuiceMe doc) {
		Item item = doc.getMainItem();
		Elements elements = doc.getDoc().select(".full-name");
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
		
		// Current roles?? Note: the current job (but not employer) is set above via the headline
		Elements cRoles = doc.getDoc().select(".current-position");
		String currentRoles = "";
		for (Element crole : cRoles) {
			// <a href="https://www.linkedin.com/title/member-of-the-board-of-trustees?trk=pprofile_title" title="Learn more about this title">Member of the Board of Trustees</a>
			currentRoles += " - "+crole.text()+"\n"; 
		}
		// Set a loonngg description
		StringBuilder longDesc = new StringBuilder(headline==null?"" : headline+"\n");
		if (desc!=null) {longDesc.append(desc); longDesc.append("\n");}
		if (currentSummary!=null) {
			if (currentSummary.startsWith("Current")) currentSummary = currentSummary.substring(7);
			longDesc.append("Current organisation: "+currentSummary+"\n");
		}
		if (ind!=null) longDesc.append("Industry: "+ind+"\n");		
		if ( ! currentRoles.isEmpty()) longDesc.append("\n"+currentRoles);
		item.put(anno(AUTHOR_DESC, longDesc.toString(), null));
		
		return true;
	}
	

	private boolean doJuiceCompany(JuiceMe doc) {
		Item item = doc.getMainItem();
		// probably already juiced by MetaDataJuicer
		String self = XId.WART_C+item.getTitle()+"@linkedin";
		Anno<String> anno = anno(AJuicer.AUTHOR_XID, self, null);
		item.put(anno);
		
		Elements followers = doc.getDoc().select(".followers-count");
		if ( ! followers.isEmpty()) {
			try {
				String fs = followers.get(0).text();
				Pattern nwc = Pattern.compile("[0-9,]+");
				Matcher m = nwc.matcher(fs);
				if (m.find()) {
					Integer cnt = Integer.valueOf(m.group().replace(",", ""));
					anno(AUTHOR_FAN_COUNT, cnt, followers.get(0));
				}
			} catch(Throwable ex) {
				Log.e(LOGTAG, ex);
			}
		}
		
		return true;
	}

	private boolean doJuicePost(JuiceMe doc) {
		// eg https://www.linkedin.com/grp/post/7445683-5898412079244681220
		Item item = doc.getMainItem();
		Elements atitle = doc.getDoc().select(".header-body .title");
		if (atitle.size()==1) {
			Element atitle0 = atitle.get(0);
			String name = atitle0.text();
			String url = atitle0.attr("href");
			// get an id from that?? Or stay with url??
			if ( ! Utils.isBlank(name)) anno(AJuicer.AUTHOR_NAME, name, atitle0);
			if ( ! Utils.isBlank(url)) {
				anno(AJuicer.AUTHOR_URL, url, atitle0);
			}
			item.put(AUTHOR_XID, xid(url));									
		}		
		
		Elements imgs = doc.getDoc().select(".post-header .header-image img");
		if (imgs.size()==1) {
			Element img = imgs.get(0);
			String isrc = img.attr("src");
			if ( ! Utils.isBlank(isrc)) item.put(AUTHOR_IMG, isrc);
		}
//.post-header .header-image
//comment-wrapper
//header-body title author name
//subtile - their job
//post-title the title
//post-date the date
//
//entity-name
//entity-image
//	comment-body
//	comment-date
//	
//	comment-content
		return true;
	}

	public static String xid(String url) {
		// Use the url as the ID, because we cant get useful cross-app IDs from LinkedIn
		
		// we do get IDs, eg "memberID":7715970 -- and you can make a url
		// https://www.linkedin.com/profile/view?id=7715970 -- shall we use those?? 
		url = WebUtils2.removeQueryParameter(url, "trk");
		
		// Let the LinkedInPlugin do any extra canonicalisation we want
		String url_wo_protocol = url.replaceFirst("https?://", "");
		return XId.WART_P+url_wo_protocol+"@linkedin";
	}


}
