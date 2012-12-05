//package com.winterwell.juice;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.Test;
//
//import winterwell.utils.Utils;
//import winterwell.utils.time.TUnit;
//import winterwell.utils.time.Time;
//import winterwell.utils.time.TimeUtils;
//import winterwell.web.FakeBrowser;
//
//public class GooseJuicerTest {
//
//	
//	@Test
//	public void testGuardianArticle() {
//		String url = "http://www.guardian.co.uk/technology/2012/feb/29/raspberry-pi-computer-sale-british";
//		String html = new FakeBrowser().getPage(url);
//		GooseJuicer pj = new GooseJuicer();
//		
//		JuiceMe doc = new JuiceMe(url, html);
//				
//		pj.juice(doc);
//		Item extractedItem = doc.getExtractedItems().get(0);
//		
//		System.out.println(extractedItem.getAnnotations());
//		
//		System.out.println(extractedItem.getTitle()+" by "+extractedItem.getAuthor()+" on "+extractedItem.getPublishedTime());		
//		
//		assert ! Utils.isBlank(extractedItem.getAuthor());
//		assert ! Utils.isBlank(extractedItem.getTitle());		
//		
//		assert TimeUtils.equalish(extractedItem.getPublishedTime(),
//									new Time(2012, 2, 29), TUnit.DAY) : extractedItem.getPublishedTime();
//	}
//	
//
//	@Test
//	public void testSandpitBlog() {
//		String url = "http://thesandpit.com/the-founders-blog/2012/4/5/big-data-must-be-accurate-data.html";
//		String html = new FakeBrowser().getPage(url);
//		GooseJuicer pj = new GooseJuicer();
//		
//		JuiceMe doc = new JuiceMe(url, html);
//		
//		pj.juice(doc);
//		Item extractedItem = doc.getExtractedItems().get(0);
//		
//		System.out.println(extractedItem.getTitle()+" by "+extractedItem.getAuthor()+" on "+extractedItem.getPublishedTime());
//		System.out.println(extractedItem.getText());
//	}
//
//}
