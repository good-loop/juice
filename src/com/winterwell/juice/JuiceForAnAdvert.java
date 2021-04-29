package com.winterwell.juice;

import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * TODO patch the gaps left by other juicers for:
 * 
 * Name
 * Tagline / slogan
 * Logo
 * Top photos
 * Brand colours
 * Fonts
 * CTA link
 * CTA "action words"
 * 
 * @author daniel
 *
 */
public class JuiceForAnAdvert extends AJuicer {

	@Override
	protected boolean juice(JuiceMe doc) {
		
		Item item = doc.getMainItem();
		
		if (item.get(AJuicer.PUBLISHER_NAME)==null && item.get(AJuicer.TITLE)!=null) {
			// use the title instead
			Anno<String> title = item.getAnnotation(AJuicer.TITLE);
			Anno<String> anno = new Anno<>(AJuicer.PUBLISHER_NAME, title.value, title.src);
			item.put(anno);
		}
		
		return false;
	}

}
