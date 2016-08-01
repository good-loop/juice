package com.winterwell.juice.juicers;

import java.io.File;
import java.util.List;

import org.junit.Test;

import winterwell.utils.Utils;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Item;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.MetaDataJuicer;
import com.winterwell.juice.TestUtils;
import com.winterwell.utils.io.FileUtils;

public class LinkedInJuicerTest {

//	https://www.linkedin.com/grp/post/5042022-6020180410548383748
	
	@Test
	public void testLISteveBalmerProfile() {		
		String url = "https://uk.linkedin.com/in/steve-balmer-082b8b61";
		File local = TestUtils.getTestFile("linkedin", url);
		MetaDataJuicer mdj = new MetaDataJuicer();
		LinkedInJuicer pj = new LinkedInJuicer();
		String html = FileUtils.read(local);
		JuiceMe doc = new JuiceMe(url, html);
		
		mdj.juice(doc);
		Item item0 = doc.getMainItem();
		boolean ok = pj.juice(doc);
		
		Item item = doc.getMainItem();
		System.out.println("title: "+item.getTitle());		
		System.out.println("xid: "+item.getXId());
		System.out.println("oxid: "+item.get(AJuicer.AUTHOR_XID));
		System.out.println(item.getText());
//		System.out.println(item.getHTML());	
		System.out.println("desc: "+item.get(AJuicer.DESC));
		System.out.println("img: "+item.get(AJuicer.IMAGE_URL));
		System.out.println("link: "+item.get(AJuicer.LINK));
		System.out.println("olink: "+item.get(AJuicer.AUTHOR_URL));
		System.out.println(item.get(AJuicer.AUTHOR_NAME));
		assert item.get(AJuicer.AUTHOR_NAME).contains("Steve") : item;
		assert ! Utils.isBlank(item.getTitle());
		assert ! Utils.isBlank(item.get(AJuicer.DESC));
		assert ! Utils.isBlank(item.get(AJuicer.AUTHOR_XID));
	}

	
	
	@Test
	public void testHarrods() {		
		String url = "https://www.linkedin.com/company/harrods";
		File local = TestUtils.getTestFile("linkedin", url);
		MetaDataJuicer mdj = new MetaDataJuicer();
		LinkedInJuicer pj = new LinkedInJuicer();
		String html = FileUtils.read(local);
		JuiceMe doc = new JuiceMe(url, html);
		
		mdj.juice(doc);
		boolean ok = pj.juice(doc);
		
		Item item = doc.getMainItem();
		
		List<Item> posts = doc.getExtractedItems();
		posts.remove(item);
		for (Item item2 : posts) {
			System.out.println(item2);	
		}		
		
		System.out.println("title: "+item.getTitle());		
		System.out.println("xid: "+item.getXId());
		System.out.println("oxid: "+item.get(AJuicer.AUTHOR_XID));
		System.out.println(item.getText());
//		System.out.println(item.getHTML());	
		System.out.println("desc: "+item.get(AJuicer.DESC));
		System.out.println("img: "+item.get(AJuicer.IMAGE_URL));
		System.out.println("link: "+item.get(AJuicer.LINK));
		System.out.println("olink: "+item.get(AJuicer.AUTHOR_URL));
		System.out.println(item.get(AJuicer.AUTHOR_NAME));
		assert item.get(AJuicer.AUTHOR_NAME).contains("Harrods") : item;
		assert ! Utils.isBlank(item.getTitle());
		assert ! Utils.isBlank(item.get(AJuicer.DESC));
		assert ! Utils.isBlank(item.get(AJuicer.AUTHOR_XID));
	}
	
	@Test
	public void testLIProfileFail() {		
		String url = "https://www.linkedin.com/in/joshua-luksberg-52867230";
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
		assert item.get(AJuicer.AUTHOR_NAME).contains("Joshua") : item;
	}
	
	
//	@Test This fails 'cos group discussions are now all private, as of 2015
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
		assert item.get(AJuicer.AUTHOR_NAME).contains("Duncan") : item;

	}

}
