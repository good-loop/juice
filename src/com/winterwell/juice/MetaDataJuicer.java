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
	
	// Dan: It would be better to use the Item itself to hold tags -- to allow for multi-Item pages, and multi-threaded use
//	private Set<String> extractedTags = new HashSet<String>();
	
	
	@Override
	void juice(JuiceMe item) {		
		
		// Dan: It would be better to have this as a local variable (less room for bugs)
//		extractedTags.clear();
		
		// TODO what if there are multiple items within a page? 
		Elements metaTags = item.getDoc().getElementsByTag("meta");
		
		for (Element metaTag : metaTags) {
			// Check if it is metadata if Open Graph format
			String propertyVal = metaTag.attr("property");			
			if (!propertyVal.isEmpty()) {
				extractOG(item, propertyVal, metaTag);
			} else {
				
				String nameValue = metaTag.attr("name");
				if (nameValue.equals("description")) {
					String descrValue = metaTag.attr("content");
					item.put(anno(AJuicer.DESC, descrValue, metaTag));
				}				
			}
		}
		
		// If no URL was extracted from Open Graph metadata, extract
		// canonical URL
		Anno<String> urlAnno = item.getAnnotation(AJuicer.URL);
		if (urlAnno == null) {
		
			Elements canons = item.getDoc().getElementsByAttributeValue("rel", "canonical");
			for (Element element : canons) {
				String urlValue = element.attr("href");
				item.put(anno(AJuicer.URL, urlValue, element));
				break;
			}
		}
		
//		saveExtractedTags(item);
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
		} else {
			value = contentStr;
		}
		
		// If value was extracted store this value
		if (value != null) {
			item.put(new Anno(key, value, srcTag));
		}
	}

//	// Save all extracted tags
//	// TODO Instead, use item.
//	private void saveExtractedTags(Item item) {
//		List<Anno> tagAnnotations = new ArrayList<Anno>();
//		
//		for (String tag : extractedTags) {
//			Anno<String> anno = new Anno<String>(AJuicer.TAGS, tag);
//			tagAnnotations.add(anno);
//		}
//		
//		item.type2annotation.addAll(AJuicer.TAGS, tagAnnotations);
//		
//	}

}
