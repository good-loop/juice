package com.winterwell.juice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import winterwell.utils.Key;
import winterwell.utils.io.FileUtils;
import winterwell.utils.time.Time;

public class WordPressJuicerTest {

	@Test
	public void testBugFeb2013() {
		String url = "http://carlwanders.wordpress.com/2010/07/06/skyscanner-net-features-and-review/";
		File f = TestUtils.getTestFile("wordpress", url);

		String html = FileUtils.read(f);
		
		JuiceMe document = new JuiceMe(url, html);		
		
		WordPressJuicer wpj = new WordPressJuicer();
		boolean done = wpj.juice(document);
		
		System.out.println(document);
	}
	
	/// Check if WordPress juicer can extract multiple pages
	
	@Test
	public void testBlogHomePagePostsNames() throws Exception {
		String htmlFileName = "CppBlog.html";		
				
		String filePath = testDirectoryPrefix + htmlFileName;
		String html = TestUtils.readFile(filePath);
		
		JuiceMe document = new JuiceMe(html);		
		
		WordPressJuicer wpj = new WordPressJuicer();
		wpj.juice(document);
		
		// Tags of the first post
		final ArrayList<String> post1Tags = new ArrayList<String>() {{
			add("programming");
			add("C++11");
			add("correctness");
			add("exception handling");
			add("resource handling");
		}};
		
		// Values of annotations for the first post
		final HashMap<Key, Object> post1 = new HashMap<Key, Object>() {{
			put(AJuicer.TITLE, "(Not) using std::thread");
			put(AJuicer.PUB_TIME, createTime(2012, 10, 14, 22, 37));
			put(AJuicer.TAGS, post1Tags);
		}};
		
		// Tags for the second post
		final ArrayList<String> post2Tags = new ArrayList<String>() {{
			add("programming");
			add("C++11");
			add("correctness");
		}};
		
		// Values of annotations for the second post
		final HashMap<Key, Object> post2 = new HashMap<Key, Object>() {{
			put(AJuicer.TITLE, "User-defined literals — Part III");
			put(AJuicer.PUB_TIME, createTime(2012, 9, 29, 21, 6));
			put(AJuicer.TAGS, post2Tags);
		}};
		
		// Tags of the third post
		final ArrayList<String> post3Tags = new ArrayList<String>() {{
			add("programming");
			add("C++11");
			add("correctness");
		}};
		
		// Values of annotations for the third post
		final HashMap<Key, Object> post3 = new HashMap<Key, Object>() {{
			put(AJuicer.TITLE, "User-defined literals — Part II");
			put(AJuicer.PUB_TIME, createTime(2012, 9, 23, 8, 51));
			put(AJuicer.TAGS, post3Tags);
		}};
		
		// Store post annotation values
		ArrayList<HashMap<Key, Object>> expected = new ArrayList<HashMap<Key, Object>>() {{
			add(post1);
			add(post2);
			add(post3);
		}};
		
		for (HashMap<Key, Object> post : expected) {
			// Get post with the required name
			String postTitle = (String) post.get(AJuicer.TITLE);
			Item resultPost = getPostWithTitle(postTitle, document);
			
			// Compare annotation values
			for (Key key : post.keySet()) {
				Object expectedValue = post.get(key);
				Object extractedValue = resultPost.getAnnotation(key).value;
				
				assertEquals(expectedValue, extractedValue);
			}
		}
	}
	
	private Item getPostWithTitle(String expectedTitle, JuiceMe document) {
		Collection<Item> posts = document.getItemsOfType(KMsgType.POST);
		
		for (Item post : posts) {
			String title = post.getAnnotation(AJuicer.TITLE).value;
			if (expectedTitle.equals(title)) {
				return post;
			}
		}
		
		assertFalse("No post found with title: " + expectedTitle, true);
		return null;
	}

	private Object createTime(int year, int month, int day, int hour, int minute) {
		Calendar calendar = new GregorianCalendar();
		calendar.set(year, month, day, hour, minute, 0);
		calendar.set(Calendar.MILLISECOND, 0);
				
		return new Time(calendar);
	}
	
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
		
		final ArrayList<String> tags = new ArrayList<String>() {{
			 add("Chrome");
             add("HTML5");
             add("JavaScript");
             add("webdev");
             add("chrome");
             add("extensions");
		}};
		
		Set<List<String>> expectedTags = new HashSet<List<String>>() {{
			add(tags);				
		}};
		
		testKeyValues(htmlFileName, AJuicer.TAGS, expectedTags);
	}
	
	@Test
	public void testTagsExtraction2() throws Exception {
		String htmlFileName = "sampleWordPressPage2.html";
		
		final ArrayList<String> tags = new ArrayList<String>() {{
			add("programming");
			add("C++11");
			add("correctness");
		}};
		
		Set<List<String>> expectedTags = new HashSet<List<String>>() {{
			add(tags);		
		}};
		
		testKeyValues(htmlFileName, AJuicer.TAGS, expectedTags);
	}
	
	@Test
	public void testTagsExtractionSoDashPost() throws Exception {
		String htmlFileName = "SoDashAI.html";
		
		final ArrayList<String> tags = new ArrayList<String>() {{
			add("AI");
			add("Artificial Intelligence");
			add("smart software");
			add("social media analytics");
			add("text analysis");
		}};
		
		Set<List<String>> expectedTags = new HashSet<List<String>>() {{
			add(tags);
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
	
	/// Test comments extraction		
	
	@Test
	public void testCommentsExtraction1() throws Exception {
		String htmlFileName = "sampleWordPressPage.html";
		List<Item> comments = extractComments(htmlFileName);
		
		assertEquals(1, comments.size());
		
		Item comment = comments.get(0);
		
		String commentText = (String) comment.getAnnotation(AJuicer.POST_BODY).value;
		Time postTime = (Time) comment.getAnnotation(AJuicer.PUB_TIME).value;
		String commentAuthor = (String) comment.getAnnotation(AJuicer.AUTHOR_NAME).value;
		
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
			String urlValue = comment.getAnnotation(AJuicer.URL).value;
			Anno<String> anno = comment.getAnnotation(AJuicer.PREVIOUS);
			
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
		
		Collection<Anno> annotations = posts.get(0).getAnnotations();
		
		Set<X> extractedValues = new HashSet();
		
		for (Anno<X> annotation : annotations) {
			if (annotation.name.equals(key)) {
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
			String commentURL = (String) comment.getAnnotation(AJuicer.URL).value;
			Anno previousURLAnno = comment.getAnnotation(AJuicer.PREVIOUS);
			
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
