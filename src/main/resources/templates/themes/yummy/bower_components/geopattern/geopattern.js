(function ($) {

'use strict';

var Pattern = require('./lib/pattern');

/*
 * Normalize arguments, if not given, to:
 * string: (new Date()).toString()
 * options: {}
 */
function optArgs(cb) {
	return function (string, options) {
		if (typeof string === 'object') {
			options = string;
			string = null;
		}
		if (string === null || string === undefined) {
			string = (new Date()).toString();
		}
		if (!options) {
			options = {};
		}

		return cb.call(this, string, options);
	};
}

var GeoPattern = module.exports = {
	generate: optArgs(function (string, options) {
		return new Pattern(string, options);
	})
};

if ($) {

	// If jQuery, add plugin
	$.fn.geopattern = optArgs(function (string, options) {
		return this.each(function () {
			var titleSha = $(this).attr('data-title-sha');
			if (titleSha) {
				options = $.extend({
					hash: titleSha
				}, options);
			}
			var pattern = GeoPattern.generate(string, options);
			$(this).css('background-image', pattern.toDataUrl());
		});
	});

}

}(typeof jQuery !== 'undefined' ? jQuery : null));
