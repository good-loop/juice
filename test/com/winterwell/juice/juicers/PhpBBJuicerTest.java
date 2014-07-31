package com.winterwell.juice.juicers;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.TestUtils;
import com.winterwell.juice.juicers.PhpBBJuicer;
import com.winterwell.juice.spider.JuicingSiteSpider;
import winterwell.utils.io.FileUtils;

import winterwell.utils.Printer;
import winterwell.utils.StrUtils;
import creole.data.IDoCanonical;
import creole.data.XId;

public class PhpBBJuicerTest {

	@Test
	public void test220Triathlon() {
		File file = TestUtils.getTestFile("phpbb", "http://www.220triathlon.com/forum/gatorade-powder-t48506.html");
		String html = FileUtils.read(file);
		PhpBBJuicer juicer = new PhpBBJuicer();
		JuiceMe doc = new JuiceMe("http://bikeradar.com/forums/", html);
		
		boolean hm = juicer.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		System.out.println(items);
		assert items.size() > 1;
	}
	
	@Test
	public void testJuiceBikeRadarTopIndex() {
		File file = TestUtils.getTestFile("phpbb", "http://bikeradar.com/forums/");
		String html = FileUtils.read(file);
		PhpBBJuicer juicer = new PhpBBJuicer();
		JuiceMe doc = new JuiceMe("http://bikeradar.com/forums/", html);
		
		boolean hm = juicer.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		System.out.println(items);
		assert items.size() > 1;
	}

	
	@Test
	public void testJuiceBikeRadarSearch() {
		String url = "http://www.bikeradar.com/forums/search.php?keywords=gatorade";
		File file = TestUtils.getTestFile("phpbb", url);
		String html = FileUtils.read(file);
		PhpBBJuicer juicer = new PhpBBJuicer();
		JuiceMe doc = new JuiceMe(url, html);
		
		boolean hm = juicer.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		for (Item item : items) {
			System.out.println(item.getUrl()+"\t"+item.isStub()+"\t"+item.getTitle());
		}
		assert items.size() > 1;
	}

	

	@Test
	public void testJuiceCruiseSite() {
		String url = "http://cruise-community.me.uk/";
		File file = TestUtils.getTestFile("phpbb", url);
		String html = FileUtils.read(file);
		PhpBBJuicer juicer = new PhpBBJuicer();
		JuiceMe doc = new JuiceMe(url, html);
		
		boolean hm = juicer.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		for (Item item : items) {
			System.out.println(item.getUrl()+"\t"+item.isStub()+"\t"+item.getTitle());
		}
		assert items.size() > 1;				
	}
	
	
	@Test
	public void testWithSpider() {
		XId.setService2canonical(IDoCanonical.DUMMY_CANONICALISER);
		
		String site = "http://www.bikeradar.com/forums";
		JuicingSiteSpider jss = new JuicingSiteSpider(site);
		
		jss.run();
		
		Set<Item> items = jss.getItems();
		System.out.println(items);
		assert items.size() > 0;
			
		for (Item item : items) {
			if (item.getUrl().contains("sid=")) {
				// WTF?
				System.err.println(item.getUrl()+" "+item.getXId2()+" "+item);
			}
		}	
	}
	
	@Test public void testTopicScrapeWithUrlCleanup() {
		String url = "http://www.bikeradar.com/forums/viewtopic.php?f=40012&p=18499001&sid=6f0f537bbd3e60c552de35be372abf9c#p18499001";
		File html = TestUtils.getTestFile("phpbb", url);
		JuiceMe doc = new JuiceMe(url, FileUtils.read(html));
		
		PhpBBJuicer j = new PhpBBJuicer();
		
		boolean ok = j.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		assert ! items.isEmpty();
		for (Item item : items) {
			assert ! item.getUrl().contains("sid=") : item.getUrl()+" "+item;
		}
	}
	

	@Test
	public void testJuiceMemberPage() {
		String url = "http://www.tritalk.co.uk/forums/profile.php?mode=viewprofile&u=13184";				
		File file = TestUtils.getTestFile("phpbb", url);
		String html = FileUtils.read(file);
		PhpBBJuicer juicer = new PhpBBJuicer();
		JuiceMe doc = new JuiceMe(url, html);
		
		boolean hm = juicer.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		assert ! items.isEmpty();
		for (Item item : items) {
			System.out.println(item.getXId()+"\t"+item.getAuthor()+" "+StrUtils.ellipsize(Printer.toString(item.getAnnotations()), 200));
		}
	}
	
	

	@Test
	public void testJuiceBikeRadar1Topic() {
		String url = "http://bikeradar.com/forums/viewforum.php?f=40065";
				
		File file = TestUtils.getTestFile("phpbb", url);
		String html = FileUtils.read(file);
		PhpBBJuicer juicer = new PhpBBJuicer();
		JuiceMe doc = new JuiceMe(url, html);
		
		boolean hm = juicer.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		System.out.println(items);
	}


	@Test
	public void testJuiceBikeRadarThread() {
		String url = "http://www.bikeradar.com/forums/viewtopic.php?f=20005&t=12937410"; 
				
		File file = TestUtils.getTestFile("phpbb", url);
		String html = FileUtils.read(file);
		PhpBBJuicer juicer = new PhpBBJuicer();
		JuiceMe doc = new JuiceMe(url, html);
		
		boolean hm = juicer.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		System.out.println(items);
		assert items.size() > 2;
	}


}
