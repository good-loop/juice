package com.winterwell.juice;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.winterwell.utils.Utils;
import com.winterwell.utils.time.Time;
import com.winterwell.utils.time.TimeUtils;
import com.winterwell.utils.web.WebUtils;
import com.winterwell.utils.web.WebUtils2;

/**
 * e.g. 	 * https://www.creativebrief.com/bite/amy-williams-founder-good-loop
		 * datetime

 * @author daniel
 * @testedby MicroFormatJuicerTest
 */
public class MicroFormatJuicer extends AJuicer {

	@Override
	protected boolean juice(JuiceMe doc) {
		Item item = doc.getMainItem();		
		// datetime?
		if ( ! item.containsKey(PUB_TIME)) {
			doDateTime(doc, item);
		}
		return false;
	}

	boolean doDateTime(JuiceMe doc, Item item) {		
		Element time = doc.getDoc().selectFirst("time");
		if (time == null) {
			time = doc.getDoc().selectFirst("[datetime]");
		}
		if (time==null) return false;			
		String ts = time.attr("datetime");
		if (Utils.isBlank(ts)) ts = time.text();
		if (Utils.isBlank(ts)) return false;
		Time pt = TimeUtils.parseExperimental(ts);
		Anno anno = anno(PUB_TIME, pt, time);
		item.put(anno);
		return true;
	}

}
