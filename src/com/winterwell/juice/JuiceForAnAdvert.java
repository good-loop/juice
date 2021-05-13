package com.winterwell.juice;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.winterwell.utils.log.Log;

/**
 * TODO patch the gaps left by other juicers for:
 * 
 * Name
 * Tagline / slogan
 * Logo
 * Top photos
 * Brand colours
 * Fonts
 * CTA link
 * CTA "action words"
 * 
 * @author daniel, kai
 *
 */
public class JuiceForAnAdvert extends AJuicer {

	@Override
	protected boolean juice(JuiceMe doc) {
		
		Item item = doc.getMainItem();
		
		if (item.get(AJuicer.PUBLISHER_NAME)==null && item.get(AJuicer.TITLE)!=null) {
			// use the title instead
			Anno<String> title = item.getAnnotation(AJuicer.TITLE);
			Anno<String> anno1 = new Anno<>(AJuicer.PUBLISHER_NAME, title.value, title.src);
			item.put(anno1);
		}
		
		// a useful api to get the logo of websites
		String logo = "https://logo.clearbit.com/"+item.getUrl();
		item.put(anno(AJuicer.PUBLISHER_LOGO, logo, null));
		// download the logo - do we need that? 
		try {
			BufferedImage websiteLogo = ImageIO.read(new URL(logo));
			ImageIO.write(websiteLogo, "png", new File("test/logo.png"));
		} catch (IOException ex) {
			Log.d("Error downloading logo");
		}
		
		// get the tagline/slogan of the website
		scrapeTagline(doc, item);
		
		try {
			// launch a headless browser using puppeteer
			Browser b = Puppeteer.launch();
			Page p = b.newPage();
			p.goTo(item.getUrl());
			// take a screenhot of the webpage
			p.screenshot("test/screenshot.png");
			//get the rendered css font family 
			scrapeFont(item, p);
			p.close();
			b.close();
		} catch(Exception ex) {
			Log.d("Error while launching headless browser: " + ex);
		} 
		
		// Build a colour histogram from the webpage screenshot
		File png = new File("test/screenshot.png");
		try {
			BufferedImage image = ImageIO.read(png);
			// histogram with 16 bins at each channel - increase number of bins to increase colour accuracy
			scrapeColour(item, image, 16);
		} catch (IOException ex) {
			Log.d("Unable to locate screenshot...");
		}
		
		// get the call-to-actions
		scrapeCTA(doc, item);
		
		return false;
	}
	
	private void scrapeTagline(JuiceMe doc, Item item) {
		String[] tags = new String[] {"h1","h2","h3","p"};
		// Normally, the tagline is the first header to appear on the webpage (or appear after the publisher name)
		// TODO: This might wrongly scrape some headers, how do we double check?
		int i = 0; 
		Elements es = doc.getDoc().getElementsByTag(tags[i]);
		while (i<4) {
			es = doc.getDoc().getElementsByTag(tags[i]);
			if (es.isEmpty() || es.get(0).text().equalsIgnoreCase(item.get(AJuicer.PUBLISHER_NAME))) {
				i++;
				continue;
			} else {
				break;
			}
		}
		if (!es.isEmpty()) {
			Element e = es.get(0);
			item.put(anno(AJuicer.TAGLINE, e.text(), e));
		}
	}
	
	private void scrapeColour(Item item, BufferedImage image, int bins) {
		int pixel = 256/bins;
		int[][][] histogram = new int[bins][bins][bins];
		for (int x=0; x<image.getWidth(); x++) {
			for (int y=0; y<image.getHeight(); y++) {
				int rgb = image.getRGB(x, y);
				int red = (rgb >> 16) & 0x000000FF;
				int green = (rgb >> 8 ) & 0x000000FF;
				int blue = (rgb) & 0x000000FF;
				histogram[red/pixel][green/pixel][blue/pixel]++;
			}
		}
		
		// get the top 4 colours 
		// the two linkedlist are always sorted
		LinkedList<Integer> dominantColours = new LinkedList<Integer>(Arrays.asList(0,0,0,0));
		LinkedList<Integer> maxValues = new LinkedList<Integer>(Arrays.asList(0,0,0,0));
		for (int i=0; i<bins; i++) {
			for (int j=0; j<bins; j++) {
				for (int k=0; k<bins; k++) {
					if (histogram[i][j][k] > maxValues.get(0)) {
						// check which position it should enter
						for (int l=3; l>=0; l--) {
							if (histogram[i][j][k] >= maxValues.get(l)) {
								maxValues.removeFirst();
								dominantColours.removeFirst();									
								maxValues.add(l,histogram[i][j][k]);
								int colour = ((i*pixel)<<16)+((j*pixel)<<8)+(k*pixel);
								dominantColours.add(l, colour);
								break;
							}
						}
					}
				}
			}
		}
		item.put(anno(WEBSITE_COLOUR, dominantColours, null));
	}
	
	private void scrapeFont(Item item, Page p) {
		// getting the rendered fonts through the chrome dev tools api
		p.client().send("DOM.enable", null, true);
		p.client().send("CSS.enable", null, true);
		JsonNode jn = p.client().send("DOM.getDocument", null, true);
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("nodeId", jn.get("root").get("nodeId"));
		m.put("selector", "body");
		JsonNode jn2 = p.client().send("DOM.querySelector", m, true);
		Map<String, Object> m2 = new HashMap<String,Object>();
		m2.put("nodeId", jn2.get("nodeId"));
		JsonNode jn3 = p.client().send("CSS.getComputedStyleForNode", m2, true);
		
		for (JsonNode j: jn3.get("computedStyle")) {
			if (j.get("name").asText().equals("font-family")) {
				String font = j.get("value").asText();
				// annotate and save the font family, null is passed as the src as font is rendered dynamically
				Anno<String> fontAnnotation = new Anno<>(AJuicer.FONT_FAMILY, font, null);
				item.put(fontAnnotation);
				break;
			}
		}
	}
	
	private void scrapeCTA(JuiceMe doc, Item item) {
		// TODO: Improvement to suit different website structures
		Elements es = doc.getDoc().getElementsByTag("a");
		HashMap<String, String> map = new HashMap<String, String>();
		for (Element e: es) {
			String action = e.text().toLowerCase();
			if (action.contains("find") || action.contains("learn") ) {
				// insert link of the webpage
				map.put("information", e.attr("href"));
			} else if (action.contains("contact") || action.contains("demo")) {
				// insert link of the webpage
				map.put("contact", e.attr("href"));
			}
		}
		if (!map.isEmpty()) {
			Anno<HashMap> ctaAnnotation = new Anno<>(AJuicer.CTA, map, null);
			item.put(ctaAnnotation);
		}
	}

}
