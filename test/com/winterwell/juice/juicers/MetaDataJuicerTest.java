package com.winterwell.juice.juicers;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.KMsgType;
import com.winterwell.juice.TestUtils;
import com.winterwell.utils.Key;
import com.winterwell.utils.io.FileUtils;
import com.winterwell.utils.time.Time;

public class MetaDataJuicerTest {
	
	
	@Test
	public void testSimpleExample() throws Exception {
		String htmlFileName = "simpleExample.html";
		
		
		HashMap<Key, Object> expectedElements = new HashMap<Key, Object>() {{
			put(AJuicer.TITLE, "The Rock");
			put(AJuicer.MSG_TYPE, KMsgType.VIDEO);
			put(AJuicer.URL, "http://www.imdb.com/title/tt0117500/");
			put(AJuicer.IMAGE_URL, "http://ia.media-imdb.com/images/rock.jpg");
			put(AJuicer.AUTHOR_XID, "anon@imdb.com");
		}};
		
		testJuicer(htmlFileName, expectedElements);
		
	}
	
	@Test
	public void testGuardianPage() throws Exception {
		String htmlFileName = "raspberryPi.html";
		
		Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse("2012-02-29T08:46:19Z");
		final Time articleTime = new Time(date);
		
		final ArrayList<String> expectedTags = new ArrayList<String>() {{
			add("Raspberry Pi");
			add("Computing");
			add("UK news");
			add("Technology");
			add("Education");
			add("Internet");
			add("Software");
			add("Linux");
		}};
		
		HashMap<Key, Object> expectedElements = new HashMap<Key, Object>() {{
			put(AJuicer.DESC, "Design intended to inspire schoolchildren and adults to program sees overwhelming demand as first versions go on sale. By Charles Arthur");
			put(AJuicer.TITLE, "Demand for Raspberry Pi, the British £22 computer, crashes website");
			put(AJuicer.MSG_TYPE, KMsgType.MISC);
			put(AJuicer.PUB_TIME, articleTime);

			put(AJuicer.AUTHOR_XID, "http://www.guardian.co.uk/profile/charlesarthur@web");
			//put(AJuicer.AUTHOR_NAME, "Charles Arthur"); // would be nice; not essential
			
			put(AJuicer.TAGS, expectedTags);
			
			put(AJuicer.URL, "http://www.guardian.co.uk/technology/2012/feb/29/raspberry-pi-computer-sale-british");
			put(AJuicer.IMAGE_URL, "https://static-secure.guim.co.uk/sys-images/Admin/BkFill/Default_image_group/2011/8/26/1314356568077/Engineer-Eben-Upton-with--003.jpg");
			
		}};
		
		testJuicer(htmlFileName, expectedElements);
		
	}
	
	private String testDirectoryPrefix = "test/testHTMLFiles/MetaDataJuicerTest/";

	@Test
	public void testTumblr() {
		// a post
		String url = "http://claudiarndt.tumblr.com/post/62083315543/frosch-philosophie";
		
		File file = TestUtils.getTestFile("wordpress", url);
		String html = FileUtils.read(file);
		
		JuiceMe document = new JuiceMe(html);
		
		MetaDataJuicer mdj = new MetaDataJuicer();
		mdj.juice(document);
		
		List<Item> items = document.getExtractedItems();
		TestUtils.out(items);
		
		Item main = document.getMainItem();
		System.out.println(main.getAuthor());
		// TODO assert main.getAuthor().toLowerCase().startsWith("claudia") : main.getAuthor();
		
	}
	
		
	private void testJuicer(String fileName, HashMap<Key, Object> expectedAnnotations) throws Exception {
		String filePath = testDirectoryPrefix + fileName;
		String html = TestUtils.readFile(filePath);
		
		JuiceMe document = new JuiceMe(html);
		
		MetaDataJuicer mdj = new MetaDataJuicer();
		mdj.juice(document);
		Item extractedItem = document.getExtractedItems().get(0); 
				
		System.out.println(extractedItem.getAnnotations());
		
		int numberOfAnnotations = extractedItem.getAnnotations().size();
		assertEquals(expectedAnnotations.keySet().size(), numberOfAnnotations);
		
		for (Key key : expectedAnnotations.keySet()) {
			Object expectedValue = expectedAnnotations.get(key);
			Object actualValue = extractedItem.getAnnotation(key).value;
			
			assertEquals(expectedValue, actualValue);
		}
	}	
	
}
