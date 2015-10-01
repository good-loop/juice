package com.winterwell.juice;

import com.winterwell.web.FakeBrowser;

public class WordPressJuicerDemo {
	public static void main(String[] argv) {
		
		String url = "http://akrzemi1.wordpress.com/2012/09/30/why-make-your-classes-final/";
		String html = new FakeBrowser().getPage(url);
		
		JuiceMe doc = new JuiceMe(url, html);
		WordPressJuicer wpj = new WordPressJuicer();
		wpj.juice(doc);
		
		for (Item item : doc.getExtractedItems()) {
			System.out.println("Title: " + item.getTitle());
			System.out.println("Text: " + item.getText());
			System.out.println("First paragraph: " + item.getAnnotation(AJuicer.POST_BODY_PART).value);
			
			System.out.println("\n\n=============\n\n");
		}
	}
}
