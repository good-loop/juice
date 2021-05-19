package com.winterwell.juice;

import java.io.File;
import java.util.Arrays;
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
	public void testScrapeColour() throws Exception {
		JuiceForAnAdvert ja = new JuiceForAnAdvert();		
		File f = new File("test/cols-scheme-test.png");
		List<String> cols = ja.scrapeColours(f, 128);
		Printer.out(cols);
		assert(cols.contains("#98c24c"));
	}
	


	@Test
	public void testScrapeImages() throws Exception {
		String url = "http://www.narcissusflowers.co.uk/";
		String html = FileUtils.read(TestUtils.getTestFile("company-website", url));
		JuiceForAnAdvert ja = new JuiceForAnAdvert();		
		Juice j = new Juice(Arrays.asList(ja));
		JuiceMe doc = j.juice(url, html);		
		Item item = doc.getMainItem();
		List<String> imgs = item.get(AJuicer.IMAGE_URLS);
		System.out.println(imgs);
		assert imgs != null && ! imgs.isEmpty();
	}
	
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
