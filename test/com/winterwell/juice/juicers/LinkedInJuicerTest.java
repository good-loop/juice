package com.winterwell.juice.juicers;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import winterwell.utils.io.FileUtils;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.MetaDataJuicer;
import com.winterwell.juice.PinterestJuicer;
import com.winterwell.juice.TestUtils;

public class LinkedInJuicerTest {

	@Test
	public void testJuice() {
		String url = "https://www.linkedin.com/grp/post/7445683-5898412079244681220";
		File local = TestUtils.getTestFile("linkedin", url);
		MetaDataJuicer mdj = new MetaDataJuicer();
		LinkedInJuicer pj = new LinkedInJuicer();
		String html = FileUtils.read(local);
		JuiceMe doc = new JuiceMe(url, html);
		
		mdj.juice(doc);
		boolean ok = pj.juice(doc);
		
		Item item = doc.getMainItem();
		System.out.println(item.getTitle());
		System.out.println(item.getXId());
		System.out.println(item.getText());
//		System.out.println(item.getHTML());	
		System.out.println(item.get(AJuicer.IMAGE_URL));
		System.out.println(item.get(AJuicer.LINK));
		System.out.println(item.get(AJuicer.AUTHOR_NAME));
		assert item.get(AJuicer.AUTHOR_NAME).contains("Duncan");

	}

}
