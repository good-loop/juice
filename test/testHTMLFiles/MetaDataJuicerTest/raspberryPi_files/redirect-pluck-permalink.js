/* m-1661~static/1918/comment-formatting.js */
jQ(function() {


    function countNewlines(text) {
		var nNewlines = 0;
		var pos = -1;
		
		do {
			pos = text.indexOf( '\n', pos + 1 );
			
  			if( pos !== -1 ) {
  				nNewlines++;	    
			}
		} while( pos !== -1)
		
		return nNewlines;
	}	   

    function textLength(str) {
		if (!str) {
			return 0;
		}

		return str.length + countNewlines(str);
	}
	   
	function limitChars(textid, limit, infodiv) {
		var text = jQ('#'+textid).val();
		var textlength = textLength(text);
		if(textlength > limit) {
			jQ('#'+textid).val(text.substr(0, limit - countNewlines(text)));
			return false;
		}
		else {
			jQ('#' + infodiv).html(limit - textlength);
			return true;
		}
	}
	jQ('#comment-body').keyup(function() {
		limitChars('comment-body', 5000, 'chars-left');
	});

	jQ('#comment-body').blur(function() {
		limitChars('comment-body', 5000, 'chars-left');
	});

	jQ('#id_reason').live('keyup', function(ev) {
		limitChars('id_reason', 250, 'chars-left-desc');
	});

	jQ('#id_reason').live('blur', function(ev) {
		limitChars('id_reason', 250, 'chars-left-desc');
	});

    jQ('form.discussion-commenting-form').each(function() {
        var jqCommentingForm=jQ(this);

        var commentTextArea=jqCommentingForm.find("textarea");
        var wrapSelectionWith = function(before, after, defaultContent)
        {
            var wrappedContent=commentTextArea.getSelection().text;
            commentTextArea.replaceSelection(before + (wrappedContent?wrappedContent:defaultContent) + after);
        }

        var jqButtonArea = jqCommentingForm.find(".formatting-buttons");
        var addButton = function(displayName, onClick)
        {
            jqButtonArea.append('<button type="button" class="fancy-button fancy-button-inline control-button-text button-'+displayName.toLowerCase()+'" value="'+displayName+'" >'+displayName+"</button>");
            jqButtonArea.find("button[value='"+displayName+"']").click(onClick);
        }

        addButton("Blockquote", function() { wrapSelectionWith('<blockquote>', '</blockquote>' , '') });
        addButton("Bold",       function() { wrapSelectionWith('<b>', '</b>', '') });
        addButton("Italic",     function() { wrapSelectionWith('<i>', '</i>', '') });
        addButton("Link",       function() {
            var url = prompt("Enter a web address:", "http://");
            if (url) { wrapSelectionWith('<a href="' + url + '">', '</a>', url ); }
        });
	});
});
/* m-1661~static/1918/comment-highlight.js */
var discussion = discussion || {};
discussion.highlight = {};

discussion.highlight.onClick = function(event) {
    var $this = jQ(event.target);
    if ($this.is('input')) {
        var $form = $this.parents('form');
        if ($form.length && $form.parent().hasClass('highlight')) {
            event.preventDefault();
            discussion.highlight.makeAjaxRequest($form);
        }
    }
};

discussion.highlight.makeAjaxRequest = function($form) {
    jQ.ajax({
        type: 'POST',
        url: $form.attr('action'),
        data: {
            'comment_id': $form.find('input[name="comment_id"]').val(),
            'is_signed_up': $form.find('input[name="is_signed_up"]').val()
        },
        success: function(data, status, xhr) {
            var $message = jQ(data).find('span.message');
            if ($message.hasClass('success')) {
                discussion.highlight.toggleHighlightedStatus($form);
            }
            else if ($message.hasClass('error')) {
                alert($message.html());
            }
        }
    });
};

discussion.highlight.toggleHighlightedStatus = function($form) {
    var $comment = $form.parents('.comment');
    var action = $form.attr('action');

    if ($comment.hasClass('highlighted')) {
        $comment.removeClass('highlighted')
            .find('.guardian-pick').remove();
        action = action.replace('unHighlightComment', 'highlightComment');
        $form.attr('action', action)
            .find('input[type="submit"]').val('Pick comment');
    }
    else {
        var msg = '<div class="guardian-pick rollover">Guardian <b>pick</b><span>This comment has been chosen by a member of Guardian staff because it\'s interesting and adds to the debate<b class="arrow"></b></span></div>';
        $comment.addClass('highlighted')
            .find('.comment-author .profile')
            .after(msg);
        action = action.replace('highlightComment', 'unHighlightComment');
        $form.attr('action', action)
            .find('input[type="submit"]').val('Un-pick comment');
    }
};

jQ(document).ready(function() {
    jQ('#discussion-comments').click(discussion.highlight.onClick);
});

/* m-1661~static/1918/comment-replyto.js */
jQ(document).ready(function(){
	// hide this for non-js users since requires js to function
	jQ('.comment-tools .reply a').show();
	
	jQ('.comment-tools .reply a').ajaxoverlay({
        'columnwidth' : 'eight-col',
        'source' : '#reply-to-placeholder',
        'dobeforeopen' : function() {        	
        	
        	// grab the comment they're replying to
        	var orig_link = this;
    		var id = orig_link.data('replyto');
    		jQ('#reply_to_id').val(id);
    		
    		if(guardian.r2.omniture.isAvailable()) {
	    		// track with omniture
	    		s.linkTrackVars = 'events,eVar37';
	        	s.linkTrackEvents = 'event37';
	    		s.eVar37 = 'Comment:Response';
	        	s.events = 'event37';
	            s.tl(true, 'o', 'Comment response');
    		}

            var orig_comment = jQ('ul#comment-' + id);
        	var comment = orig_comment.clone();
        	comment.attr('id', 'cloned-comment-' + id).addClass('discussion cloned-reply');        	
        	
        	// grab the reply form itself
            var comment_form = jQ('#post-area').hide().css('width', '600px');
            var textarea = comment_form.find('textarea');
            textarea.css('width', '592px');
            comment_form.find('#discussion-preview-comment').val('Update preview');
                        
            // add everything
            comment_form.show().appendTo('.ajax-popup-window');
        	comment.prependTo('.ajax-popup-window');
        	
            // disable links in popup comment preview
            jQ('.ajax-popup-window ul.comment a').die().live('click', function(e){ e.preventDefault(); return false; });
            jQ('.ajax-popup-window ul.comment li.abuse-report').remove();
            jQ('.ajax-popup-window ul.comment li.clip form').live('submit', function() { return false; });
        	
        },
        'dobeforeclose' : function(){
        	jQ('#reply_to_id').val(''); // reset reply-to ID
        	// plonk the form back into the page
            var comment_form = jQ('#post-area').hide().removeAttr('style');
            comment_form.find('textarea').attr('style', '');
            comment_form.find('#discussion-preview-comment').val('Preview');
            comment_form.show().insertAfter('#post-area-placeholder');
        }
    });
    
	
	
});
/* m-1661~static/1918/comment-setup.js */
jQ(function() {
    var permalink = jQ('.comment-tools .link a, .comment-actions .link a');
	var closeLink = jQ('.comment-tools .permalink .close, .comment-actions .permalink .close');

	function loadTwitterButton(overlay) {
		var placeholder = overlay.find('span.twitter-share-button');
		if (0 === placeholder.length) { return; }

		var link = jQ('<a class="twitter-share-button"></a>')
					.attr('href', placeholder.attr('data-href'))
					.attr('data-url', placeholder.attr('data-url'))
					.attr('data-text', placeholder.attr('data-text'))
					.attr('data-count', placeholder.attr('data-count'))
					.html(placeholder.html());
		placeholder.empty().replaceWith(link);

		if ('undefined' !== twttr.widgets.load) { twttr.widgets.load(); }
	}

	function bindFacebookButton(overlay) {
		if ('undefined' !== popUpNewWindow) {
			overlay.find('.facebook-share-btn').click(function(e) {
				e.preventDefault();
				popUpNewWindow(this.href, 580, 400);
			});
		}
	}

	function openPermalink(link) {
		var overlay = jQ(link).parent().siblings('.permalink');
		loadTwitterButton(overlay);
		bindFacebookButton(overlay);
		overlay.show().find('input').focus();
	}

	function closePermalink() {
		jQ(this).parents('.permalink').hide();
	}

	closeLink.live('click', closePermalink);

	jQ('.permalink input').live('focus', function() {
		jQ(this).select();
	});

	permalink.bind('click', function() {
		if(guardian.r2.omniture.isAvailable()) {
			s.linkTrackVars = 'events,eVar12';
	    	s.linkTrackEvents = 'event16';
			s.eVar12 = 'Comment:Share';
	    	s.events = 'event16';
	        s.tl(true, 'o', 'Comment share');
		}

		openPermalink(this);
		return false;
	});
	
	jQ('a.new-window').click(function() {
		window.open(this.href);
		return false;
	});
	
	if(window.location.hash) {
		var hash = window.location.hash;
		var re = /comment-(\d+)/;
		if(hash.match(re)) {
			var comment = jQ(hash);
			comment.addClass('is-permalinked');
		}
	}
	
	if(guardian.r2.omniture.isAvailable()) {
		// bind omniture events: begin typing comment, submitting comment & navigating pagination
		jQ('textarea#comment-body').keydown(guardian.r2.discussion.begin_commenting);
		jQ('form#newcommenting-form').submit(guardian.r2.discussion.end_commenting);
		jQ('.response-form').submit(guardian.r2.discussion.end_responding);
		jQ('.discussion-pagination .view-latest a, .discussion-pagination .view-all a, .discussion-pagination .view-next a').click(guardian.r2.discussion.nav_click);
	}

	// textarea plugins
	jQ('#comment-body').textareacount({
		counter_selector: 'span',
		show_when_under: 100
	});

	jQ('a[href="#post-area"]').live('click', function() {
		jQ('#comment-body').focus();
	});
});

ensurePackage("guardian.r2.discussion");
guardian.r2.discussion.commenting_started = false;

// track when user begins typing comment
guardian.r2.discussion.begin_commenting = function() {
	if(!guardian.r2.discussion.commenting_started) {
		s.linkTrackVars='events';
		s.linkTrackEvents='event53';
		s.events='event53';
		s.tl(this,'o','Comment begin');
	}
	guardian.r2.discussion.commenting_started = true;
}

// track when user submits comment
guardian.r2.discussion.end_commenting = function() {
	if (guardian.r2.omniture.isAvailable()) {
		// Track page name / threaded
	    guardian.r2.discussion.track_comment_posted();

	    // first comment
		if (discussion.first_comment) {
            s.linkTrackVars='events';
            s.linkTrackEvents='event52';
            s.events='event52';
            s.tl(this,'o','First comment posted');
	    }

	    // normal comment
	    else if (document.getElementById('newcommenting-form').reply_to_id.value === '') {
			s.linkTrackVars='events';
			s.linkTrackEvents='event51';
			s.events='event51';
			s.tl(this,'o','Comment complete');
		}

		// reply
		else {
			guardian.r2.discussion.end_responding();
		}
	}
}

// track a response being sent
guardian.r2.discussion.end_responding = function() {
	guardian.r2.discussion.track_comment_posted();

	s.linkTrackVars='events';
	s.linkTrackEvents='event67';
	s.events='event67';
	s.tl(this,'o','Response complete');
}

// track clicks on pagination nav
guardian.r2.discussion.nav_click = function() {
	var nav_type = jQ(this).data('nav-type');
	s.linkTrackVars = 'eVar37';
	
	switch(nav_type) {
		case "latest":
			s.eVar37='Comment: View Latest';
			break;
		case "all":
			s.eVar37='Comment: View All';
			break;
		case "next":
			s.eVar37='Comment: View Next';
			break;
	}
	s.tl(true, 'o', 'Comment navigation');
}
guardian.r2.discussion.track_comment_posted = function() {
	// threaded or un-threaded and page name
	s.linkTrackVars = 'eVar49,eVar7';
	s.eVar49 = discussion.tracking.comment_type;
	s.eVar7 = document.title;
	s.tl(this,'o','Comment type,Page Name');
}


/* m-1661~static/1918/comment-sorting.js */
jQ(document).ready(function() {
  var $form = jQ('#sort-comments'),
      $fieldset = $form.find('fieldset'),
      $popupStart = jQ('.popup-start');

  $form.find('input[type="submit"]').remove();
  $form.find('label').addClass('hidden');
  $form.find('p span').replaceWith($fieldset.removeClass('hidden'));
  $popupStart.removeClass('no-js');

  $form.find('select[name="sort_by"]').bind('change', function() {
    window.location.hash = 'start-of-comments';
    $form.submit();
  });
});
/* m-1661~static/1918/js/autoresize.jquery.min.js */
/*
 * jQuery autoResize (textarea auto-resizer)
 * @copyright James Padolsey http://james.padolsey.com
 * @version 1.04
 */

(function(a){a.fn.autoResize=function(j){var b=a.extend({onResize:function(){},animate:true,animateDuration:150,animateCallback:function(){},extraSpace:20,limit:1000},j);this.filter('textarea').each(function(){var c=a(this).css({resize:'none','overflow-y':'hidden'}),k=c.height(),f=(function(){var l=['height','width','lineHeight','textDecoration','letterSpacing'],h={};a.each(l,function(d,e){h[e]=c.css(e)});return c.clone().removeAttr('id').removeAttr('name').css({position:'absolute',top:0,left:-9999}).css(h).attr('tabIndex','-1').insertBefore(c)})(),i=null,g=function(){f.height(0).val(a(this).val()).scrollTop(10000);var d=Math.max(f.scrollTop(),k)+b.extraSpace,e=a(this).add(f);if(i===d){return}i=d;if(d>=b.limit){a(this).css('overflow-y','');return}b.onResize.call(this);b.animate&&c.css('display')==='block'?e.stop().animate({height:d},b.animateDuration,b.animateCallback):e.height(d)};c.unbind('.dynSiz').bind('keyup.dynSiz',g).bind('keydown.dynSiz',g).bind('change.dynSiz',g)});return this}})(jQuery);

/* m-1661~static/1918/js/textareacount.jquery.min.js */
(function($) {
	var methods = {
		init : function(o) {
			return this.each(function() {
				var textarea = $(this);
				var counter_holder = $('[data-textarea="' + this.id + '"]');
				var current_count = this.innerHTML.length;
				var max_length = textarea.data('maxlength');
				var counter = o.counter_selector ? counter_holder.find(o.counter_selector) : counter_holder;
				
				// TODO: Make this a method and work properly
				if (o.show_when_under) {
					counter_holder.hide();
				}

				textarea.bind('keyup', function() {
					if (this.value.length > max_length) {
						this.value = this.value.substring(0, max_length);
					}
					var characters_left = max_length - this.value.length;
					
					if (o.show_when_under && (characters_left <= o.show_when_under)) {
						counter_holder.fadeIn();
					} else {
						counter_holder.hide();
					}
					counter.text(characters_left);
				});
			});
		},
		show : function() {},
		hide : function() {},
		update : function() {}
	};

	$.fn.textareacount = function(method) {
		if (methods[method]) {
			return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error('Method ' +  method + ' does not exist on textareacount.jquery');
		}
	};
}) (jQuery);

/* m-1661~static/1918/redirect-pluck-permalink.js */

function fixPluckCommentUrl(serviceUrl) {
    var pluckHashRegexp = /CommentKey:(.{36})/

    var pluckHashMatcher=pluckHashRegexp.exec(window.location.hash)

    if (pluckHashMatcher!=null) {
        var pluckCommentKey = pluckHashMatcher[0]
        jQ.ajax({
            url: serviceUrl.split('?')[0],
            data: { 'pluck-comment-keys' : pluckCommentKey },
            dataType: "jsonp",
            cache: true,
            success: function(data) {
                var discussionCommentId = data[0].discussionCommentId
                window.location.hash="#comment-"+discussionCommentId;
            }
        });
    }
}


function fixPluckForumUrl(serviceUrl) {
    var urlParams=decodeURIComponent(window.location.search);
    var pluckForumIdentifiers = {};

    var addForumIdentifier = function(identifier,pluckIdentiferName)
    {
        var matcher=new RegExp(pluckIdentiferName+":([0-9a-fA-F\-]{36})").exec(urlParams);
        if (matcher!=null) {
            pluckForumIdentifiers['pluck-'+identifier+'-id']=matcher[1];
            pluckForumIdentifiers.found=true;
        }
    }

    addForumIdentifier('category','Cat');
    addForumIdentifier('forum','Forum');
    addForumIdentifier('discussion','Discussion');
    addForumIdentifier('post','Post');

    if (pluckForumIdentifiers.found) {
        jQ.ajax({
            url: serviceUrl.split('?')[0],
            data: pluckForumIdentifiers,
            dataType: "jsonp",
            cache: true,
            success: function(data) {
                var discussionUrl = data[0].discussionUrl;
                if (discussionUrl) {
                    window.location=discussionUrl;
                }
            }
        });
    }
}

