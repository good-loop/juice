package com.winterwell.juice;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

import org.jsoup.nodes.Element;
import org.junit.Test;

import winterwell.utils.StrUtils;
import winterwell.utils.io.FileUtils;

public class DateFinderTest {


	@Test
	public void testRegex() {
		Matcher m = DateFinder.ENGLISH_DATE2.matcher("E.g. Published on May 18, 2013");
		assert m.find();
	}
	
	@Test
	public void testDate() {
		String url = "http://www.worldsultimate.net/plan-a-royal-vacation-to-edinburgh-scotland.htm";
		File file = TestUtils.getTestFile("misc", url);
		Juice j = new Juice();
		String html = FileUtils.read(file);
		
		DateFinder df = new DateFinder();
		JuiceMe doc = new JuiceMe(url, html);
		
		Element e = doc.getMainItem().getDoc();
		List<Anno> dates = df.findDates(e);
		assert ! dates.isEmpty();
		
		df.juice(doc);
		List<Item> items0 = doc.getExtractedItems();
		System.out.println(items0);
		assert items0.get(0).getPublishedTime() != null;		
	}

}
