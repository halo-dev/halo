/*eslint sort-vars:0, curly:0*/

'use strict';

/**
 * Converts a hex CSS color value to RGB.
 * Adapted from http://stackoverflow.com/a/5624139.
 *
 * @param	String	hex		The hexadecimal color value
 * @return	Object			The RGB representation
 */
function hex2rgb(hex) {
	// Expand shorthand form (e.g. "03F") to full form (e.g. "0033FF")
	var shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
	hex = hex.replace(shorthandRegex, function (m, r, g, b) {
		return r + r + g + g + b + b;
	});

	var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
	return result ? {
		r: parseInt(result[1], 16),
		g: parseInt(result[2], 16),
		b: parseInt(result[3], 16)
	} : null;
}

/**
 * Converts an RGB color value to a hex string.
 * @param  Object rgb RGB as r, g, and b keys
 * @return String     Hex color string
 */
function rgb2hex(rgb) {
	return '#' + ['r', 'g', 'b'].map(function (key) {
		return ('0' + rgb[key].toString(16)).slice(-2);
	}).join('');
}

/**
 * Converts an RGB color value to HSL. Conversion formula adapted from
 * http://en.wikipedia.org/wiki/HSL_color_space. This function adapted
 * from http://stackoverflow.com/a/9493060.
 * Assumes r, g, and b are contained in the set [0, 255] and
 * returns h, s, and l in the set [0, 1].
 *
 * @param   Object  rgb     RGB as r, g, and b keys
 * @return  Object          HSL as h, s, and l keys
 */
function rgb2hsl(rgb) {
	var r = rgb.r, g = rgb.g, b = rgb.b;
	r /= 255; g /= 255; b /= 255;
	var max = Math.max(r, g, b), min = Math.min(r, g, b);
	var h, s, l = (max + min) / 2;

	if (max === min) {
		h = s = 0; // achromatic
	} else {
		var d = max - min;
		s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
		switch (max) {
			case r: h = (g - b) / d + (g < b ? 6 : 0); break;
			case g: h = (b - r) / d + 2; break;
			case b: h = (r - g) / d + 4; break;
		}
		h /= 6;
	}

	return { h: h, s: s, l: l };
}

/**
 * Converts an HSL color value to RGB. Conversion formula adapted from
 * http://en.wikipedia.org/wiki/HSL_color_space. This function adapted
 * from http://stackoverflow.com/a/9493060.
 * Assumes h, s, and l are contained in the set [0, 1] and
 * returns r, g, and b in the set [0, 255].
 *
 * @param   Object  hsl     HSL as h, s, and l keys
 * @return  Object          RGB as r, g, and b values
 */
function hsl2rgb(hsl) {

	function hue2rgb(p, q, t) {
		if (t < 0) t += 1;
		if (t > 1) t -= 1;
		if (t < 1 / 6) return p + (q - p) * 6 * t;
		if (t < 1 / 2) return q;
		if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6;
		return p;
	}

	var h = hsl.h, s = hsl.s, l = hsl.l;
	var r, g, b;

	if(s === 0){
		r = g = b = l; // achromatic
	}else{

		var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
		var p = 2 * l - q;
		r = hue2rgb(p, q, h + 1 / 3);
		g = hue2rgb(p, q, h);
		b = hue2rgb(p, q, h - 1 / 3);
	}

	return {
		r: Math.round(r * 255),
		g: Math.round(g * 255),
		b: Math.round(b * 255)
	};
}

module.exports = {
	hex2rgb: hex2rgb,
	rgb2hex: rgb2hex,
	rgb2hsl: rgb2hsl,
	hsl2rgb: hsl2rgb,
	rgb2rgbString: function (rgb) {
		return 'rgb(' + [rgb.r, rgb.g, rgb.b].join(',') + ')';
	}
};
