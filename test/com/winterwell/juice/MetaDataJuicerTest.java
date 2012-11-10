package com.winterwell.juice;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import winterwell.utils.time.Time;

public class MetaDataJuicerTest {
	
	
	@Test
	public void testSimpleExample() throws Exception {
		String htmlFileName = "simpleExample.html";
		
		
		Set<Anno> expectedElements = new HashSet<Anno>() {{
			add(new Anno<String>(AJuicer.TITLE, "The Rock"));
			add(new Anno<KMsgType>(AJuicer.MSG_TYPE, KMsgType.VIDEO));
			add(new Anno<String>(AJuicer.URL, "http://www.imdb.com/title/tt0117500/"));
			add(new Anno<String>(AJuicer.IMAGE_URL, "http://ia.media-imdb.com/images/rock.jpg"));
		}};
		
		testJuicer(htmlFileName, expectedElements);
		
	}
	
	@Test
	public void testGuardianPage() throws Exception {
		String htmlFileName = "raspberryPi.html";
		
		Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse("2012-02-29T08:46:19Z");
		final Time articleTime = new Time(date);
		
		Set<Anno> expectedElements = new HashSet<Anno>() {{
			add(new Anno<String>(AJuicer.DESC, "Design intended to inspire schoolchildren and adults to program sees overwhelming demand as first versions go on sale. By Charles Arthur"));
			add(new Anno<String>(AJuicer.TITLE, "Demand for Raspberry Pi, the British £22 computer, crashes website"));
			add(new Anno<KMsgType>(AJuicer.MSG_TYPE, KMsgType.MISC));
			add(new Anno<Time>(AJuicer.PUB_TIME, articleTime));
			
			add(new Anno<String>(AJuicer.TAGS, "Raspberry Pi"));
			add(new Anno<String>(AJuicer.TAGS, "Computing"));
			add(new Anno<String>(AJuicer.TAGS, "UK news"));
			add(new Anno<String>(AJuicer.TAGS, "Technology"));
			add(new Anno<String>(AJuicer.TAGS, "Education"));
			add(new Anno<String>(AJuicer.TAGS, "Internet"));
			add(new Anno<String>(AJuicer.TAGS, "Software"));
			add(new Anno<String>(AJuicer.TAGS, "Linux"));
			
			add(new Anno<String>(AJuicer.URL, "http://www.guardian.co.uk/technology/2012/feb/29/raspberry-pi-computer-sale-british"));
			add(new Anno<String>(AJuicer.IMAGE_URL, "https://static-secure.guim.co.uk/sys-images/Admin/BkFill/Default_image_group/2011/8/26/1314356568077/Engineer-Eben-Upton-with--003.jpg"));
			
		}};
		
		testJuicer(htmlFileName, expectedElements);
		
	}
	
	private String testDirectoryPrefix = "test/testHTMLFiles/MetaDataJuicerTest/";

	private void testJuicer(String fileName, Set<Anno> expectedAnnotations) throws Exception {
		String filePath = testDirectoryPrefix + fileName;
		String html = TestUtils.readFile(filePath);
		
		JuiceMe document = new JuiceMe(html);
		
		MetaDataJuicer mdj = new MetaDataJuicer();
		mdj.juice(document);
		List<Anno> resultAnnotations = document.getAnnotations(); 
		
		containsSameElements(resultAnnotations, expectedAnnotations);
	}
	
	// Check extracted elements are the same as expected elements ignoring order
	private void containsSameElements(List<Anno> extracted, Set<Anno> expected) {
		Set<Anno> extractedSet = new HashSet<Anno>(extracted);
		
		/*System.out.print("Expected set: ");
		System.out.println(expected);
		System.out.print("Extracted set: ");
		System.out.println(extracted);
		
		for (Anno extracEl : extracted) {
			if (!expected.contains(extracEl)) {
				System.out.println("Not expected: " + extracEl);
			}
		}
		
		for (Anno expectEl : expected) {
			if (!extracted.contains(expectEl)) {
				System.out.println("Expected: " + expectEl);
			}
		}*/
		
		assertEquals(expected, extractedSet);
	}
	
	
}
