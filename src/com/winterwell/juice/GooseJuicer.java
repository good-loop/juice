package com.winterwell.juice;

import java.util.Collections;
import java.util.List;


import com.gravity.goose.Article;
import com.gravity.goose.Configuration;
import com.gravity.goose.Goose;
import com.gravity.goose.extractors.PublishDateExtractor;

import winterwell.utils.TodoException;
import winterwell.utils.time.Time;

/**
 * @deprecated Initial tests are not impressive.
 * 
 * Use the Goose library. 
 * @author daniel
 * @testedby {@link GooseJuicerTest}
 */
public class GooseJuicer extends AJuicer {	

	@Override
	void juice(JuiceMe doc) {
	 Configuration configuration = new Configuration();
	    configuration.setMinBytesForImages(4500);
	    configuration.setLocalStoragePath("/tmp/goose");
	    configuration.setEnableImageFetching(false); // i don't care about the image, just want text, this is much faster!
	    configuration.setImagemagickConvertPath("/opt/local/bin/convert");
		Goose goose = new Goose(configuration);		
		Article article = goose.extractContent(doc.getURL(), doc.getHTML());
//		System.out.println(article);
		
		String cleanText = article.cleanedArticleText();
		doc.put(POST_BODY, cleanText);
		
		String title = article.title();
		doc.putIfAbsent(TITLE, title);
		
		String uri = article.canonicalLink();
		doc.put(URL, uri);
		
		String desc = article.metaDescription();
		doc.putIfAbsent(DESC, desc);
		
		if (article.publishDate()!=null) {
			doc.putIfAbsent(PUB_TIME, new Time(article.publishDate()));
		}
	}

	
	
	
	
}
