package com.winterwell.juice;
//package com.winterwell.juice;
//
//import java.util.Collections;
//import java.util.List;
//
//
//import com.gravity.goose.Article;
//import com.gravity.goose.Configuration;
//import com.gravity.goose.Goose;
//import com.gravity.goose.extractors.PublishDateExtractor;
//
//import com.winterwell.utils.TodoException;
//import com.winterwell.utils.time.Time;
//
///**
// * @deprecated Initial tests are not impressive.
// * 
// * Use the Goose library. 
// * @author daniel
// * @testedby {@link GooseJuicerTest}
// */
//public class GooseJuicer extends AJuicer {	
//
//	@Override
//	void juice(JuiceMe doc) {
//	 Configuration configuration = new Configuration();
//	    configuration.setMinBytesForImages(4500);
//	    configuration.setLocalStoragePath("/tmp/goose");
//	    configuration.setEnableImageFetching(false); // i don't care about the image, just want text, this is much faster!
//	    configuration.setImagemagickConvertPath("/opt/local/bin/convert");
//		Goose goose = new Goose(configuration);		
//		Article article = goose.extractContent(doc.getURL(), doc.getHTML());
////		System.out.println(article);
//		
//		Item item = new Item(doc.getDoc());
//		
//		
//		String cleanText = article.cleanedArticleText();
//		item.put(anno(POST_BODY, cleanText, null));
//		
//		String title = article.title();
//		item.putIfAbsent(anno(TITLE, title, null));
//		
//		String uri = article.canonicalLink();
//		item.put(anno(URL, uri, null));
//		
//		String desc = article.metaDescription();
//		item.putIfAbsent(anno(DESC, desc, null));
//		
//		if (article.publishDate()!=null) {
//			item.putIfAbsent(anno(PUB_TIME, new Time(article.publishDate()), null));
//		}
//	}
//
//	
//	
//	
//	
//}
