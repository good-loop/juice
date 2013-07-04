/**
 * 
 */
package com.winterwell.juice;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

import winterwell.utils.TodoException;
import winterwell.utils.time.Time;
import winterwell.utils.time.TimeUtils;

/**
 * TODO Look for publication date in a document.
 * @author daniel
 *
 */
public class DateFinder extends AJuicer {

	/**
	 * Find dates within doc (ideally we'll get the main article's publication date).
	 */
	@Override
	public boolean juice(JuiceMe doc) {
		throw new TodoException();		
	}
	
	static final Pattern ENGLISH_DATE = Pattern.compile("(\\d\\d?)(st|th)? (jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\w* ?(\\d+)?", Pattern.CASE_INSENSITIVE);	
	
	public List<Anno> findDates(Element doc) {
		// Crude: Flatten the DOM & look for regex matches
		String text = doc.text();
		// Efficiency Hack: Cap how far we look
		if (text.length() > 10000) {
			text = text.substring(0,10000);
		}
		Matcher m = ENGLISH_DATE.matcher(text);
		List<Anno> list = new ArrayList();
		while (m.find()) {
			try {
				Time t = TimeUtils.parseExperimental(m.group());
				list.add(new Anno(AJuicer.PUB_TIME, t, null));
			} catch(Exception ex) {
				// oh well
			}
		}
		return list;
	}
	

}
