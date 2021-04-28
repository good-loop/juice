package com.winterwell.juice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.winterwell.juice.juicers.ContactDetailsJuicer;
import com.winterwell.juice.juicers.CyclingCCJuicer;
import com.winterwell.juice.juicers.LinkedInJuicer;
import com.winterwell.juice.juicers.PhpBBJuicer;
import com.winterwell.utils.log.Log;
import com.winterwell.web.FakeBrowser;

/**
 * Extract stuff from web pages!
 * 
 * See:
 * http://tomazkovacic.com/blog/56/list-of-resources-article-text-extraction-from-html-documents/
 * 
 * @author daniel
 * @testedby  JuiceTest}
 */
public class Juice {
	
	private static final String LOGTAG = "juice";
	List<AJuicer> juicers;
	
	/**
	 * Just a test sketch of how to use this.
	 * @param args
	 */
	public static void main(String[] args) {		
		System.out.println("Juice version "+JuiceConfig.version); // TODO use config and AMain
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
		// in rough order of reliability
		this(new ArrayList<> (Arrays.asList(
			new SchemaOrgJuicer(),
			new MicroFormatJuicer(),
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
		)));
		// TODO config
	}
	
	public Juice(List<AJuicer> juicers) {
		this.juicers = juicers;
	}
	
	/**
	 * 
	 * @param juicer
	 * @param first If true, put this juicer first - you trust it for the task above the others.
	 * Juicers _typically_ only add missing annotations, accepting an earlier juicer's work as correct.
	 * Otherwise it goes last.
	 * @return
	 */
	public Juice addJuicer(AJuicer juicer, boolean first) {
		if (first) juicers.add(0, juicer); 
		else juicers.add(juicer);
		return this;
	}

	public JuiceMe juice(String url, String html) {		
		JuiceMe doc = new JuiceMe(url, html);
		for(AJuicer juicer : juicers) {
			try {
				boolean done = juicer.juice(doc);
				// Stop early?? No -- the juicer's judgement isn't reliable
				if (done) {
					// yes for some
					if (juicer instanceof PinterestJuicer) return doc;
					Log.d(LOGTAG, juicer.getClass().getSimpleName()+" says done for "+doc.getURL());
				}
			} catch(Throwable ex) {
				// allow juicers to fail without killing the juice
				Log.e(LOGTAG, ex);
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
