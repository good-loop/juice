package com.winterwell.juice.web;

import java.util.Map;

import com.winterwell.juice.Item;
import com.winterwell.juice.Juice;
import com.winterwell.juice.JuiceConfig;
import com.winterwell.juice.JuiceMe;
import com.winterwell.juice.juicers.JuiceForAnAdvert;
import com.winterwell.juice.juicers.SocialMediaLinksJuicer;
import com.winterwell.utils.containers.Containers;
import com.winterwell.web.FakeBrowser;
import com.winterwell.web.ajax.JSend;
import com.winterwell.web.app.CommonFields;
import com.winterwell.web.app.IServlet;
import com.winterwell.web.app.WebRequest;
import com.winterwell.web.fields.UrlField;

public class JuiceServlet implements IServlet {

	@Override
	public void process(WebRequest state) throws Exception {
		Juice juice = new Juice();
		juice.addJuicer(new JuiceForAnAdvert(), false);
		juice.addJuicer(new SocialMediaLinksJuicer(), false);
		String url = state.get(CommonFields.URL);
		String html = new FakeBrowser().getPage(url);
		JuiceMe juiced = juice.juice(url, html);
		Item item = juiced.getExtractedItems().get(0);
		System.out.println('"'+item.getTitle()+"\" by "+item.getAuthor()+" date:"+item.getPublishedTime());
		Map<String, Object> imap = Containers.getMap(item);
		JSend jsend =  new JSend();
		jsend.setData(imap);
		jsend.send(state);
	}

}
