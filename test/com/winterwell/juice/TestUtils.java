package com.winterwell.juice;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Ignore;

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
}
