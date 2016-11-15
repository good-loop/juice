package com.winterwell.juice.spider;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import winterwell.maths.graph.DiGraph;
import winterwell.maths.graph.DiNode;
import winterwell.maths.graph.DotPrinter;
import winterwell.utils.StrUtils;

import com.winterwell.juice.Item;
import com.winterwell.utils.Printer;
import com.winterwell.utils.gui.GuiUtils;
import com.winterwell.utils.io.FileUtils;
import com.winterwell.utils.web.XStreamUtils;

import creole.data.IDoCanonical;
import creole.data.XId;

public class JuicingSiteSpiderTest {

	

	@Test
	public void testXStream() throws IOException {

		String site = "http://www.bikeradar.com/forums";
		final JuicingSiteSpider jss = new JuicingSiteSpider(site);
		
		String xml = XStreamUtils.serialiseToXml(jss);
		System.out.println(xml.length()+" "+StrUtils.ellipsize(xml, 400));
	}
	

	@Test
	public void testPagesTouched() throws IOException {
		// init XId
		XId.setService2canonical(IDoCanonical.DUMMY_CANONICALISER);
		
		String site = "http://www.bikeradar.com/forums";
		final JuicingSiteSpider jss = new JuicingSiteSpider(site);
//		jss.buildWeb = true;
		jss.setMaxDepth(3);
		DiGraph<Item> web = jss.run();
		Set<Item> items = jss.getItems();
		
		int i=0;
		Set uniq = new HashSet();
		for (Item item : items) {
			i++;
			System.out.println(i+". "+item.getTitle()+"\t"+item.getUrl()+"\t"+item.getXId2());
			assert ! uniq.contains(item.getXId2()) : item.getXId2();
			uniq.add(item.getXId2());
		}
				
		Printer.out(web);		
	}
	
	@Test
	public void testGetItems() throws IOException {
		final JuicingSiteSpider jss = new JuicingSiteSpider("http://www.soda.sh/static/blog");
		XId.setService2canonical(IDoCanonical.DUMMY_CANONICALISER);
//		jss.buildWeb = true;
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
