package com.winterwell.juice;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import winterwell.utils.Key;
import winterwell.utils.time.Time;

public class WordPressJuicerTest {

	// Title extraction tests
	
	@Test
	public void testTitleExtraction1() throws Exception {
		String htmlFilePath = "sampleWordPressPage.html";		
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("GDL-IL on Chrome Extensions And Backbone.js In The Real World");				
		}};
		
		testKeyValues(htmlFilePath, AJuicer.TITLE, expectedTags);
	}
	
	@Test
	public void testTitleExtraction2() throws Exception {
		String htmlFilePath = "sampleWordPressPage2.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("User-defined literals — Part I");
		}};
		
		testKeyValues(htmlFilePath, AJuicer.TITLE, expectedTags);
	}
	
	@Test
	public void testTitleExtractionSoDashPost() throws Exception {
		String htmlFilePath = "SoDashAI.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("How does the SoDash AI work?");		
		}};
		
		testKeyValues(htmlFilePath, AJuicer.TITLE, expectedTags);
	}
	
	/// Tag extraction tests
	
	@Test
	public void testTagsExtraction1() throws Exception {
		String htmlFilePath = "sampleWordPressPage.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("Chrome");
			add("HTML5");
			add("JavaScript");
			add("webdev");
			add("chrome");
			add("extensions");			
		}};
		
		testKeyValues(htmlFilePath, AJuicer.TAGS, expectedTags);
	}
	
	@Test
	public void testTagsExtraction2() throws Exception {
		String htmlFilePath = "sampleWordPressPage2.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("programming");
			add("C++11");
			add("correctness");			
		}};
		
		testKeyValues(htmlFilePath, AJuicer.TAGS, expectedTags);
	}
	
	@Test
	public void testTagsExtractionSoDashPost() throws Exception {
		String htmlFilePath = "SoDashAI.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("AI");
			add("Artificial Intelligence");
			add("smart software");
			add("social media analytics");
			add("text analysis");
		}};
		
		testKeyValues(htmlFilePath, AJuicer.TAGS, expectedTags);
	}
	
	/// Author name extraction test
	
	@Test
	public void testAuthorExtraction1() throws Exception {
		String htmlFilePath = "sampleWordPressPage.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("greenido");			
		}};
		
		testKeyValues(htmlFilePath, AJuicer.AUTHOR_NAME, expectedTags);
	}
	
	@Test
	public void testAuthorExtraction2() throws Exception {
		String htmlFilePath = "sampleWordPressPage2.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("Andrzej Krzemieński");
		}};
		
		testKeyValues(htmlFilePath, AJuicer.AUTHOR_NAME, expectedTags);
	}
	
	@Test
	public void testAuthorExtractionSoDashPost() throws Exception {
		String htmlFilePath = "SoDashAI.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("winterstein");			
		}};
		
		testKeyValues(htmlFilePath, AJuicer.AUTHOR_NAME, expectedTags);
	}
	
	/// Date/time extraction test
	
	@Test
	public void testDateExtraction1() throws Exception {
		String htmlFilePath = "sampleWordPressPage.html";
		
		Calendar calendar = new GregorianCalendar();
		calendar.set(2012, 10, 7, 14, 55, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		final Time expectedTime = new Time(calendar);		
		
		Set<Time> expectedTags = new HashSet<Time>() {{
			add(expectedTime);			
		}};
		
		testKeyValues(htmlFilePath, AJuicer.PUB_TIME, expectedTags);
	}
	
	@Test
	public void testDateExtraction2() throws Exception {
		String htmlFilePath = "sampleWordPressPage2.html";
		
		Calendar calendar = new GregorianCalendar();
		calendar.set(2012, 7, 12, 18, 56, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		final Time expectedTime = new Time(calendar);
		
		Set<Time> expectedTags = new HashSet<Time>() {{
			add(expectedTime);
		}};
		
		testKeyValues(htmlFilePath, AJuicer.PUB_TIME, expectedTags);
	}
	
	@Test
	public void testDateExtractionSoDashPost() throws Exception {
		String htmlFilePath = "SoDashAI.html";
		
		Calendar calendar = new GregorianCalendar();
		calendar.set(2011, 7, 5, 9, 55, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		final Time expectedTime = new Time(calendar);
		
		Set<Time> expectedTags = new HashSet<Time>() {{
			add(expectedTime);			
		}};
		
		testKeyValues(htmlFilePath, AJuicer.PUB_TIME, expectedTags);
	}
	
	/// URL to prev post extraction tests
	
	@Test
	public void testPrevPostExtraction1() throws Exception {
		String htmlFilePath = "sampleWordPressPage.html";		
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("http://greenido.wordpress.com/2012/10/31/chrome-for-enterprise/");				
		}};
		
		testKeyValues(htmlFilePath, AJuicer.PREVIOUS, expectedTags);
	}
	
	@Test
	public void testPrevPostExtraction2() throws Exception {
		String htmlFilePath = "sampleWordPressPage2.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("http://akrzemi1.wordpress.com/2012/07/12/quality-matters/");
		}};
		
		testKeyValues(htmlFilePath, AJuicer.PREVIOUS, expectedTags);
	}
	
	@Test
	public void testPrevPostExtractionSoDashPost() throws Exception {
		String htmlFilePath = "SoDashAI.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("http://www.soda.sh/static/blog/?p=33");		
		}};
		
		testKeyValues(htmlFilePath, AJuicer.PREVIOUS, expectedTags);
	}
	
	private String testDirectoryPrefix = "test/testHTMLFiles/WordPressJuicerTest/";

	/**
	 * Check if WordPressJuicer extracts correct values for a specified key.
	 * @param htmlFileName - name of the HTML file that should be parsed
	 * @param key
	 * @param expectedValues - values that should be extracted from the docuement
	 * @throws Exception
	 */
	private <X> void testKeyValues(String htmlFileName, Key<X> key, Set<X> expectedValues) throws Exception {
		String filePath = testDirectoryPrefix + htmlFileName;
		String html = TestUtils.readFile(filePath);
		
		JuiceMe document = new JuiceMe(html);		
		
		WordPressJuicer wpj = new WordPressJuicer();		
		
		List<Anno> annotations = wpj.juice(document);
		
		Set<X> extractedValues = new HashSet<>();
		
		for (Anno<X> annotation : annotations) {
			if (annotation.type.equals(key.getName())) {
				extractedValues.add(annotation.value);
			}
		}
		
		assertEquals(expectedValues, extractedValues);
		
	}
	

}
