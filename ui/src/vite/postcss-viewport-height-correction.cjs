const hasViewportHeight = /-?[0-9.]+vh/;
const viewportHeight = /(-?[0-9.]+)vh/g;
const correctedViewportHeight = /var\(--vh,\s*1vh\)/;

module.exports = () => ({
  postcssPlugin: "postcss-viewport-height-correction",
  Declaration(declaration) {
    const { value } = declaration;
    if (!hasViewportHeight.test(value) || correctedViewportHeight.test(value)) {
      return;
    }

    declaration.cloneAfter({
      value: value.replace(viewportHeight, "calc(var(--vh, 1vh) * $1)"),
    });
  },
});

module.exports.postcss = true;
