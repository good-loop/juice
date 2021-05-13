package com.winterwell.juice;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.util.ajax.JSON;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.winterwell.utils.Utils;
import com.winterwell.utils.containers.Containers;
import com.winterwell.utils.log.Log;
import com.winterwell.utils.web.WebUtils2;

/**
 * Status: half done
 * 
 * TODO read schema.org metadata
 * e.g. see 
 * view-source:https://www.scotsman.com/business/eie-2018-5-start-ups-watch-years-conference-311184
 * 
 * <script data-schema="Organization" type="application/ld+json">
 * {"@type":"Organization","@context":"https://schema.org",
 * "name":"The Scotsman",
 * "url":"https://www.scotsman.com",
 * "logo":{"@type":"ImageObject","url":"/img/logo.png","width":600,"height":121},
 * "sameAs":["https://twitter.com/TheScotsman","https://www.facebook.com/TheScotsmanNewspaper"]}</script>
 * <script type="application/ld+json">{"@context":"http://schema.org","@type":"NewsArticle",
 * "mainEntityOfPage":{"@type":"WebPage","@id":"https://www.scotsman.com/business/eie-2018-5-start-ups-watch-years-conference-311184"},
 * "headline":"EIE 2018: 5 start-ups to watch at this year's conference",
 * "datePublished":"2018-03-27T17:35:56.000Z","dateModified":"2018-03-27T17:58:49.000Z",
 * "description":"Investors will soon arrive in Edinburgh hoping to spot the next FanDuel or Skyscanner, writes Nick Freer",
 * "keywords":["economic sector","regions","software","innovators","scotland","research","places","discovery and innovation","science and technology","futurescotland;","computing and information technology","edinburgh, fife and lothians","economy, business and finance"],
 * "thumbnailUrl":"https://www.scotsman.com/images-e.jpimedia.uk/imagefetch/http://www.scotsman.com/webimage/Prestige.Item.1.64336114!image/image.jpg","articleSection":"Business","url":"https://www.scotsman.com/business/eie-2018-5-start-ups-watch-years-conference-311184",
 * "publisher":{"@type":"Organization","name":"The Scotsman","url":"https://www.scotsman.com",
 * "logo":{"@type":"ImageObject","url":"/img/logo.png","width":600,"height":121}},
 * "image":"https://www.scotsman.com/images-e.jpimedia.uk/imagefetch/http://www.scotsman.com/webimage/Prestige.Item.1.64336114!image/image.jpg",
 * "author":{"@type":"Person","name":"By The Newsroom"}}</script>
 * <script type="application/ld+json">{"@context":"https://schema.org","@type":"BreadcrumbList",
 * "itemListElement":[{"@type":"ListItem","position":1,"name":"Business","item":"https://www.scotsman.com/business"}]}</script>
 * 
 * or bbc
 * <script type="application/ld+json">
         {"@context":"http://schema.org","@type":"WebPage",
         "description":"Visit BBC News for up-to-the-minute news, breaking news, video, audio and feature stories. BBC News provides trusted World and UK news as well as local and regional perspectives. Also entertainment, business, science, technology and health news.",
         "url":"https://www.bbc.co.uk/news",
         "mainEntityOfPage":"https://www.bbc.co.uk/news",
         "publisher":{"@type":"NewsMediaOrganization","name":"BBC News",
         	"logo":"//m.files.bbci.co.uk/modules/bbc-morph-news-waf-page-meta/2.6.3/bbc_news_logo.png",
         	"publishingPrinciples":"https://www.bbc.co.uk/news/help-41670342"},
         "name":"Home - BBC News"}
        </script>
 * @author daniel
 *
 *
 */
public class SchemaOrgJuicer extends AJuicer {


	@Override
	protected boolean juice(JuiceMe doc) {
		List<Map> jsons = getSchemaOrgJson(doc.getDoc());
		// url - nah, can be top-level for the site not the article
		Item item = doc.getMainItem(); // TODO support multiple items
		for (Map map : jsons) {
			String type = (String) map.get("@type");
			// publisher
			if ("publisher".equals(type)) {
				juicePublisher(map, doc, item);
			} else {
				Object pub = map.get("publisher");
				if (pub!=null) {
					if (pub instanceof Map) {
						juicePublisher((Map) pub, doc, item);
					} else if (pub instanceof String) {
						Anno<String> anno = new Anno<>(PUBLISHER_NAME, (String)pub, getSrcElement(map));
						item.put(anno);
					}
				}
			}
			// author
			if ("author".equals(type)) {
				juiceAuthor(map, doc, item);
			} else {
				Object pub = map.get("author");
				if (pub instanceof Map) {
					juiceAuthor((Map) pub, doc, item);
				} else if (pub instanceof String) {
					Anno<String> anno = new Anno<>(AUTHOR_NAME, (String)pub, getSrcElement(map));
					item.put(anno);
				}				
			}
		}
		// TODO author etc
		return false;
	}

	private void juiceAuthor(Map map, JuiceMe doc, Item item) {
		String n = (String) map.get("name");
		if (n!=null) {
			Anno anno = new Anno(AUTHOR_NAME, n, getSrcElement(map));
			item.put(anno);
		}
	}

	private void juicePublisher(Map map, JuiceMe doc, Item item) {
		String n = (String) map.get("name");
		if (n!=null) {
			Anno<String> anno = new Anno<>(PUBLISHER_NAME, n, getSrcElement(map));
			item.put(anno);
		}
		// logo
		Object l = map.get("logo");
		if (l!=null) {
			juiceLogo(doc, item, l, getSrcElement(map));
		}
	}

	private void juiceLogo(JuiceMe doc, Item item, Object l, Element src) {
		if (l instanceof Map) {
			l = ((Map) l).get("url");
		}	
		if (l instanceof String) {
			String logo = (String) l;
			// relative url?				
			URI abslogo = WebUtils2.resolveUri(doc.getURL().toString(), logo);
			Anno<String> anno = new Anno<>(PUBLISHER_LOGO, abslogo.toString(), src);
			item.put(anno);	
		}		
	}

	private Element getSrcElement(Map map) {
		return (Element) map.get("_element");
	}

	private List<Map> getSchemaOrgJson(Element doc) {
		ArrayList<Map> jsons = new ArrayList();
		Elements scripts = doc.getElementsByTag("script");
		for (Element element : scripts) {
			try {
				String typ = element.attr("type");
				if ( ! "application/ld+json".equals(typ)) continue;
				String txt = Utils.or(element.text(), element.html());
				Object json = WebUtils2.parseJSON(txt);
				if (json instanceof Map) {
					((Map)json).put("_element", element); // hack - see getSrcElement()
					jsons.add((Map)json);
				} else if (json instanceof List || json.getClass().isArray()) {
					List<Map> jlist = Containers.asList(json);
					jsons.addAll(jlist);
				}
			} catch(Exception ex) {
				Log.w(LOGTAG, ex);
			}
		}
		// TODO micro formats - and others??
		return jsons;
	}

}
