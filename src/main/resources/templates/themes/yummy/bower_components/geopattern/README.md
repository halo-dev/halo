# GeoPattern

This is a JavaScript port of [jasonlong/geo_pattern](https://github.com/jasonlong/geo_pattern) with a [live preview page](http://btmills.github.io/geopattern/geopattern.html) and is derived from the background generator originally used for [GitHub Guides](http://guides.github.com/).

## Usage

### Web

Include the [minified script](js/geopattern.min.js). jQuery is optional.

```HTML
<script src="js/jquery.min.js"></script> <!-- optional -->
<script src="js/geopattern.min.js"></script>
```

Use either the `GeoPattern` browser global or the jQuery plugin:

```JavaScript
// Use the global...
var pattern = GeoPattern.generate('GitHub');
$('#geopattern').css('background-image', pattern.toDataUrl());

// ...or the plugin
$('#geopattern').geopattern('GitHub');
```

For backwards compatibility with the script on the [Guides](http://guides.github.com/), the source hash for generation can be supplied with a `data-title-sha` attribute on the element. If the attribute exists, the generator will ignore the input string and use the supplied hash.

View [`geopattern.html`](geopattern.html) for a complete example.

### Node.js

```bash
npm install geopattern
```

After requiring `geopattern`, the API is identical to the browser version, minus the jQuery plugin.

```js
var GeoPattern = require('geopattern');
var pattern = GeoPattern.generate('GitHub');
pattern.toDataUrl(); // url("data:image/svg+xml;...
```

### API

#### GeoPattern.generate(string, options)

Returns a newly-generated, tiling SVG Pattern.

- `string` Will be hashed using the SHA1 algorithm, and the resulting hash will be used as the seed for generation.

- `options.color` Specify an exact background color. This is a CSS hexadecimal color value.

- `options.baseColor` Controls the relative background color of the generated image. The color is not identical to that used in the pattern because the hue is rotated by the generator. This is a CSS hexadecimal color value, which defaults to `#933c3c`.

- `options.generator` Determines the pattern. [All of the original patterns](https://github.com/jasonlong/geo_pattern#available-patterns) are available in this port, and their names are camelCased.

#### Pattern.color

Gets the pattern's background color as a hexadecimal string.

```js
GeoPattern.generate('GitHub').color // => "#455e8a"
```

#### Pattern.toString() and Pattern.toSvg()

Gets the SVG string representing the pattern.

#### Pattern.toBase64()

Gets the SVG as a Base64-encoded string.

#### Pattern.toDataUri()

Gets the pattern as a data URI, i.e. `data:image/svg+xml;base64,PHN2ZyB...`.

#### Pattern.toDataUrl()

Gets the pattern as a data URL suitable for use as a CSS `background-image`, i.e. `url("data:image/svg+xml;base64,PHN2ZyB...")`.

## License

Licensed under the terms of the MIT License, the full text of which can be read in [LICENSE](LICENSE).
