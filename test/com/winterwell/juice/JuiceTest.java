/**
 * 
 */
package com.winterwell.juice;

import java.io.File;
import java.util.List;

import org.junit.Test;

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
		
		System.out.println(doc);
		assert doc.getTitle().equals("Demand for Raspberry Pi, the British £22 computer, crashes website") : doc.getTitle();
		
		assert ! Utils.isBlank(doc.getAuthor()) : doc.getExtractedItems();		
		assert TimeUtils.equalish(doc.getPublishedTime(),
									new Time(2012, 2, 29), TUnit.DAY) : doc.getPublishedTime();
	}

}
