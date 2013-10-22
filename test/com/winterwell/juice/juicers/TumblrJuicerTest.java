/**
 * 
 */
package com.winterwell.juice.juicers;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.TestUtils;
import com.winterwell.juice.juicers.TumblrJuicer;

import winterwell.utils.StrUtils;
import winterwell.utils.Utils;
import winterwell.utils.io.FileUtils;
import winterwell.utils.reporting.Log;
import winterwell.utils.time.TUnit;
import winterwell.utils.time.Time;
import winterwell.utils.time.TimeUtils;
import winterwell.web.FakeBrowser;

/**
 * @tested PageJuicer
 * @author daniel
 *
 */
public class TumblrJuicerTest {


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
	
}
