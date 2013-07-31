package com.winterwell.juice.spider;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
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
