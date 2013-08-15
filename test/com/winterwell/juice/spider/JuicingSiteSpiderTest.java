package com.winterwell.juice.spider;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import winterwell.maths.graph.DiGraph;
import winterwell.maths.graph.DiNode;
import winterwell.maths.graph.DotPrinter;
import winterwell.utils.Printer;
import winterwell.utils.gui.GuiUtils;
import winterwell.utils.io.FileUtils;

import com.winterwell.juice.Item;

public class JuicingSiteSpiderTest {

	

	@Test
	public void testPagesTouched() throws IOException {
		String site = "http://www.bikeradar.com/forums";
		final JuicingSiteSpider jss = new JuicingSiteSpider(site);
		jss.buildWeb = true;
		jss.setMaxDepth(3);
		DiGraph<Item> web = jss.run();
		Set<Item> items = jss.getItems();
		
		int i=0;
		Set<String> uniq = new HashSet();
		for (Item item : items) {
			i++;
			System.out.println(i+". "+item.getTitle()+"\t"+item.getUrl());
			assert ! uniq.contains(item.getUrl()) : item.getUrl();
			uniq.add(item.getUrl());
		}
				
		Printer.out(web);		
	}
	
	@Test
	public void testGetItems() throws IOException {
		final JuicingSiteSpider jss = new JuicingSiteSpider("http://www.soda.sh/static/blog");
		jss.buildWeb = true;
		jss.setMaxDepth(3);
		DiGraph<Item> web = jss.run();
		Set<Item> items = jss.getItems();
		System.out.println(items);
		
		
		Printer.out(web);
		
		DotPrinter dp = new DotPrinter<DiNode<Item>>(web) {			
			@Override
			protected String getLabel(DiNode<Item> object) {
				Item item = object.getValue();
				if (item==null) {
					return ""+object.hashCode();	
				}
				String u = item.getUrl();
				if (u.startsWith(jss.getStartUrl())) {
					return u.substring(jss.getStartUrl().length());
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
