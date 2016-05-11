package com.winterwell.juice;

import java.util.List;

import winterwell.utils.reporting.Log;

import com.winterwell.juice.juicers.ContactDetailsJuicer;
import com.winterwell.juice.juicers.CyclingCCJuicer;
import com.winterwell.juice.juicers.LinkedInJuicer;
import com.winterwell.juice.juicers.PhpBBJuicer;
import com.winterwell.web.FakeBrowser;

/**
 * Extract stuff from web pages!
 * 
 * See:
 * http://tomazkovacic.com/blog/56/list-of-resources-article-text-extraction-from-html-documents/
 * 
 * @author daniel
 * @testedby {@link JuiceTest}
 */
public class Juice {
	
	private static final String LOGTAG = "juice";
	AJuicer[] juicers;
	
	/**
	 * Just a test sketch of how to use this.
	 * @param args
	 */
	public static void main(String[] args) {
		Juice juice = new Juice();
		if (args==null || args.length==0) {
			args = new String[]{"http://www.guardian.co.uk/technology/2012/feb/29/raspberry-pi-computer-sale-british"};
		}
		
		for (String url : args) {
			String html = new FakeBrowser().getPage(url);
			JuiceMe juiced = juice.juice(url, html);
			Item item = juiced.getExtractedItems().get(0);
			System.out.println('"'+item.getTitle()+"\" by "+item.getAuthor()+" date:"+item.getPublishedTime());
		}
	}
	
	public Juice() {
		juicers = new AJuicer[]{				
			new WordPressJuicer(),			
			new PinterestJuicer(),
			new PhpBBJuicer(),			
			new MetaDataJuicer(),
			new ContactDetailsJuicer(),
//			new GenericJuicer()	
			// Site specific hacks :(
			new LinkedInJuicer(),
			new CyclingCCJuicer(),
			// Fall back to desperation
			new CommonNamesJuicer(),			
			new DateFinder()
		};
	}
	
	public JuiceMe juice(String url, String html) {		
		JuiceMe doc = new JuiceMe(url, html);
		for(AJuicer juicer : juicers) {
			boolean done = juicer.juice(doc);
			// Stop early?? No -- the juicer's judgement isn't reliable
			if (done) {
				// yes for some
				if (juicer instanceof PinterestJuicer) return doc;
				Log.d(LOGTAG, juicer.getClass().getSimpleName()+" says done for "+doc.getURL());
			}
		}		
		// Mark stable urls
		List<Item> items = doc.getExtractedItems();
		if (items.size() > 1) {
			for (Item item : items) {
				String setUrl = item.get(AJuicer.URL);
				if (setUrl==null && item.stable1ItemUrl==null) {
					item.stable1ItemUrl = false;
				}
			}
		}
		return doc;
	}
}
