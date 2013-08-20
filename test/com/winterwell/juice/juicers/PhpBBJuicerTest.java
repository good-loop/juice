package com.winterwell.juice.juicers;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.TestUtils;
import com.winterwell.juice.juicers.PhpBBJuicer;

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
		String url = "";
				
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
