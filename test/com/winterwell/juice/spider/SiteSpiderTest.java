package com.winterwell.juice.spider;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import winterwell.maths.graph.DiGraph;
import winterwell.maths.graph.DiNode;
import winterwell.maths.graph.DotPrinter;
import winterwell.utils.IFilter;
import winterwell.utils.Printer;
import winterwell.utils.containers.Containers;
import winterwell.utils.gui.GuiUtils;
import winterwell.utils.io.FileUtils;

import com.winterwell.juice.Item;
import com.winterwell.juice.TestUtils;

public class SiteSpiderTest {

	@Test public void testLinkExtract() {
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
		assert f.accept(url);
	}
	
	@Test
	public void testSiteSpider() throws Exception {
		final SiteSpider ss = new SiteSpider("http://winterwell.com");
		
		// php only
		IFilter<String> f = Containers.And(ss.getUrlFilter(), new IFilter<String>() {
			@Override
			public boolean accept(String x) {
				return x.endsWith(".php");
			}
		});
		ss.setUrlFilter(f);
		
		DiGraph<Item> web = ss.run();
		
		Printer.out(web);
		
		DotPrinter dp = new DotPrinter<DiNode<Item>>(web) {			
			@Override
			protected String getLabel(DiNode<Item> object) {
				Item item = object.getValue();
				if (item==null) {
					return ""+object.hashCode();	
				}
				String u = item.getUrl();
				if (u.startsWith(ss.getStartUrl())) {
					return u.substring(ss.getStartUrl().length());
				}
				return u;
			}
		};
		
		File dotFile = File.createTempFile("dot", ".dot");
		FileUtils.write(dotFile, dp.out());
		Printer.out(dotFile);

		File imgFile = File.createTempFile("dot", ".png");
		DotPrinter.create(dotFile, "png", imgFile);
		Printer.out(imgFile);

		GuiUtils.popupAndBlock(GuiUtils.load(imgFile));
	}

}
