package com.winterwell.juice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import winterwell.utils.Key;
import winterwell.utils.reporting.Log;
import winterwell.utils.time.Time;
import winterwell.utils.web.WebUtils;
import winterwell.utils.web.WebUtils2;


/**
 * Juicer for extracting metadata in Facebook Open Graph (c.f. http://ogp.me/)
 * Or common <meta> tags. 
 * @author daniel
 *
 */
public class MetaDataJuicer extends AJuicer {

	static final String ANON = "anon";

	private static final String LOGTAG = "MetaDataJuicer";

	// Map from name of property to 
	private final Map<String, Key> propertyKeyMap = new HashMap<String, Key>() {{
		put("og:title", AJuicer.TITLE);
		put("og:url", AJuicer.URL);
		put("og:image", AJuicer.IMAGE_URL);
		put("og:type", AJuicer.MSG_TYPE);
		put("og:description", AJuicer.DESC);
		put("article:author", AJuicer.AUTHOR_XID);
		
		put("article:published_time", AJuicer.PUB_TIME);
					
		// Properties for document tags
		put("article:tag", AJuicer.TAGS);
		put("video:tag", AJuicer.TAGS);
		put("book:tag", AJuicer.TAGS);
	}};
	
	// Set of values for property 'og:type' related with video data
	private final Set<String> videoTypeNames = new HashSet<String>() {{
		add("video.movie");
		add("video.episode");
		add("video.tv_show");
		add("video.other");
	}};
	
	@Override
	public boolean juice(JuiceMe document) {		
		Item item = document.getMainItem();
		
		Elements metaTags = document.getDoc().getElementsByTag("meta");
		
		for (Element metaTag : metaTags) {
			// Check if it is metadata if Open Graph format
			String propertyVal = metaTag.attr("property");			
			if ( ! propertyVal.isEmpty()) {
				extractOG(item, propertyVal, metaTag, document);
				continue;
			}
			String nameValue = metaTag.attr("name");
			if (nameValue.equals("description")) {
				String descrValue = metaTag.attr("content");
				item.put(anno(AJuicer.DESC, descrValue, metaTag));
			}							
		}
		
		// If no URL was extracted from Open Graph metadata, extract
		// canonical URL
		Anno<String> urlAnno = item.getAnnotation(AJuicer.URL);
		if (urlAnno == null) {		
			Elements canons = document.getDoc().getElementsByAttributeValue("rel", "canonical");
			for (Element element : canons) {
				String urlValue = element.attr("href");
				// Bit of a hack, we can generate invalid URLs from here.
				urlValue.replace(" ", "%20");
				item.put(anno(AJuicer.URL, urlValue, element));
				break;
			}
		}
				
		juice2_author(document, item, metaTags);
		
		return false;
	}

	/**
	 * @param document
	 * @param item
	 * @param metaTags
	 */
	private void juice2_author(JuiceMe document, Item item, Elements metaTags) {
		String oxid = item.get(AUTHOR_XID);
		if (oxid != null) return;
				
		// Website domain
		String url = item.get(AJuicer.URL);
		String domain = null;
		if (url != null) {
			domain = WebUtils.getDomain(url);
		}
		if (domain==null) {
			String du = document.getURL();
			domain = WebUtils.getDomain(du);
		}
		if (domain==null) domain = "web";
	
		// Author tag?
		// e.g. <meta name=author content=eliphas>
		for (Element metaTag : metaTags) {
			if ("author".equals(metaTag.attr("name"))) {
				String author = metaTag.attr("content");
				if ( ! winterwell.utils.Utils.isBlank(author)) {
					String xid = author+"@"+domain;
					item.put(anno(AJuicer.AUTHOR_XID, xid, metaTag));		
					break;	
				}
			}
		}			
		
		// Fallback: Domain as author 
		item.putIfAbsent(anno(AJuicer.AUTHOR_XID, ANON+"@"+domain, null));
	    			
		// Icon
		Elements es = document.getDoc().getElementsByAttributeValue("rel", "shortcut icon");
		if ( ! es.isEmpty()) {
			String iconUrl = es.get(0).attr("href");
			item.putIfAbsent(anno(AJuicer.AUTHOR_IMG, iconUrl, es.get(0)));
		}	
	}	

	/** Extract Open Graph metadata from meta tag 
	 * @param document */
	private void extractOG(Item doc, String propertyVal, Element metaTag, JuiceMe document) {
		String contentVal = metaTag.attr("content");
		Key key = propertyKeyMap.get(propertyVal);		
		if (key != null) {
			extractOG2_saveValue(doc, key, contentVal, metaTag, document);
		}		
	}
	
	private SimpleDateFormat dataFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	/**
	 * @param document 
	 * 
	 */
	private void extractOG2_saveValue(Item item, Key key, String contentStr, Element srcTag, JuiceMe document) {
		assert item != null && key != null : contentStr;
		assert contentStr!=null : item+" "+key;
		Object value = null;
		
		// We store all tags at once
		if (key == AJuicer.TAGS) {
			Anno<List<String>> tags = item.getAnnotation(AJuicer.TAGS);
			if (tags==null) {
				// Dan: Here is one place where a list of Annos would allow more flexibility
				// -- the flexibility to record the source behind each tag. But we won't need that now, perhaps never. 
				tags = anno(AJuicer.TAGS, new ArrayList(), null);
				item.type2annotation.put(AJuicer.TAGS, tags);
			}
			tags.value.add(contentStr);
			return;
		}
		
		if (key == AJuicer.PUB_TIME) {
			Date date;
			try {
				date = dataFormater.parse(contentStr);
				value = new Time(date);
			} catch (ParseException e) {
				// Simply ignore the exception. Value will be equal to null no annotations
				// will be stored
			}
			
			
		} else if (key == AJuicer.MSG_TYPE) {
			if (videoTypeNames.contains(contentStr)) {
				value = KMsgType.VIDEO;
			} else {
				value = KMsgType.MISC;
			} 
		} else if (key == AJuicer.AUTHOR_XID) {
			value = contentStr += "@web";
		} else if (key == AJuicer.URL || key==AJuicer.AUTHOR_URL) {
			// Bug #2997: Some muppet put an a tag into their metadata instead of a url
			if (contentStr.startsWith("<a")) {				
				Matcher m = WebUtils2.pHref.matcher(contentStr);
				boolean ok = m.find();
				if (ok) {
					contentStr = m.group(1);
				} else {
					Log.w(LOGTAG, "Bogus url: "+contentStr);
					return;
				}
			}
			// Resolve relative urls
			if ( ! contentStr.startsWith("http") && document!=null && document.getURL()!=null) {
				String base = document.getURL();
				// Resolve the URI with the base url to get an absolute one
				try {
					contentStr = WebUtils.resolveUri(base, contentStr).toString();
				} catch(Exception ex) {
					Log.w(LOGTAG, "Bogus url: "+contentStr+" in doc "+base+": "+ex);
					return;
				}
			}
			// OK
			value = contentStr;
		} else {
			value = contentStr;
		}
		
		// If value was extracted store this value
		if (value != null) {
			item.put(anno(key, value, srcTag));
		}
	}

}
