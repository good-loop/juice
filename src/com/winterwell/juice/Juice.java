package com.winterwell.juice;

import java.util.ArrayList;
import java.util.List;

import winterwell.utils.FailureException;
import winterwell.utils.Printer;
import winterwell.web.FakeBrowser;

/**
 * Extract stuff from web pages!
 * 
 * See:
 * http://tomazkovacic.com/blog/56/list-of-resources-article-text-extraction-from-html-documents/
 * 
 * @author daniel
 * @testedby {@link JuiceTest}
 */
public class Juice {
	
	AJuicer[] juicers;
	
	/**
	 * Just a test sketch of how to use this.
	 * @param args
	 */
	public static void main(String[] args) {
		Juice juice = new Juice();
		if (args==null || args.length==0) {
			args = new String[]{"http://www.guardian.co.uk/technology/2012/feb/29/raspberry-pi-computer-sale-british"};
		}
		
		for (String url : args) {
			String html = new FakeBrowser().getPage(url);
			JuiceMe juiced = juice.juice(url, html);
			System.out.println('"'+juiced.getTitle()+"\" by "+juiced.getAuthor()+" date:"+juiced.getPublishedTime());
		}
	}
	
	public Juice() {
		juicers = new AJuicer[]{
			new WordPressJuicer(),
			new MetaDataJuicer()
//			new GenericJuicer()	
		};
	}
	
	public JuiceMe juice(String url, String html) {		
		JuiceMe doc = new JuiceMe(url,html);
		for(AJuicer juicer : juicers) {
			juicer.juice(doc);
			// TODO stop early -- When are we done??
			
		}		
		return doc;
	}
}
