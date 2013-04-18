package com.winterwell.juice;

import java.io.File;

import org.junit.Test;

import winterwell.utils.io.FileUtils;

public class PinterestJuicerTest {

	@Test
	public void testJuice() {		
//		"http://pinterest.com/pin/262827328226288279/";
		String url = "http://pinterest.com/pin/262827328225815837/";
		File local = TestUtils.getTestFile("pinterest", url);
		PinterestJuicer pj = new PinterestJuicer();
		String html = FileUtils.read(local);
		JuiceMe doc = new JuiceMe(url, html);
		boolean ok = pj.juice(doc);
		Item item = doc.getMainItem();
		System.out.println(item.getTitle());
		System.out.println(item.getText());
//		System.out.println(item.getHTML());	
		System.out.println(item.get(AJuicer.IMAGE_URL));
		
		JuiceMe juiced = new Juice().juice(url, html);
		Item item2 = juiced.getMainItem();
		System.out.println(item2.getTitle());
		System.out.println(item2.getText());	
		System.out.println(item2.get(AJuicer.IMAGE_URL));
		
	}

}
