(function() {
	var require_options = {
		'paths': {
			'jquery' : 'libs/jquery/1.6.4/jquery',
			'twttr' : 'libs/twttr/twttr'
		},

		'packagePaths':  {
			'gu' : 'http://static.gu.com/js/gu'
		}
	};
	var require_script = document.getElementById('require-js');
	var modules = require_script.getAttribute('data-modules').split(/\s?,\s?/);
	var callback = require_script.getAttribute('data-callback');
	
	require(require_options, modules, function() {
		if (window[callback] && typeof window[callback] === 'function') {
			window[callback]();
		}
	});
})();