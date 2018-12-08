/*
https://github.com/creationix/git-sha1/blob/master/git-sha1.js

The MIT License (MIT)

Copyright (c) 2013 Tim Caswell

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

'use strict';

// A streaming interface for when nothing is passed in.
function create() {

	var h0 = 0x67452301;
	var h1 = 0xEFCDAB89;
	var h2 = 0x98BADCFE;
	var h3 = 0x10325476;
	var h4 = 0xC3D2E1F0;
	// The first 64 bytes (16 words) is the data chunk
	var block = new Uint32Array(80), offset = 0, shift = 24;
	var totalLength = 0;

	// We have a full block to process.  Let's do it!
	function processBlock() {
		// Extend the sixteen 32-bit words into eighty 32-bit words:
		for (var i = 16; i < 80; i++) {
			var w = block[i - 3] ^ block[i - 8] ^ block[i - 14] ^ block[i - 16];
			block[i] = (w << 1) | (w >>> 31);
		}

		// log(block);

		// Initialize hash value for this chunk:
		var a = h0;
		var b = h1;
		var c = h2;
		var d = h3;
		var e = h4;
		var f, k;

		// Main loop:
		for (i = 0; i < 80; i++) {
			if (i < 20) {
				f = d ^ (b & (c ^ d));
				k = 0x5A827999;
			}
			else if (i < 40) {
				f = b ^ c ^ d;
				k = 0x6ED9EBA1;
			}
			else if (i < 60) {
				f = (b & c) | (d & (b | c));
				k = 0x8F1BBCDC;
			}
			else {
				f = b ^ c ^ d;
				k = 0xCA62C1D6;
			}
			var temp = (a << 5 | a >>> 27) + f + e + k + (block[i] | 0);
			e = d;
			d = c;
			c = (b << 30 | b >>> 2);
			b = a;
			a = temp;
		}

		// Add this chunk's hash to result so far:
		h0 = (h0 + a) | 0;
		h1 = (h1 + b) | 0;
		h2 = (h2 + c) | 0;
		h3 = (h3 + d) | 0;
		h4 = (h4 + e) | 0;

		// The block is now reusable.
		offset = 0;
		for (i = 0; i < 16; i++) {
			block[i] = 0;
		}
	}

	function write(byte) {
		block[offset] |= (byte & 0xff) << shift;
		if (shift) {
			shift -= 8;
		}
		else {
			offset++;
			shift = 24;
		}
		if (offset === 16) {
			processBlock();
		}
	}

	function updateString(string) {
		var length = string.length;
		totalLength += length * 8;
		for (var i = 0; i < length; i++) {
			write(string.charCodeAt(i));
		}
	}

	// The user gave us more data.  Store it!
	function update(chunk) {
		if (typeof chunk === 'string') {
			return updateString(chunk);
		}
		var length = chunk.length;
		totalLength += length * 8;
		for (var i = 0; i < length; i++) {
			write(chunk[i]);
		}
	}

	function toHex(word) {
		var hex = '';
		for (var i = 28; i >= 0; i -= 4) {
			hex += ((word >> i) & 0xf).toString(16);
		}
		return hex;
	}

	// No more data will come, pad the block, process and return the result.
	function digest() {
		// Pad
		write(0x80);
		if (offset > 14 || (offset === 14 && shift < 24)) {
			processBlock();
		}
		offset = 14;
		shift = 24;

		// 64-bit length big-endian
		write(0x00); // numbers this big aren't accurate in javascript anyway
		write(0x00); // ..So just hard-code to zero.
		write(totalLength > 0xffffffffff ? totalLength / 0x10000000000 : 0x00);
		write(totalLength > 0xffffffff ? totalLength / 0x100000000 : 0x00);
		for (var s = 24; s >= 0; s -= 8) {
			write(totalLength >> s);
		}

		// At this point one last processBlock() should trigger and we can pull out the result.
		return toHex(h0) +
		toHex(h1) +
		toHex(h2) +
		toHex(h3) +
		toHex(h4);
	}

	return { update: update, digest: digest };
}

// Input chunks must be either arrays of bytes or "raw" encoded strings
module.exports = function sha1(buffer) {
	if (buffer === undefined) {
		return create();
	}
	var shasum = create();
	shasum.update(buffer);
	return shasum.digest();
};
