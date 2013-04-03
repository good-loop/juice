package com.winterwell.juice;

import java.io.File;

import org.junit.Test;

import winterwell.utils.io.FileUtils;

public class PinterestJuicerTest {

	@Test
	public void testJuice() {		
		String url = "http://pinterest.com/pin/262827328225815837/";
		File local = TestUtils.getTestFile("pinterest", url);
		PinterestJuicer pj = new PinterestJuicer();
		String html = FileUtils.read(local);
		JuiceMe doc = new JuiceMe(url, html);
		boolean ok = pj.juice(doc);
		Item item = doc.getMainItem();
		System.out.println(item);		
	}

}
