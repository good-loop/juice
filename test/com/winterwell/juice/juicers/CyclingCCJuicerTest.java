package com.winterwell.juice.juicers;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.TestUtils;
import com.winterwell.utils.containers.Pair2;
import com.winterwell.utils.io.FileUtils;
import com.winterwell.utils.web.WebUtils2;

import com.winterwell.utils.StrUtils;
import com.winterwell.utils.Utils;
import creole.data.IDoCanonical;
import creole.data.XId;

public class CyclingCCJuicerTest {

	public CyclingCCJuicerTest() {
		XId.setService2canonical(IDoCanonical.DUMMY_CANONICALISER);
	}
	
	@Test
	public void testJuiceIndex() {
		String url = "http://www.cyclingweekly.cc/forum/all";
		File file = TestUtils.getTestFile("cycling.cc", url);
		String html = FileUtils.read(file);
		JuiceMe doc = new JuiceMe(url, html);
		
		CyclingCCJuicer j = new CyclingCCJuicer();
		
		boolean ok = j.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		
		Object warnings = TestUtils.out(items);
		System.out.println(warnings);
		
		assert items.size() > 1 : items;
	}

	@Test
	public void testJuiceThread() {
		// page 3 of a long thread
		String url = "http://www.cyclingweekly.cc/forum/racing-314/vuelta-a-espaa-chat-may-contain-spoilers-155467?page=3";
		File file = TestUtils.getTestFile("cycling.cc", url);
		String html = FileUtils.read(file);
		
//		WebUtils2.display(html);
		
		JuiceMe doc = new JuiceMe(url, html);
		
		CyclingCCJuicer j = new CyclingCCJuicer();
		
		boolean ok = j.juice(doc);
		
		List<Item> items = doc.getExtractedItems();

		Object warnings = TestUtils.out(items);
		System.out.println(warnings);
		
		assert items.size() > 1 : items;
	}
	
}
