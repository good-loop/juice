package com.winterwell.juice.web;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.Juice;
import com.winterwell.juice.JuiceForAnAdvert;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.juicers.SocialMediaLinksJuicer;
import com.winterwell.utils.MathUtils;
import com.winterwell.utils.StrUtils;
import com.winterwell.utils.Utils;
import com.winterwell.utils.containers.ArrayMap;
import com.winterwell.utils.containers.Containers;
import com.winterwell.utils.io.FileUtils;
import com.winterwell.utils.web.WebUtils;
import com.winterwell.utils.web.WebUtils2;
import com.winterwell.web.FakeBrowser;
import com.winterwell.web.WebPage;
import com.winterwell.web.ajax.JSend;
import com.winterwell.web.app.CommonFields;
import com.winterwell.web.app.CrudServlet;
import com.winterwell.web.app.FileServlet;
import com.winterwell.web.app.IServlet;
import com.winterwell.web.app.WebRequest;
import com.winterwell.web.app.WebRequest.KResponseType;

/**
 * For use with VideoMakerPage.jsx
 * @author daniel
 *
 */
public class XrayServlet implements IServlet {
	
	@Override
	public void process(WebRequest state) throws Exception {		
			
		String url = state.get(CommonFields.URL);
		String html = new FakeBrowser().getPage(url);
		JuiceMe doc = new JuiceMe(url, html);
		List tags = new ArrayList();
		Map analysis = new ArrayMap("tags", tags);
		
		// preferred size from viewport (really this is just for our own pages)
		Elements metaTags = doc.getDoc().getElementsByTag("meta");
		for (Element metaTag : metaTags) {
			String nameValue = metaTag.attr("name");
			if ( ! nameValue.equals("viewport")) continue;
			String content = metaTag.attr("content");
			ArrayMap vp = new ArrayMap("tag", "viewport");
			List<String> kvs = StrUtils.splitCSVStyle(content);
			for (String kv : kvs) {
				if (kv.startsWith("width=")) {
					String w = kv.substring(6);
					if (MathUtils.isNumber(w)) {
						vp.put("width", Integer.valueOf(w));
					}
				}
				if (kv.startsWith("height=")) {
					String w = kv.substring(7);
					if (MathUtils.isNumber(w)) {
						vp.put("height", Integer.valueOf(w));
					}
				}
			}
			analysis.put("size", vp);
		}
		
		Elements imgTags = doc.getDoc().getElementsByTag("img");		
		for(Element img : imgTags) {
			String imgId = img.attr("id");
			if (imgId==null) continue;			
			tags.add(new ArrayMap(
				"tag", "img",
				"id", imgId,
				"src", img.attr("src")
			));
		}
		
		Elements spanTags = doc.getDoc().getElementsByTag("span");		
		for(Element img : spanTags) {
			String imgId = img.attr("id");
			if (imgId==null) continue;	
			String text = Utils.or(img.text(), img.html());
			tags.add(new ArrayMap(
				"tag", "span",
				"id", imgId,
				"text", text
			));
		}
		
		JSend js = new JSend(analysis);
		js.send(state);
		return;
	}

}
