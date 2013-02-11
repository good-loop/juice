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

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import winterwell.utils.Key;
import winterwell.utils.time.Time;


/**
 * Juicer for extracting metadata in Facebook Open Graph (c.f. http://ogp.me/)
 * Or common <meta> tags. 
 * @author daniel
 *
 */
public class MetaDataJuicer extends AJuicer {

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
	boolean juice(JuiceMe document) {		
		Item item = document.getMainItem();
		
		Elements metaTags = document.getDoc().getElementsByTag("meta");
		
		for (Element metaTag : metaTags) {
			// Check if it is metadata if Open Graph format
			String propertyVal = metaTag.attr("property");			
			if ( ! propertyVal.isEmpty()) {
				extractOG(item, propertyVal, metaTag);
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
				item.put(anno(AJuicer.URL, urlValue, element));
				break;
			}
		}
		
		return false;
	}	

	/** Extract Open Graph metadata from meta tag */
	private void extractOG(Item doc, String propertyVal, Element metaTag) {
		String contentVal = metaTag.attr("content");
		Key key = propertyKeyMap.get(propertyVal);		
		if (key != null) {
			saveValue(doc, key, contentVal, metaTag);
		}		
	}
	
	private SimpleDateFormat dataFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private void saveValue(Item item, Key key, String contentStr, Element srcTag) {
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
		} else {
			value = contentStr;
		}
		
		// If value was extracted store this value
		if (value != null) {
			item.put(anno(key, value, srcTag));
		}
	}

}
