package com.winterwell.juice.juicers;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.winterwell.juice.AJuicer;
import com.winterwell.juice.JuiceMe;

/**
 * TODO Tumblr and Tumblogs
 * @author Daniel
 *
 */
public class TumblrJuicer extends AJuicer {

	@Override
	protected boolean juice(JuiceMe doc) {
		// Is it a tumblelog??
		if ( ! doc.getURL().contains("tumblr.com")) return false;
		
		// Meta-data: meta-data juicer
		
		// Pick out items
		Elements posts = doc.getDoc().getElementsByClass("post");
		for (Element element : posts) {
			String id = element.id();
			
		}
//		<li id="post64683119271" class="post">
//		<div class="meta">
//		<center><h2>
//		<a href="http://filmtodigital.tumblr.com/post/64683119271/autumn-colors-photo-by-daniel-sorine" title="link to this post">21 Oct</a>
//		</h2></center>
//		    </div><br>
//		    <div class="content photo-post">
//		      <div class="photo">
//		        <a href="http://filmtodigital.tumblr.com/post/64683119271/autumn-colors-photo-by-daniel-sorine">
//		          <img alt="Autumn colors. Photo by Daniel Sorine." src="http://25.media.tumblr.com/e5ca95053cbd9c68dbe912fedb153640/tumblr_mv0y5z5Xr21s3gpmko1_1280.jpg">
//		        </a>
//		      </div>
//		      <div class="caption"><p>Autumn colors. Photo by Daniel Sorine.</p></div>
//		    </div>
		
		// TODO Auto-generated method stub
		return false;
	}

}
