/**
 * 
 */
package com.winterwell.juice.juicers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Anno;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.utils.log.Log;
import com.winterwell.utils.time.TUnit;
import com.winterwell.utils.time.Time;
import com.winterwell.utils.time.TimeUtils;

/**
 * TODO Look for publication date in a document.
 * @author daniel
 * @tesedby DateFinderTest
 */
public class DateFinder extends AJuicer {

	/**
	 * Find dates within doc (ideally we'll get the main article's publication date).
	 */
	@Override
	public boolean juice(JuiceMe doc) {
		// Does it have a date?
		List<Item> items = doc.getExtractedItems();
		for (Item item : items) {
			if (item.get(PUB_TIME)!=null) continue;
			List<Anno> dates = findDates(item.getDoc());
			if (dates.isEmpty()) continue;
			// HACK: Pick the first & hope! 
			// If the first is in the future, try the next etc. We can't have future publication dates.
			Time now = new Time();
			Anno date = null;
			for (int i=0; i<dates.size(); i++) {
				date = dates.get(i);
				Time t = (Time) date.getValue();
				if (t.isAfter(now)) continue;
			}
			if (date != null) item.put(date);			
		}
		return false;
	}
	
	static final Pattern ENGLISH_DATE = Pattern.compile("(\\d\\d?)(st|th)? (jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\w* ?(\\d+)?", Pattern.CASE_INSENSITIVE);
	/**
	 * E.g. Published on May 18, 2013
	 */
	static final Pattern ENGLISH_DATE2 = Pattern.compile("(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\w* (\\d\\d?),? ?(19\\d{0,2}|20\\d{0,2}|21\\d{0,2})?", Pattern.CASE_INSENSITIVE);
	
	private static final String TAG = "DateFinder";
	
	/**
	 * Does NOT add the dates to an item
	 * 
	 * TODO time as well as date!
	 * @param doc
	 * @return
	 */
	public List<Anno> findDates(Element doc) {
		// Crude: Flatten the DOM & look for regex matches
		String text = doc.text();
		// Efficiency Hack: Cap how far we look
		if (text.length() > 10000) {
			text = text.substring(0,10000);
		}
		
		// TODO time!
		
		List<Anno> list = findDates2(text, ENGLISH_DATE);
		if ( ! list.isEmpty()) {
			return list;
		}
		list = findDates2(text, ENGLISH_DATE2);		
		return list;
	}

	private List<Anno> findDates2(String text, Pattern regex) {
		Matcher m = regex.matcher(text);
		List<Anno> list = new ArrayList();
		// Filter out future dates (allow for some clock drift)
		Time now = new Time().plus(5, TUnit.MINUTE);
		while (m.find()) {
			try {
				String ms = m.group();
				Time t = TimeUtils.parseExperimental(ms);
				if (t.isAfter(now)) {
					Log.d(TAG, "Skip future date "+t+" from "+ms+" in "+text);
					continue;
				}
				Anno anno = new Anno(AJuicer.PUB_TIME, t, null).setJuicer(this);
				anno.setText(ms); 
				list.add(anno);
			} catch(Exception ex) {
				// oh well
			}
		}
		return list;
	}
	

}
