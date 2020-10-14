package com.winterwell.juice;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

import org.jsoup.nodes.Element;
import org.junit.Test;

import com.winterwell.utils.io.FileUtils;
import com.winterwell.utils.time.Time;

public class DateFinderTest {

	@Test
	public void testABitOff() {
		 String text = "<html><p>&raquo; Mon Aug 19, 2013 8:55 am </p></html>";
		 JuiceMe jm = new JuiceMe(text);
		 DateFinder df = new DateFinder();
		 List<Anno> dates = df.findDates(jm.getDoc());
		 System.out.println(dates);
		 assert dates.size() == 1;
		 Time date = (Time) dates.get(0).value;
		 
		 Time expected = new Time("Aug 19, 2013");
		 assert date.equals(expected) : date + " vs " + expected;
	}

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
	

	@Test
	public void testDateOct2020() {
		String url = "https://dailybusinessgroup.co.uk/2020/10/engineering-and-software-winners-announced/";
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
		assert items0.get(0).getPublishedTime().ddMMyyyy().equals("02/10/2020") : items0.get(0).getPublishedTime();
	}
	
	
	

}
