/**
 * 
 */
package com.winterwell.juice;

import java.io.File;
import java.util.List;

import org.junit.Test;

import winterwell.utils.StrUtils;
import winterwell.utils.Utils;
import winterwell.utils.io.FileUtils;
import winterwell.utils.time.TUnit;
import winterwell.utils.time.Time;
import winterwell.utils.time.TimeUtils;
import winterwell.web.FakeBrowser;

/**
 * @tested PageJuicer
 * @author daniel
 *
 */
public class JuiceTest {

//	@Test
	public void testWinterwellCom() {
		
	}
	
	/**
	 * c.f. bug #3646
	 */
	@Test
	public void testRefLoop() {
		String url = "http://news.google.com/news/url?sa=t&fd=R&usg=AFQjCNFdiIbKyKJ0bp2bQgdXxUDX3xsZeQ&url=http://metro.co.uk/2013/05/28/gordon-brown-is-parliaments-highest-earning-mp-but-he-donates-it-all-to-charity-3810963/";
		File file = TestUtils.getTestFile("misc", url);
		Juice j = new Juice();
		String html = FileUtils.read(file);
		JuiceMe juiced = j.juice(url, html);
		List<Item> items = juiced.getExtractedItems();
		for (Item item : items) {
			String iurl = item.get(AJuicer.URL);
			System.out.println(item.getXId()+"\t"+item.getTitle()+"\t"+iurl+"\t"+StrUtils.ellipsize(item.getText(), 100));
		}
	}
	
	/**
	 * c.f. bug #3747
	 */
	@Test
	public void testRelatedItems() {
		String url = "http://news.google.com/news/url?sa=t&fd=R&usg=AFQjCNFdiIbKyKJ0bp2bQgdXxUDX3xsZeQ&url=http://metro.co.uk/2013/05/28/gordon-brown-is-parliaments-highest-earning-mp-but-he-donates-it-all-to-charity-3810963/";
		File file = TestUtils.getTestFile("misc", url);
		Juice j = new Juice();
		String html = FileUtils.read(file);
		JuiceMe juiced = j.juice(url, html);
		List<Item> items = juiced.getExtractedItems();
		for (Item item : items) {
			String iurl = item.get(AJuicer.URL);
			System.out.println(item.getXId()+"\t"+item.getTitle()+"\t"+iurl+"\t"+StrUtils.ellipsize(item.getText(), 100));
		}
		assert items.size() == 1;
	}
	
	@Test
	public void testBadParse() {
		String url = "http://28g.co.uk/story.php?title=flats-to-rent-edinburgh";
		File file = TestUtils.getTestFile("misc", url);
		Juice j = new Juice();
		String html = FileUtils.read(file);
		JuiceMe juiced = j.juice(url, html);
		List<Item> items = juiced.getExtractedItems();
		for (Item item : items) {
			String iurl = item.get(AJuicer.URL);
			assert iurl==null || iurl.startsWith("http") : iurl;
			System.out.println(item.getXId()+"\t"+item.getTitle()+"\t"+iurl);
		}
	}
	
	@Test
	public void testBug2997() {
		String url = "http://www.solunet-infomex.com/mexbdir/company.cfm?company=1545622_Telmex_Guadalajara_Jalisco";
		File file = TestUtils.getTestFile("misc", url);
		Juice j = new Juice();
		String html = FileUtils.read(file);
		JuiceMe juiced = j.juice(url, html);
		List<Item> items = juiced.getExtractedItems();
		for (Item item : items) {
			String iurl = item.get(AJuicer.URL);
			assert iurl==null || iurl.startsWith("http") : iurl;
			System.out.println(item.getXId()+"\t"+item.getTitle()+"\t"+iurl);
		}
	}
	
	@Test
	public void testMiscBlogEngine() {
		String url = "http://www.runnersworld.co.uk/gear/gear-pick-merrell-barefoot-road-glove-dash-2/9500.html";
		File file = TestUtils.getTestFile("misc", url);
		Juice j = new Juice();
		String html = FileUtils.read(file);
		JuiceMe juiced = j.juice(url, html);
		List<Item> items = juiced.getExtractedItems();
		for (Item item : items) {
			System.out.println(item.getXId()+"\t"+item.getTitle()+"\t"+item.get(AJuicer.URL));
		}
	}

	
	@Test
	public void testNoUrl() {
		String url = "http://familyfriendly.wordpress.com/2011/10/20/dot-fines-orbitz-for-violating-airline-price-advertising-rules/";
		File file = TestUtils.getTestFile("wordpress", url);
		Juice j = new Juice();
		JuiceMe juiced = j.juice(url, FileUtils.read(file));
		List<Item> items = juiced.getExtractedItems();
		for (Item item : items) {
			System.out.println(item.getXId()+"\t"+item.getTitle()+"\t"+item.get(AJuicer.URL));
		}
	}
	
//	@Test
	public void testWordpressFrontPage() {
		
	}
	
//	@Test
	public void testWordpressBlogPost() {
		
	}

	
//	@Test
	public void testGuardianFrontPage() {
		String html = new FakeBrowser().getPage("http://www.guardian.co.uk");
	}
	
	@Test
	public void testGuardianArticle() {
		String url = "http://www.guardian.co.uk/technology/2012/feb/29/raspberry-pi-computer-sale-british";
		String htmlFileName = "raspberryPi.html";
		String html = FileUtils.read(new File("test/testHTMLFiles/MetaDataJuicerTest/"+htmlFileName)); //new FakeBrowser().getPage(url);
		Juice pj = new Juice();
		
		JuiceMe doc = pj.juice(url, html);
		Item extractedItem = doc.getExtractedItems().get(0);
		
		System.out.println(extractedItem);
		assert extractedItem.getTitle().equals("Demand for Raspberry Pi, the British £22 computer, crashes website") : extractedItem.getTitle();
		
		assert ! Utils.isBlank(extractedItem.getAuthor()) : extractedItem.getAnnotations();		
		assert TimeUtils.equalish(extractedItem.getPublishedTime(),
									new Time(2012, 2, 29), TUnit.DAY) : extractedItem.getPublishedTime();
	}

}
