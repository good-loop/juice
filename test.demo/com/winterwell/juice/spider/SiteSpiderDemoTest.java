package com.winterwell.juice.spider;

import java.io.File;

import org.junit.Test;

import winterwell.maths.graph.DiGraph;
import winterwell.maths.graph.DiNode;
import winterwell.maths.graph.DotPrinter;

import com.winterwell.juice.Item;
import com.winterwell.utils.io.FileUtils;

import winterwell.utils.IFilter;
import winterwell.utils.Printer;
import winterwell.utils.containers.Containers;
import winterwell.utils.gui.GuiUtils;
import creole.data.IDoCanonical;
import creole.data.XId;

public class SiteSpiderDemoTest {

	@Test
	public void testSiteSpider() throws Exception {
		final SiteSpider ss = new SiteSpider("http://winterwell.com");
		XId.setService2canonical(IDoCanonical.DUMMY_CANONICALISER);
		
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
