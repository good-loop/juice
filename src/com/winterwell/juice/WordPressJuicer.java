package com.winterwell.juice;

import java.util.Collections;
import java.util.List;

import winterwell.utils.TodoException;

public class WordPressJuicer extends AJuicer {


// An example of a post:
	//
//	<div id="post-88" class="post-88 post type-post status-publish format-standard hentry category-uncategorized">
//	<h2 class="entry-title"><a href="http://www.soda.sh/static/blog/?p=88" title="Permalink to SoDash nominated for Best Advertising or Marketing Tech Startup Award" rel="bookmark">SoDash nominated for Best Advertising or Marketing Tech Startup Award</a></h2>
//
//	<div class="entry-meta">
//		<span class="meta-prep meta-prep-author">Posted on</span> <a href="http://www.soda.sh/static/blog/?p=88" title="1:50 pm" rel="bookmark"><span class="entry-date">October 28, 2011</span></a> <span class="meta-sep">by</span> <span class="author vcard"><a class="url fn n" href="http://www.soda.sh/static/blog/?author=2" title="View all posts by winterstein">winterstein</a></span>			</div><!-- .entry-meta -->
//
//		<div class="entry-content">
//		<div style='float:right; width:200px;'><img src='http://farm5.static.flickr.com/4058/4446461866_2a2822cd2d.jpg' width='200px'>Image (cc) Alan Cleaver</div>
//<p>SoDash has been nominated for the TechCrunch / Europas Startup Awards, in the category of Best Advertising or Marketing Tech.</p>
//<p>We’ve got just a few days left to vote, with the awards on the 17th of November. We&#8217;d really appreciate it if you’d cast a vote our way!</p>
//<p>To do so, please visit: <a href='http://theeuropas.com/vote/'>http://theeuropas.com/vote</a> and scroll down to Best Advertising or Marketing Tech Startup, 3rd category from the top.  SoDash is 5th from the bottom of that list – select us and hit ‘vote’.</p>
//<p>Or, just press CTRL+F and type in SoDash to find us quickly on the page.</p>
//<p>There’s no registration required so this will only take you a few seconds – it’s literally two clicks!  Please feel free to tell all your friends, family, colleagues, acquaintances, etc. to vote for us as well…</p>
//<p>Many thanks in advance!</p>
//					</div><!-- .entry-content -->
//
//	<div class="entry-utility">
//							<span class="cat-links">
//				<span class="entry-utility-prep entry-utility-prep-cat-links">Posted in</span> <a href="http://www.soda.sh/static/blog/?cat=1" title="View all posts in Uncategorized" rel="category">Uncategorized</a>					</span>
//			<span class="meta-sep">|</span>
//										<span class="comments-link"><a href="http://www.soda.sh/static/blog/?p=88#respond" title="Comment on SoDash nominated for Best Advertising or Marketing Tech Startup Award">Leave a comment</a></span>
//		<span class="meta-sep">|</span> <span class="edit-link"><a class="post-edit-link" href="http://www.soda.sh/static/blog/wp-admin/post.php?post=88&amp;action=edit" title="Edit Post">Edit</a></span>			</div><!-- .entry-utility -->
//</div><!-- #post-## -->
	
	@Override
	List<Anno> juice(JuiceMe doc) {
		// Fail fast for non-WordPress
		String blog = new BlogSniffer().sniff(doc.html);
		if ( ! BlogSniffer.WORDPRESS.equals(blog)) return Collections.EMPTY_LIST;
	
		// TODO Look for meta-data
		
		return added(doc);
	}

}
