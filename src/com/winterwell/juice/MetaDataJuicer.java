package com.winterwell.juice;

import java.util.List;

import org.jsoup.select.Elements;

import winterwell.utils.TodoException;


/**
 * TODO Extract info from explicit meta-data elements.
 * E.g. Facebook Open Graph (c.f. http://ogp.me/)
 * Or common <meta> tags. 
 * @author daniel
 *
 */
public class MetaDataJuicer extends AJuicer {

	@Override
	List<Anno> juice(JuiceMe doc) {
		// Is there another one in the doc html?
		// Examples:
// Guardian:
// <link rel="canonical" href="http://www.guardian.co.uk/technology/2012/feb/29/raspberry-pi-computer-sale-british" />
// <meta property="og:url" content="http://www.guardian.co.uk/technology/2012/feb/29/raspberry-pi-computer-sale-british"/>
		Elements canons = doc.doc.getElementsByAttribute("canonical");
		// rel;
		
		Elements urls = doc.doc.getElementsByAttributeValue("property", "og:url");
		// content
		
		return added(doc);
	}



}
