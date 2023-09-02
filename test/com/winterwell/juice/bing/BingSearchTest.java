package com.winterwell.juice.bing;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.winterwell.utils.Utils;
import com.winterwell.utils.io.CSVReader;
import com.winterwell.utils.io.CSVWriter;
import com.winterwell.utils.io.ConfigFactory;
import com.winterwell.utils.io.FileUtils;
import com.winterwell.utils.log.Log;
import com.winterwell.utils.web.WebUtils2;

public class BingSearchTest {

	

	@Test
	public void testExpandCSV() {
		ConfigFactory.get().setDebug(true);
		ConfigFactory cf = ConfigFactory.get();
		
		BingConfig bc = cf.getConfig(BingConfig.class);
		assert bc.subscriptionKey != null;
		BingSearch bs = new BingSearch();
		File infile = new File("test/com/winterwell/juice/bing/Domains-Table.urls.csv");
		File outfile = FileUtils.changeType(infile, ".urls.csv.out");
		CSVReader r = new CSVReader(infile);
		CSVWriter w = new CSVWriter(outfile);
		for (String[] row : r) {
			if (row.length > 1)  {
				w.write(row);
				continue;
			}
			try {
				String d = WebUtils2.getDomain(row[0]);
				List<Map> res = bs.search("site:"+d);
				for(int i=0; i<Math.min(res.size(), 3); i++) {
					Map resi = res.get(i);
					String url = (String) resi.get("url");
					w.write(row[0].trim(), url.trim());
				}
			} catch (Throwable ex) {
				w.write(row);
				Utils.sleep(2000);
				Log.e(ex);				
			}
		}
		w.close();
		FileUtils.move(outfile, infile);
	}

	
	@Test
	public void testSearch() {
		ConfigFactory.get().setDebug(true);
		ConfigFactory cf = ConfigFactory.get();
		
		BingConfig bc = cf.getConfig(BingConfig.class);
		assert bc.subscriptionKey != null;
		BingSearch bs = new BingSearch();
		System.out.println(bs.search("Hello World"));
	}


	@Test
	public void testSearchSite() {
		ConfigFactory.get().setDebug(true);
		ConfigFactory cf = ConfigFactory.get();
		
		BingConfig bc = cf.getConfig(BingConfig.class);
		assert bc.subscriptionKey != null;
		BingSearch bs = new BingSearch();
		bs.setDebug(true);
		List<Map> res = bs.search("site:dadsnews.com");
		Map r0 = res.get(0);
		System.out.println(r0);
		System.out.println(res);
	}
}
