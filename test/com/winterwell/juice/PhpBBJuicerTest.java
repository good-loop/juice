package com.winterwell.juice;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import winterwell.utils.io.FileUtils;

public class PhpBBJuicerTest {

	@Test
	public void testJuiceBikeRadarTopIndex() {
		File file = TestUtils.getTestFile("phpbb", "http://bikeradar.com/forums/");
		String html = FileUtils.read(file);
		PhpBBJuicer juicer = new PhpBBJuicer();
		JuiceMe doc = new JuiceMe("http://bikeradar.com/forums/", html);
		
		boolean hm = juicer.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		System.out.println(items);
	}

	

	@Test
	public void testJuiceBikeRadarMemberPage() {
		String url = null;
				
		File file = TestUtils.getTestFile("phpbb", url);
		String html = FileUtils.read(file);
		PhpBBJuicer juicer = new PhpBBJuicer();
		JuiceMe doc = new JuiceMe(url, html);
		
		boolean hm = juicer.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		System.out.println(items);
	}
	
	

	@Test
	public void testJuiceBikeRadar1Topic() {
		String url = null;
				
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
		String url = null; 
				
		File file = TestUtils.getTestFile("phpbb", url);
		String html = FileUtils.read(file);
		PhpBBJuicer juicer = new PhpBBJuicer();
		JuiceMe doc = new JuiceMe(url, html);
		
		boolean hm = juicer.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		System.out.println(items);
	}


}
