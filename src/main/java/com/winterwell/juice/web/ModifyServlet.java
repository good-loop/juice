package com.winterwell.juice.web;

import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.winterwell.juice.JuiceMe;
import com.winterwell.utils.Dep;
import com.winterwell.utils.MathUtils;
import com.winterwell.utils.StrUtils;
import com.winterwell.utils.Utils;
import com.winterwell.utils.log.Log;
import com.winterwell.utils.web.WebUtils2;
import com.winterwell.web.FakeBrowser;
import com.winterwell.web.WebEx;
import com.winterwell.web.app.CommonFields;
import com.winterwell.web.app.IServlet;
import com.winterwell.web.app.WebRequest;
import com.winterwell.youagain.client.AuthToken;
import com.winterwell.youagain.client.YouAgainClient;

/**
 * A crude url-based template engine:
 * Take the underlying url
 * Adjust imgs, text, fonts based on url parameters
 * Return html
 * 
 * TODO This can be improved by replacing the regex with
 * parsing the html (using jsoup), then supporting xpath=newvalue
 * 
 * @author daniel
 *
 */
public class ModifyServlet implements IServlet {

	/**
	 * Copy-pasta from CrudServlet, as /modify could be used to request localhost urls
	 * 
	 * Crude security filter: reject non-Good-Loop users with an exception
	 * @param state
	 * @throws WebEx.E401
	 */
	void securityHack_teamGoodLoop(WebRequest state) throws WebEx.E401 {
		YouAgainClient yac = Dep.get(YouAgainClient.class);
		List<AuthToken> tokens = yac.getAuthTokens(state);
		for (AuthToken authToken : tokens) {
			String name = authToken.getXId().getName();
			if ( ! WebUtils2.isValidEmail(name)) {
				// app2app also, but nothing else (eg Twitter)
				if (authToken.getXId().isService("app")) {
					if (name.endsWith("good-loop.com")) {
						return;
					}
				}
				continue;
			}
			if (name.endsWith("@good-loop.com")) {
				if ( ! authToken.isVerified()) {
					// TODO verify
					Log.w(LOGTAG(), "not verified "+authToken);
				}
				// That will do for us for now
				return;
			}
			// hack: Alexander, Pete, Amanda, Emilia
			if ("alexander.scurlock@gmail.com info@frankaccounting.co.uk amanda_shields@hotmail.co.uk em@kireli.studio".contains(name)) {
				return;
			}
		}
		// TODO use YA shares to allow other emails through
		// No - sod off
		throw new WebEx.E401("This is for Team Good-Loop - Please ask for access");
	}

	
	@Override
	public void process(WebRequest state) throws Exception {
				
		securityHack_teamGoodLoop(state);		
		
		String url = state.get(CommonFields.URL);
		String html = new FakeBrowser().getPage(url);
		Element doc = new JuiceMe(url, html).getDoc();

		Map<String, String> pmap = state.getMap();
		
		Element body = doc.getElementsByTag("body").first();
		if (body==null) { // does this work to handle fragments??
			body = doc;
		}
		Element head = doc.getElementsByTag("head").first();		
		if (head==null) {
			head = doc.prepend("<head></head>");
		}		
		
		// fix width and height?
		String width = state.get("width");
		String height = state.get("height");
		if (width!=null || height != null) {
			String bstyle = body.attr("style");
			if (width!=null) bstyle += " width:"+width+(StrUtils.isNumber(width)?"px":"")+";";
			if (height!=null) bstyle += " height:"+height+(StrUtils.isNumber(height)?"px":"")+";";
			body.attr("style", bstyle.trim());
		}
		
		// transform the doc!
		for(String key : pmap.keySet()) {
			String value = pmap.get(key);
			if (Utils.isBlank(value)) {
				continue;
			}
			if ("css".equals(key)) {				
				body.prepend(value);
				continue;
			}
			if (key.startsWith("font")) {				
				// HACK define .font1 = import of a google font
				// HACK remove style tag from doc if marked up with our custom key="key" marker
				// This avoid loading unwanted fonts
				Elements styles = doc.getElementsByTag("style");
				for(Element style : styles) {
					if (key.equals(style.attr("key"))) {
						style.remove();
					}
				}
				head.append("<style>\n@import url('https://fonts.googleapis.com/css2?family="+value+"&display=swap');\n"
					+"."+key+" { font-family: '"+value+"'; }\n"
					+ "</style>");
				continue;
			}
			
			int di = key.lastIndexOf('.');
			if (di==-1) {
				continue;
			}
			// img or span (or div if you like)			
			String selector = key.substring(0, di);
			String attr = key.substring(di+1);
			Elements imgs = doc.select(selector);
			for (Element img : imgs) {
				if ("text".equals(attr)) {	// hack to set text
					img.text(value);
				} else {
					img.attr(attr, value);
				}
			}
		}
		
		// return a web page
		String modHtml = doc.outerHtml();				
		WebUtils2.sendHtml(modHtml, state.getResponse());
	}

}
