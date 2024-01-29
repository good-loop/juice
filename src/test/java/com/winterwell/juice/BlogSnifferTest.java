package com.winterwell.juice;

import org.junit.Test;

import com.winterwell.web.FakeBrowser;

public class BlogSnifferTest {

	@Test
	public void testSniffWordpress() {
		BlogSniffer sniffer = new BlogSniffer();
		FakeBrowser fb = new FakeBrowser();
		{	// main page wordpress
			String html = fb.getPage("http://www.soda.sh/static/blog");
			String type = sniffer.sniff(html);
			assert BlogSniffer.WORDPRESS.equals(type) : type;
		}
		{	// post page wordpress
			String html = fb.getPage("http://www.soda.sh/static/blog/?p=52");
			String type = sniffer.sniff(html);
			assert BlogSniffer.WORDPRESS.equals(type) : type;
		}
	}
	
	@Test
	public void testSniffNotWordpress() {
		BlogSniffer sniffer = new BlogSniffer();
		FakeBrowser fb = new FakeBrowser();
		{	// Sandpit (ContentWave?)
			String html = fb.getPage("http://googleblog.blogspot.co.uk/2013/08/tenth-annual-code-jam-competition-wraps.html");
			String type = sniffer.sniff(html);
			assert !type.equals(BlogSniffer.WORDPRESS);
		}
	}
	
}
