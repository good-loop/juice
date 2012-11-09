package com.winterwell.juice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
	
	private Set<String> extractedTags = new HashSet<String>();
	
	@Override
	List<Anno> juice(JuiceMe doc) {		
		
		extractedTags.clear();
		
		Elements metaTags = doc.doc.getElementsByTag("meta");
		
		for (Element metaTag : metaTags) {
			// Check if it is metadata if Open Graph format
			String propertyVal = metaTag.attr("property");			
			if (!propertyVal.isEmpty()) {
				extractOG(doc, propertyVal, metaTag);
			} else {
				
				String nameValue = metaTag.attr("name");
				if (nameValue.equals("description")) {
					String descrValue = metaTag.attr("content");
					put(doc, AJuicer.DESC, descrValue);
				}				
			}
		}
		
		// If no URL was extracted from Open Graph metadata, extract
		// canonical URL
		List<Anno> urlAnnos = doc.type2annotation.get(AJuicer.URL);
		if (urlAnnos == null) {
		
			Elements canons = doc.doc.getElementsByAttributeValue("rel", "canonical");
			for (Element element : canons) {
				String urlValue = element.attr("href");
				put(doc, AJuicer.URL, urlValue);
				break;
			}
		}
		
		saveExtractedTags(doc);
		
		return added(doc);
	}

	// Extract Open Graph metadata from meta tag
	private void extractOG(JuiceMe doc, String propertyVal, Element metaTag) {
		String contentVal = metaTag.attr("content");
		Key key = propertyKeyMap.get(propertyVal);
		
		if (key != null) {
			saveValue(doc, key, contentVal);
		}
		
	}
	
	private SimpleDateFormat dataFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	private void saveValue(JuiceMe doc, Key key, String contentStr) {
		Object value = null;
		
		// We store all tags at once
		if (key == AJuicer.TAGS) {
			extractedTags.add(contentStr);
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
		} else {
			value = contentStr;
		}
		
		// If value was extracted store this value
		if (value != null) {
			put(doc, key, value);
		}
	}

	// Save all extracted tags
	private void saveExtractedTags(JuiceMe doc) {
		List<Anno> tagAnnotations = new ArrayList<Anno>();
		
		for (String tag : extractedTags) {
			Anno<String> anno = new Anno<String>(0, 0, AJuicer.TAGS, tag);
			anno.juicer = this;
			tagAnnotations.add(anno);
		}
		
		doc.type2annotation.addAll(AJuicer.TAGS, tagAnnotations);
		
	}

}
