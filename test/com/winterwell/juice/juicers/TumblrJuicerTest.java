/**
 * 
 */
package com.winterwell.juice.juicers;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.winterwell.utils.io.FileUtils;

import com.winterwell.juice.Item;
import com.winterwell.juice.Juice;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.TestUtils;

/**
 * @tested PageJuicer
 * @author daniel
 *
 */
public class TumblrJuicerTest {

	

	@Test
	public void testTumblrIndex() throws Exception {
		String url = "http://tumblr.com/tagged/edinburgh";
		String html = FileUtils.read(TestUtils.getTestFile("tumblr", url));
		Juice j = new Juice();
		JuiceMe doc = j.juice(url, html);
		List<Item> items = doc.getExtractedItems();		
		TestUtils.out(items);
		// assert items.size() > 1;
	}

	@Test
	public void testTumblog() {
		String url = "http://giuseppesantillo.tumblr.com/";
		File file = TestUtils.getTestFile("tumblr", url);
		
		String html = FileUtils.read(file);
		
		JuiceMe jm = new JuiceMe(url, html);
		TumblrJuicer tj = new TumblrJuicer();
		
		tj.juice(jm);
		
		List<Item> items = jm.getExtractedItems();
		
		TestUtils.out(items);
	}
	
	

	@Test
	public void testSearch() {
		String url = "http://www.tumblr.com/search/blogs?q=tacos";
		File file = TestUtils.getTestFile("tumblr", url);
		
		String html = FileUtils.read(file);
		
		JuiceMe jm = new JuiceMe(url, html);
		TumblrJuicer tj = new TumblrJuicer();
		
		tj.juice(jm);
		
		List<Item> items = jm.getExtractedItems();
		
		TestUtils.out(items);
	}
	

	@Test
	public void testTagged() {
		String url = "http://www.tumblr.com/tagged/foo";
		File file = TestUtils.getTestFile("tumblr", url);
		
		String html = FileUtils.read(file);
		
		JuiceMe jm = new JuiceMe(url, html);
		TumblrJuicer tj = new TumblrJuicer();
		
		tj.juice(jm);
		
		List<Item> items = jm.getExtractedItems();
		
		TestUtils.out(items);
	}

}
