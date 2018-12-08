# Contributing to Hover.css

Thanks for considering to contribute to Hover.css. To improve your chances of having your hard work merged into Hover.css, here's a quick guide.

## Guidelines for Effects

1. Effects should work with only one HTML element. This way, developers only need add a class to their element for your fancy effect to work on their page. CSS pseudo-elements are perfectly acceptable as they don't require additional changes to HTML.

2. Aim to provide subtle effects that enhance user experience.

3. Hover.css is available in Sass, LESS, and CSS formats. If you can, please submit your effects in as many of these formats as possible. Don't worry if you're unfamiliar with any though, we'll convert them for you.

4. Try not to use transitions and animations together on the same effect, [we've found this is buggy in Webkit/Blink browsers](https://github.com/IanLunn/Hover/issues/24).

5. Consider submitting an effect that is paired with an opposite, for example: `Bounce In`/`Bounce Out`, `Float`/`Sink`, `Icon Back`/`Icon Forward`.

## Browser Testing Effects

The Hover.css project attempts to deliver effects that work in the latest versions of modern browsers (Firefox, Chrome, Safari, Opera, Internet Explorer 10+), as well as providing simple CSS fixes for older browsers where possible (a fallback color when `rgba()` is used for example).

Fallbacks for older browsers are the responsibility of the developer adding Hover.css to their page, as described in the README's [Browser Support section](https://github.com/IanLunn/Hover#browser-support).

Please be certain any effects you submit at least work in the latest versions of modern browsers, and advise us accordingly if that is not the case.
