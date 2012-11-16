package com.winterwell.juice;

import org.junit.Test;

import winterwell.web.FakeBrowser;

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
			String html = fb.getPage("http://thesandpit.com/the-founders-blog/2012/4/5/big-data-must-be-accurate-data.html");
			String type = sniffer.sniff(html);
			assert type == null;
		}
	}
	
}
