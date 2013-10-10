package com.winterwell.juice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * What type of web page is it?
 * 
 * TODO sniff forums
 * 
 * @author daniel
 * @testedby BlogSnifferTest
 */
public class BlogSniffer {

	public static final String WORDPRESS = "blog.wordpress";
	private static final String BLOGGER = "blog.googleblogger";
	private static final String MOVABLE_TYPE = "blog.movabletype";
	private static final String TYPEPAD = "blog.typepad";
	private static final String PINTEREST = "pinterest";
	
	/**
	 * 
	 * @param html
	 * @return known blog type, or null
	 */
	public String sniff(String html) {		
		// xpath would be better - except not-good-xml can throw it 
		Pattern GENERATOR1 = Pattern.compile(
				"<meta\\s+name=[\"']generator[\"']\\s+content=[\"'](.+?)[\"']", Pattern.CASE_INSENSITIVE);
		Pattern GENERATOR2 = Pattern.compile(
				"<meta\\s+content=[\"'](.+?)[\"']\\s+name=[\"']generator[\"']", Pattern.CASE_INSENSITIVE);
		Matcher m = GENERATOR1.matcher(html);
		if ( ! m.find()) {
			m = GENERATOR2.matcher(html);
			if ( ! m.find()) {
				return sniff2_guess(html);
			}
		}
		String gen = m.group(1).toLowerCase();
		// wordpress?
		if (gen.startsWith("wordpress")) {
			return WORDPRESS;
		}
		// blogger?
		if (gen.startsWith("blogger")) {
			return BLOGGER;
		}
		// moveable type?
		if (gen.startsWith("movable")) {
			return MOVABLE_TYPE;
		}
		if (gen.startsWith("typepad")) {
			return TYPEPAD;
		}
		if (html.contains("pinterestapp:pinboard")) {
			return PINTEREST;
		}		
		// unknown!
		return null;
	}

	private String sniff2_guess(String html) {
		if (html.contains("/wp-content/themes")) return WORDPRESS;
		return null;
	}
}
