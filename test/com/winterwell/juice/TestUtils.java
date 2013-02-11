package com.winterwell.juice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Ignore;

import winterwell.utils.io.FileUtils;
import winterwell.web.FakeBrowser;

@Ignore
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
}
