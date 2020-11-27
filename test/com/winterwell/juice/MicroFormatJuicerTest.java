package com.winterwell.juice;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.winterwell.utils.io.FileUtils;
import com.winterwell.utils.time.Time;

public class MicroFormatJuicerTest {

	@Test
	public void testDateTime() {
		// a post
		String url = "https://www.creativebrief.com/bite/amy-williams-founder-good-loop";
		
		File file = TestUtils.getTestFile("miscpage", url);
		String html = FileUtils.read(file);
		
		JuiceMe document = new JuiceMe(html);
		
		MicroFormatJuicer mdj = new MicroFormatJuicer();
		mdj.juice(document);
		
		List<Item> items = document.getExtractedItems();
		TestUtils.out(items);
		
		Item main = document.getMainItem();
		System.out.println(main.getPublishedTime());
		assert(main.getPublishedTime().equals(new Time(2020,5,18)));

	}

}
