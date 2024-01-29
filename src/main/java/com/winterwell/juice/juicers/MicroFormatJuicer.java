package com.winterwell.juice.juicers;

import org.jsoup.nodes.Element;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Anno;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.utils.Utils;
import com.winterwell.utils.time.Time;
import com.winterwell.utils.time.TimeUtils;

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
		Anno anno = anno(PUB_TIME, pt, time).setJuicer(this);
		item.put(anno);
		return true;
	}

}
