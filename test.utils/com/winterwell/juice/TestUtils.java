package com.winterwell.juice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.junit.Ignore;

import winterwell.utils.containers.ListMap;
import winterwell.utils.containers.Pair2;

import winterwell.utils.io.FileUtils;
import com.winterwell.web.FakeBrowser;
import winterwell.utils.StrUtils;
import winterwell.utils.Utils;

public class TestUtils {
	// Reading HTML markup from a file
	static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));

		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");

		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}

		return stringBuilder.toString();
	}

	static String testDirectoryPrefix = "test/testHTMLFiles/";
	
	public static File getTestFile(String type, String url) {
		String u = FileUtils.safeFilename(url, false);
		File f = type==null? new File(testDirectoryPrefix+u) : new File(testDirectoryPrefix+type+"/"+u);
		if (f.isFile()) return f;
		FakeBrowser fb = new FakeBrowser();
		String html = fb.getPage(url);
		f.getParentFile().mkdirs();
		FileUtils.write(f, html);
		return f;
	}

	public static ListMap<Item,String> out(List<Item> items) {
		assert items.size() > 0 : items;
		ListMap<Item, String> warnings = new ListMap();
		for (Item item : items) {
			String text = Utils.or(item.getText(), item.get(AJuicer.DESC));
			System.out.println(
					Utils.or(item.getXId(),item.getUrl())+"\t"
					+item.getTitle()
					+"\tby "+item.getAuthor()+"("+item.get(AJuicer.AUTHOR_XID)+")\t"
					+StrUtils.ellipsize(text, 140)
					);
			if (Utils.isBlank(text)) {
				warnings.add(item, "No text or desc");
			}
			if (Utils.isBlank(item.get(AJuicer.AUTHOR_XID))) {
				warnings.add(item, "No oxid");
			}			
		}
		return warnings;
	}
}
