jQuery( document.body ).on( 'post-load', function() {
	if ( typeof twttr == 'object' && twttr.widgets && twttr.widgets.load )
		twttr.widgets.load();
});;
jQuery( document ).ready( function( $ ) {

	// handle failed tweets or tweets that haven't been fetched yet
	$( '.pending-tweet' ).each( do_client_side_twitter_oembed );

	function do_client_side_twitter_oembed() {
		var $this = $(this),
			text = $this.text(),
			url = 'http://api.twitter.com/1/statuses/oembed.json?omit_script=true&callback=?&';

			
		// If we find an exact match, we want to fetch its content from the oembed endpoint and display it
		if ( text.match( /^http(s|):\/\/twitter\.com(\/\#\!\/|\/)([a-zA-Z0-9_]{1,20})\/status(es)*\/(\d+)$/ ) ) {
			url += 'url=' + encodeURIComponent( text );
		} else if ( text.match( /^(\d+)$/ ) ) {
			url += 'id=' + text;
		} else {
			return;
		}

		// Need to make a JSONP call to avoid CORS issues
		$.getJSON( url, function( data ) {
			if ( data.html ) {
				$this.html( data.html );
				$this.show();
			}
		} );
	}
} );;
