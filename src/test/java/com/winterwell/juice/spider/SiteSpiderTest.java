package com.winterwell.juice.spider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import com.winterwell.juice.Item;
import com.winterwell.juice.TestUtils;
import com.winterwell.maths.graph.DiGraph;
import com.winterwell.maths.graph.DiNode;
import com.winterwell.utils.io.FileUtils;

public class SiteSpiderTest {
	

	@Test 
	public void testSiteSpider() {
		String domain = "news.com.au";
		SiteSpider ss = new SiteSpider("https://"+domain);
		ss.setMaxDepth(1);
		DiGraph<Item> graph = ss.run();
		System.out.println(graph);
		Collection<DiNode<Item>> nodes = graph.getNodes();
		for (DiNode<Item> diNode : nodes) {
			System.out.println(diNode.getValue().getUrl());
		}
	}
	

	@Test 
	public void testLinkExtract() {
		String url = "http://www.bikeradar.com/racing/";
		File f = TestUtils.getTestFile("forum", url);
		String html = FileUtils.read(f);
		
//		String snippet = html.substring(27645, 27652);
		String snippet = html.substring(27600, 27700);
		System.out.println(snippet);
		
		Spiderlet spiderlet = new Spiderlet(null, url, 0);
		List<String> links = new ArrayList();
		spiderlet.extractLinks(html, links);
		System.out.println(links);
		
	}
	
	@Test
	public void testRegex() {
		String start = "http://www.soda.sh/static/blog";
		String url = "http://www.soda.sh/static/blog/wp-content/themes/twentyten/style.css";
		
		Pattern domainP = Pattern.compile("^https?://(\\w+\\.)?"+Pattern.quote("soda.sh"));
		assert domainP.matcher(url).find();
		assert domainP.matcher(start).find();
		assert domainP.matcher("https://soda.sh/foobar").find();
		
		InSiteFilter f = new InSiteFilter(start);
		assert !f.accept(url);
	}
}
