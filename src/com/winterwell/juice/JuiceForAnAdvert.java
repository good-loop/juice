package com.winterwell.juice;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.page.Page;
import com.winterwell.utils.Proc;
import com.winterwell.utils.Utils;
import com.winterwell.utils.io.FileUtils;
import com.winterwell.utils.log.Log;
import com.winterwell.utils.time.TUnit;
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
		
		if (item.get(AJuicer.PUBLISHER_LOGO)==null && item.get(AJuicer.IMAGE_URL)!=null) {
			// in most cases, the image URL is the publisher logo
			// but is there a way to double check??
			Anno<String> logo = item.getAnnotation(AJuicer.IMAGE_URL);
			Anno<String> anno2 = new Anno<>(AJuicer.PUBLISHER_LOGO, logo.value, logo.src);
			item.put(anno2);
		}
		
		// take a screenshot from the webpage and build a colour histogram
		File png = new File("test/screenshot.png");
		takeScreenshot(item.getUrl(), png);
		try {
			BufferedImage image = ImageIO.read(png);
			// histogram with 16 bins at each channel - increase number of bins to increase colour accuracy
			scrapeColour(item, image, 16);
		} catch (IOException ex) {
			Log.d("Unable to locate screenshot...");
		}
		
		// Use a chrome headless browser to get the rendered CSS font family
		try {
			scrapeFont(item);
		} catch (Exception ex) {
			Log.d("Error while using Puppeteer client");
		}
			
		return false;
	}
	
	private static void takeScreenshot(String url, File pngOut) {
		File temp1 = null;
		try {
			temp1 = File.createTempFile("chart", ".pdf", new File("test/"));
			Proc p1 = WebUtils.renderUrlToPdf_usingChrome(url, temp1, "--include-background");
			p1.waitFor(TUnit.MINUTE.dt);
			assert temp1.exists() && temp1.length() > 0;

			// Render, trim and convert to PNG with convert
			// Only get the first page
			String cmd = "convert -trim -antialias -density 300 "
					+ temp1.getAbsolutePath() + "[0] " + pngOut.getAbsolutePath();
			Log.d(cmd);
			try (Proc p2 = new Proc(cmd)) {
				p2.start();
				p2.waitFor(TUnit.MINUTE.getMillisecs());		
			
				if ( ! pngOut.exists()) {
					throw new IOException("Failed to create " + pngOut + "\t"
							+ p2.getError());
				}
			}		
		} catch (Exception e) {
			throw Utils.runtime(e);
		} finally {
			// clean up
			if (temp1 != null) {
				FileUtils.delete(temp1);
			}
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
	
	private void scrapeFont(Item item) throws Exception {
		Browser b = Puppeteer.launch();
		Page p = b.newPage();
		p.goTo(item.getUrl());
		
		// getting the rendered fonts through the chrome dev tools api
		p.client().send("DOM.enable", null, true);
		p.client().send("CSS.enable", null, true);
		JsonNode jn = p.client().send("DOM.getDocument", null, true);
		Map<String, Object> m = new HashMap<String,Object>();
		m.put("nodeId", jn.get("root").get("nodeId"));
		m.put("selector", "h1");
		JsonNode jn2 = p.client().send("DOM.querySelector", m, true);
		Map<String, Object> m2 = new HashMap<String,Object>();
		m2.put("nodeId", jn2.get("nodeId"));
		JsonNode jn3 = p.client().send("CSS.getPlatformFontsForNode", m2, true);
		String font = jn3.get("fonts").get(0).get("familyName").asText();
		
		// annotate and save the font family, null is passed as the src as font is rendered dynamically
		Anno<String> fontAnnotation = new Anno<>(AJuicer.FONT_FAMILY, font, null);
		item.put(fontAnnotation);
	}

}
