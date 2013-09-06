package com.winterwell.juice.juicers;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import winterwell.utils.io.FileUtils;

import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.TestUtils;

public class CyclingCCJuicerTest {

	@Test
	public void testJuiceIndex() {
		String url = "http://www.cyclingweekly.cc/forum/all";
		File file = TestUtils.getTestFile("cycling.cc", url);
		String html = FileUtils.read(file);
		JuiceMe doc = new JuiceMe(url, html);
		
		CyclingCCJuicer j = new CyclingCCJuicer();
		
		boolean ok = j.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		
		assert items.size() > 1 : items;
		System.out.println(items);
	}

	@Test
	public void testJuiceThread() {
		// page 3 of a long thread
		String url = "http://www.cyclingweekly.cc/forum/racing-314/vuelta-a-espaa-chat-may-contain-spoilers-155467?page=3";
		File file = TestUtils.getTestFile("cycling.cc", url);
		String html = FileUtils.read(file);
		JuiceMe doc = new JuiceMe(url, html);
		
		CyclingCCJuicer j = new CyclingCCJuicer();
		
		boolean ok = j.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		
		assert items.size() > 1 : items;
		System.out.println(items);
	}
	
}
