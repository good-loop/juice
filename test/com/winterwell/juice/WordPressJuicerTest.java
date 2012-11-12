package com.winterwell.juice;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
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
		String htmlFileName = "sampleWordPressPage.html";		
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("GDL-IL on Chrome Extensions And Backbone.js In The Real World");				
		}};
		
		testKeyValues(htmlFileName, AJuicer.TITLE, expectedTags);
	}
	
	@Test
	public void testTitleExtraction2() throws Exception {
		String htmlFileName = "sampleWordPressPage2.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("Constant initialization");
		}};
		
		testKeyValues(htmlFileName, AJuicer.TITLE, expectedTags);
	}
	
	@Test
	public void testTitleExtractionSoDashPost() throws Exception {
		String htmlFileName = "SoDashAI.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("How does the SoDash AI work?");		
		}};
		
		testKeyValues(htmlFileName, AJuicer.TITLE, expectedTags);
	}
	
	/// Tag extraction tests
	
	@Test
	public void testTagsExtraction1() throws Exception {
		String htmlFileName = "sampleWordPressPage.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("Chrome");
			add("HTML5");
			add("JavaScript");
			add("webdev");
			add("chrome");
			add("extensions");			
		}};
		
		testKeyValues(htmlFileName, AJuicer.TAGS, expectedTags);
	}
	
	@Test
	public void testTagsExtraction2() throws Exception {
		String htmlFileName = "sampleWordPressPage2.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("programming");
			add("C++11");
			add("correctness");			
		}};
		
		testKeyValues(htmlFileName, AJuicer.TAGS, expectedTags);
	}
	
	@Test
	public void testTagsExtractionSoDashPost() throws Exception {
		String htmlFileName = "SoDashAI.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("AI");
			add("Artificial Intelligence");
			add("smart software");
			add("social media analytics");
			add("text analysis");
		}};
		
		testKeyValues(htmlFileName, AJuicer.TAGS, expectedTags);
	}
	
	/// Author name extraction test
	
	@Test
	public void testAuthorExtraction1() throws Exception {
		String htmlFileName = "sampleWordPressPage.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("greenido");			
		}};
		
		testKeyValues(htmlFileName, AJuicer.AUTHOR_NAME, expectedTags);
	}
	
	@Test
	public void testAuthorExtraction2() throws Exception {
		String htmlFileName = "sampleWordPressPage2.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("Andrzej Krzemieński");
		}};
		
		testKeyValues(htmlFileName, AJuicer.AUTHOR_NAME, expectedTags);
	}
	
	@Test
	public void testAuthorExtractionSoDashPost() throws Exception {
		String htmlFileName = "SoDashAI.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("winterstein");			
		}};
		
		testKeyValues(htmlFileName, AJuicer.AUTHOR_NAME, expectedTags);
	}
	
	/// Date/time extraction test
	
	@Test
	public void testDateExtraction1() throws Exception {
		String htmlFileName = "sampleWordPressPage.html";
		
		Calendar calendar = new GregorianCalendar();
		calendar.set(2012, 10, 7, 14, 55, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		final Time expectedTime = new Time(calendar);		
		
		Set<Time> expectedTags = new HashSet<Time>() {{
			add(expectedTime);			
		}};
		
		testKeyValues(htmlFileName, AJuicer.PUB_TIME, expectedTags);
	}
	
	@Test
	public void testDateExtraction2() throws Exception {
		String htmlFileName = "sampleWordPressPage2.html";
		
		Calendar calendar = new GregorianCalendar();
		calendar.set(2012, 4, 27, 19, 55, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		final Time expectedTime = new Time(calendar);
		
		Set<Time> expectedTags = new HashSet<Time>() {{
			add(expectedTime);
		}};
		
		testKeyValues(htmlFileName, AJuicer.PUB_TIME, expectedTags);
	}
	
	@Test
	public void testDateExtractionSoDashPost() throws Exception {
		String htmlFileName = "SoDashAI.html";
		
		Calendar calendar = new GregorianCalendar();
		calendar.set(2011, 7, 5, 9, 55, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		final Time expectedTime = new Time(calendar);
		
		Set<Time> expectedTags = new HashSet<Time>() {{
			add(expectedTime);			
		}};
		
		testKeyValues(htmlFileName, AJuicer.PUB_TIME, expectedTags);
	}
	
	/// URL to prev post extraction tests
	
	@Test
	public void testPrevPostExtraction1() throws Exception {
		String htmlFileName = "sampleWordPressPage.html";		
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("http://greenido.wordpress.com/2012/10/31/chrome-for-enterprise/");				
		}};
		
		testKeyValues(htmlFileName, AJuicer.PREVIOUS, expectedTags);
	}
	
	@Test
	public void testPrevPostExtraction2() throws Exception {
		String htmlFileName = "sampleWordPressPage2.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("http://akrzemi1.wordpress.com/2012/04/23/using-pods-in-c11/");
		}};
		
		testKeyValues(htmlFileName, AJuicer.PREVIOUS, expectedTags);
	}
	
	@Test
	public void testPrevPostExtractionSoDashPost() throws Exception {
		String htmlFileName = "SoDashAI.html";
		
		Set<String> expectedTags = new HashSet<String>() {{
			add("http://www.soda.sh/static/blog/?p=33");		
		}};
		
		testKeyValues(htmlFileName, AJuicer.PREVIOUS, expectedTags);
	}
	
	/// Test comments extraction
		
	
	@Test
	public void testCommentsExtraction1() throws Exception {
		String htmlFileName = "sampleWordPressPage.html";
		List<Item> comments = extractComments(htmlFileName);
		
		assertEquals(comments.size(), 1);
		
		Item comment = comments.get(0);
		
		String commentText = (String) comment.getAnnotations(AJuicer.POST_BODY).get(0).value;
		Time postTime = (Time) comment.getAnnotations(AJuicer.PUB_TIME).get(0).value;
		String commentAuthor = (String) comment.getAnnotations(AJuicer.AUTHOR_NAME).get(0).value;
		
	    String expectedCommentText = "Ron Reiter rocks!";
		Time expectedPostTime = new Time(new GregorianCalendar(2012, 10, 7, 15, 11, 0));
		String expectedAuthorName = "Omri";
		
		assertEquals(expectedCommentText, commentText);
		assertEquals(expectedPostTime, postTime);
		assertEquals(expectedAuthorName, commentAuthor);		
		
	}
	
	@Test
	// Test if previous comment was extracted correctly
	public void testCommentsRelations() throws Exception {
		String htmlFileName = "sampleWordPressPage2.html";
		List<Item> comments = extractComments(htmlFileName);

		for (Item comment : comments) {
			String urlValue = comment.getSingleAnnotation(AJuicer.URL).value;
			Anno<String> anno = comment.getSingleAnnotation(AJuicer.PREVIOUS);
			
			if (anno == null) {
				System.out.println(urlValue + " -> None" );
			} else {
				System.out.println(urlValue + " ->" + anno.value);
			}
		}
		
		assertEquals(9, comments.size());
		
		checkReplyRelations(comments,
				"http://akrzemi1.wordpress.com/2012/05/27/constant-initialization/#comment-364",
				"http://akrzemi1.wordpress.com/2012/05/27/constant-initialization/#comment-365");
		
		checkReplyRelations(comments,
				"http://akrzemi1.wordpress.com/2012/05/27/constant-initialization/#comment-365",
				"http://akrzemi1.wordpress.com/2012/05/27/constant-initialization/#comment-366");
		
		checkReplyRelations(comments,
				"http://akrzemi1.wordpress.com/2012/05/27/constant-initialization/#comment-367",
				"http://akrzemi1.wordpress.com/2012/05/27/constant-initialization/#comment-368");
		
		checkReplyRelations(comments,
				"http://akrzemi1.wordpress.com/2012/05/27/constant-initialization/#comment-439",
				"http://akrzemi1.wordpress.com/2012/05/27/constant-initialization/#comment-441");
		
		checkReplyRelations(comments,
				"http://akrzemi1.wordpress.com/2012/05/27/constant-initialization/#comment-508",
				"http://akrzemi1.wordpress.com/2012/05/27/constant-initialization/#comment-514");
	}	
	
	@Test
	public void testCommentsExtractionSoDash() throws Exception {
		String htmlFileName = "SoDashAI.html";
		List<Item> comments = extractComments(htmlFileName);
		
		assertEquals(0, comments.size());
		
	}

	private List<Item> extractComments(String htmlFileName) throws IOException {
		String filePath = testDirectoryPrefix + htmlFileName;
		
		String html = TestUtils.readFile(filePath);
		
		JuiceMe document = new JuiceMe(html);		
		WordPressJuicer wpj = new WordPressJuicer();		
		wpj.juice(document);
		
		List<Item> comments = document.getItemsOfType(KMsgType.COMMENT);
		return comments;
	}
	
	
	private String testDirectoryPrefix = "test/testHTMLFiles/WordPressJuicerTest/";

	/**
	 * Check if WordPressJuicer extracts correct values for a specified key.
	 * @param htmlFileName - name of the HTML file that should be parsed
	 * @param key
	 * @param expectedValues - values that should be extracted from the document
	 * @throws Exception
	 */
	private <X> void testKeyValues(String htmlFileName, Key<X> key, Set<X> expectedValues) throws Exception {
		String filePath = testDirectoryPrefix + htmlFileName;
		String html = TestUtils.readFile(filePath);
		
		JuiceMe document = new JuiceMe(html);		
		
		WordPressJuicer wpj = new WordPressJuicer();		
		
		wpj.juice(document);
		
		List<Item> posts = document.getItemsOfType(KMsgType.POST);
		
		List<Anno> annotations = posts.get(0).getAnnotations(); 
		
		Set<X> extractedValues = new HashSet();
		
		for (Anno<X> annotation : annotations) {
			if (annotation.name.equals(key.getName())) {
				extractedValues.add(annotation.value);
			}
		}
		
		assertEquals(expectedValues, extractedValues);
		
	}
	
	/**
	 * Check if reply to a comment was correctly extracted.
	 * @param comments - list of comments extracted from post
	 * @param expectedPreviousURL - expected URL of a previous comment
	 * @param replyURL - URL of a reply comment
	 */
	private void checkReplyRelations(List<Item> comments, String expectedPreviousURL,
			String replyURL) {
		for (Item comment : comments) {
			String commentURL = (String) comment.getSingleAnnotation(AJuicer.URL).value;
			Anno previousURLAnno = comment.getSingleAnnotation(AJuicer.PREVIOUS);
			
			if (previousURLAnno == null) {
				continue;
			}
			
			String previousURL = (String) previousURLAnno.value;
			
			if (commentURL.equals(replyURL)) {
				assertEquals(expectedPreviousURL, previousURL);
				break;
			}
		}
		
	}
	
}
