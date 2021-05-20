package com.winterwell.juice;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.BoxModel;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.JSHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.Clip;
import com.winterwell.utils.Best;
import com.winterwell.utils.Printer;
import com.winterwell.utils.containers.TopNList;
import com.winterwell.utils.io.FileUtils;
import com.winterwell.utils.log.Log;
import com.winterwell.utils.web.WebUtils;

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
 * @testedby JuiceForAnAdvertTest
 */
public class JuiceForAnAdvert extends AJuicer {

	@Override
	protected boolean juice(JuiceMe doc) {
		
		Item item = doc.getMainItem();
		
		if (item.get(AJuicer.PUBLISHER_NAME)==null && item.get(AJuicer.TITLE)!=null) {
			// use the title instead
			Anno<String> title = item.getAnnotation(AJuicer.TITLE);
			String pub = title.value;
			// HACK
			String[] bits = pub.split("\\|"); // e.g. "Publisher | Page"
			pub = bits[0].trim();
			// set
			Anno<String> anno1 = new Anno<>(AJuicer.PUBLISHER_NAME, pub, title.src);
			item.put(anno1);
		}
		
		// a useful api to get the logo of websites
		String logo = "https://logo.clearbit.com/"+WebUtils.urlEncode(item.getUrl());
		item.put(anno(AJuicer.PUBLISHER_LOGO, logo, null));
//		// download the logo - do we need that? 
//		try {
//			BufferedImage websiteLogo = ImageIO.read(new URL(logo));
//			ImageIO.write(websiteLogo, "png", new File("test/logo.png"));
//		} catch (IOException ex) {
//			Log.d(LOGTAG, "Error downloading logo");
//		}
		
		// get the tagline/slogan of the website
		scrapeTagline(doc, item);
		
		Browser b = null;
		Page p = null; 
		try {
			// launch a headless browser using puppeteer
			b = Puppeteer.launch();
			p = b.newPage();
			p.goTo(item.getUrl());

			// take a screenhot of the webpage
			File png = File.createTempFile("screenshot", ".png");
			p.screenshot(png.getAbsolutePath());
			// Build a colour histogram from the webpage screenshot
			try {				
				// histogram with 16 bins at each channel - increase number of bins to increase colour accuracy
				List<String> topColours = scrapeColours(png, 128);
				item.put(anno(WEBSITE_COLOURS, topColours, null));
			} catch (IOException ex) {
				Log.e(LOGTAG, "Unable to locate screenshot..."+ex);
			}

			//get the rendered css font family 
			scrapeFont(item, p);
			
			// get images
			List<String> imgs = scrapeImages(item, p, 5);
			item.put(anno(IMAGE_URLS, imgs, null));			
			
		} catch(Exception ex) {
			Log.e(LOGTAG, "Error launching headless browser: " + ex);
		} finally {
			try {
				if (p!=null && ! p.isClosed()) p.close();
			} catch (Exception ignore) {
				Log.i(LOGTAG, "Error closing page"+ignore);	
			}
			try {
				if (b!=null) b.close();
			} catch (Exception ignore) {
				Log.i(LOGTAG, "Error closing browser "+ignore);	
			}
		}
				
		// get the call-to-actions
		scrapeCTA(doc, item);
		
		return false;
	}
	
	private void scrapeTagline(JuiceMe doc, Item item) {
		String[] tags = new String[] {"h1","h2","h3","p"};
		// Normally, the tagline is the first header to appear on the webpage (or appear after the publisher name)
		// NB: When a website doesn't have a tagline, most likely it will scrape a wrong value
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
	
	/**
	 * 
	 * @param pngFile
	 * @param bins 256 for precise colours (uses 16mb of memory) Lower e.g. 16 for approx colours.
	 * @return
	 * @throws IOException
	 */
	List<String> scrapeColours(File pngFile, int bins) throws IOException {
		BufferedImage png = ImageIO.read(pngFile);
		int pixel = 256/bins;
		int[][][] histogram = new int[bins][bins][bins];
		for (int x=0; x<png.getWidth(); x++) {
			for (int y=0; y<png.getHeight(); y++) {
				int rgb = png.getRGB(x, y);
				int red = (rgb >> 16) & 0x000000FF;
				int green = (rgb >> 8 ) & 0x000000FF;
				int blue = (rgb) & 0x000000FF;
				histogram[red/pixel][green/pixel][blue/pixel]++;
			}
		}
		
		// get the top 4 colours
		TopNList<Integer> topColours = new TopNList(4);
		for (int i=0; i<bins; i++) {
			for (int j=0; j<bins; j++) {
				for (int k=0; k<bins; k++) {			
					// new code using utility method
					int score = histogram[i][j][k];
					int colour = ((i*pixel)<<16)+((j*pixel)<<8)+(k*pixel);
					topColours.maybeAdd(colour, score);
				}
			}
		}		
		// convert to css codes
		List<String> cols = new ArrayList();
		for(Integer i : topColours) {
			Color col = new Color(i);
			String scol = WebUtils.color2html(col);
			cols.add(scol);
		}
		return cols;
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
	
	private List<String> scrapeImages(Item item, Page p, int n) {
		// collect the largest images
		TopNList<String> images = new TopNList(n);		
		List<ElementHandle> sel = p.$$("img");
		for (ElementHandle ehImg : sel) {
			JSHandle jssrc = ehImg.getProperty("src");
			if (jssrc == null) {
				continue;
			}
			String src = (String) jssrc.jsonValue();
			BoxModel bm = null;
			try {
				//Unable to compute box model for images which are rendered through user's interactions
				bm = ehImg.boxModel();
				if (bm == null) continue;
			} catch (Exception ex) {
				continue;
			}
			int w = bm.getWidth();
			int h = bm.getHeight();
			if ( w < 64 || h < 64) {
				continue;
			}
			images.maybeAdd(src, w*h);
			// TODO maybe get any classes or surrounding text as extra info?
		}
		return images;
	}
	
	private void scrapeCTA(JuiceMe doc, Item item) {
		// four main CTAs on websites: book a demo, purchase/explore products, contact us, booking and reservation
		// each CTA is associated with a key (demo, products, contact, booking), with the value being the URL 
		Elements es = doc.getDoc().getElementsByTag("a");
		Collections.reverse(es); //reverse the order as more important CTAs are usually put on top (prevent overwriting)
		HashMap<String, String> map = new HashMap<String, String>();
		for (Element e: es) {
			String action = e.text().toLowerCase();
			if (action.contains("demo")) {
				map.put("demo", e.absUrl("href"));
			} else if (action.contains("buy") || action.contains("shop") || action.contains("explore") || 
					action.contains("get started") || action.contains("start")) {
				map.put("products", e.absUrl("href"));
			} else if (action.contains("contact") || action.contains("get in touch")) {
				map.put("contact", e.absUrl("href"));
			} else if (action.contains("book") || action.contains("reserve")) {
				map.put("booking", e.absUrl("href"));
			}
		}
		if (!map.isEmpty()) {
			Anno<HashMap> ctaAnnotation = new Anno<>(AJuicer.CTA, map, null);
			item.put(ctaAnnotation);
		}
	}

}
