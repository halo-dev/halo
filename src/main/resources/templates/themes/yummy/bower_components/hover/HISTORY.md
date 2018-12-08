## 2.0.2 - Jan 27, 2015

- Added `back-pulse` effect

## 2.0.1 - Jan 26, 2015

- Added `includeClasses` option to Sass/LESS so you can now generate CSS with or without Hover.css default class names
- Added `!default` flags to Sass variables in `_options.scss`

## 2.0.0 - Jan 7, 2015

- Added lots of new effects
- Added LESS support
- Prefixed all effect names with `hvr-` (can be changed in `scss/_options.scss`)
- Updated some effects for better performance and/or browser support
- Moved default button styles out of library
- Moved all effects into sub folders with the name of the category they belong to
- Renamed `hover` and `hover shadow` effects to `bob` and `bob shadow`
- Removed `hover shadow` effect due to browser inconsistencies courtesy of [this Webkit/Blink bug](https://github.com/IanLunn/Hover/issues/24) which can't be worked around
- Updated `bob` (formerly `hover`) and `hang` effects to work around [this WebKit/Blink bug](https://github.com/IanLunn/Hover/issues/24)
- Change default `animation-timing-function` and `transition-timing-function` values for various effects
- Changed all instances of 'colour' to 'color'
- Added small amount of JS to demo page to prevent `<a href="#"></a>` elements from navigating (not required for hover.css to work)
- Added "What's Included?" section to README.md
