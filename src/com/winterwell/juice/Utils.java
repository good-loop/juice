package com.winterwell.juice;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * Common methods for several juicers.
 * 
 * @author ivan
 *
 */
public class Utils {
	
	private Utils() {}
	
	/**
	 * Extract first paragraph from a text
	 * 
	 * @param pElement root element
	 * @return First paragraph of a text.
	 */
	public static String extractFirstParagraph(Element pElement) {
		
		
		StringBuilder sb = new StringBuilder();
		
		for (Node child : pElement.childNodes()) {
			// Stop at line break tag
			if (child.nodeName().equals("br")) {
				break;
			}
			
			if (child instanceof TextNode) {
				sb.append(((TextNode) child).text());
				sb.append(" ");
			}
			
		}
		
		return sb.toString();
	}

	/**
	 * Get element with a first paragraph 
	 * 
	 * @param element - root of a tree where search is conducted
	 * @return <p> element with a first paragraph
	 */
	public static Element getFirstParagraphElement(Element element) {
		return element.getElementsByTag("p").first();
	}
}
