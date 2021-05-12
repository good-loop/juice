package com.winterwell.juice;

import java.util.List;

import org.junit.Test;

import com.winterwell.utils.Printer;
import com.winterwell.utils.io.FileUtils;

/**
 * Following MailChimp's Creative Assistant -- Can we extract name, logo, fonts, colours, photo, tagline?
 * (noting that MailChimp doesn't do a great job there)
 * @author daniel
 *
 */
public class JuiceForAnAdvertTest {
	
	@Test
	public void testInC() throws Exception {
		String url = "https://good-loop.com/";
		String html = FileUtils.read(TestUtils.getTestFile("company-website", url));
		
		Juice j = new Juice();
		j.addJuicer(new JuiceForAnAdvert(), false);
		JuiceMe doc = j.juice(url, html);
		
		List<Item> items = doc.getExtractedItems();	
		TestUtils.out(items);
		
		Item item = doc.getMainItem();
		
		Printer.out(Printer.toString(item.getAnnotations(), "\n"));
		
		// name
		String name = item.get(AJuicer.PUBLISHER_NAME);
		assert name.equalsIgnoreCase("good-loop") : name;
		// logo
		String logo = item.get(JuiceKeys.LOGO);
		assert logo != null;
		// tagline
		String tagline = item.get(AJuicer.TAGLINE);
		assert tagline.equalsIgnoreCase("effective advertising that's a force for good in the world");
		// TODO
		// photos
		// fonts
		String font = item.get(AJuicer.FONT_FAMILY);
		assert font != null;
		// colours
	}
}
