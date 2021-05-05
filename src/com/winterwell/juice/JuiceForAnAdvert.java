package com.winterwell.juice;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.winterwell.utils.Proc;
import com.winterwell.utils.Utils;
import com.winterwell.utils.io.FileUtils;
import com.winterwell.utils.log.Log;
import com.winterwell.utils.time.TUnit;
import com.winterwell.utils.web.WebUtils;
import com.winterwell.web.FakeBrowser;

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
 * @author daniel
 *
 */
public class JuiceForAnAdvert extends AJuicer {

	@Override
	protected boolean juice(JuiceMe doc) {
		
		Item item = doc.getMainItem();
		
		if (item.get(AJuicer.PUBLISHER_NAME)==null && item.get(AJuicer.TITLE)!=null) {
			// use the title instead
			Anno<String> title = item.getAnnotation(AJuicer.TITLE);
			Anno<String> anno = new Anno<>(AJuicer.PUBLISHER_NAME, title.value, title.src);
			item.put(anno);
		}
		
		// take a screenshot from the webpage and build a colour histogram
		File png = new File("test/screenshot.png");
		takeScreenshot(item.getUrl(), png);
		// histogram with 16 bins at each channel - increase number of bins to increase colour accuracy
		int[][][] histogram = new int[16][16][16];
		try {
			BufferedImage image = ImageIO.read(png);
			for (int x=0; x<image.getWidth(); x++) {
				for (int y=0; y<image.getHeight(); y++) {
					int rgb = image.getRGB(x, y);
					int red = (rgb >> 16) & 0x000000FF;
					int green = (rgb >> 8 ) & 0x000000FF;
					int blue = (rgb) & 0x000000FF;
					histogram[red / 16][green / 16][blue / 16]++;
				}
			}
		} catch (Exception ex) {
			Log.d("Unable to create colour histogram");
		}
		// TODO: how to deal with the colour histogram, do we want to extract a single colour pixel or a range? 
		
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

}
