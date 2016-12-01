package com.winterwell.juice.juicers;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.TestUtils;
import com.winterwell.utils.io.FileUtils;
import com.winterwell.web.data.IDoCanonical;
import com.winterwell.web.data.XId;

public class ContactDetailsJuicerTest {

	public ContactDetailsJuicerTest() {
		XId.setService2canonical(IDoCanonical.DUMMY_CANONICALISER);
	}
	
	@Test
	public void testJuiceBoat() {
		String url = "http://www.manxbaskingsharkwatch.org/endeavour/";
		File file = TestUtils.getTestFile("sharks", url);
		String html = FileUtils.read(file);
		JuiceMe doc = new JuiceMe(url, html);
		
		ContactDetailsJuicer j = new ContactDetailsJuicer();
		
		boolean ok = j.juice(doc);
		
		List<Item> items = doc.getExtractedItems();
		
		Object warnings = TestUtils.out(items);
		System.out.println(warnings);
		
		assert items.size() > 1 : items;
	}

	
}
