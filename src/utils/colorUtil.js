const isLight = color => {
  if (!isHex(color)) {
    return false
  }

  // Check the format of the color, HEX or RGB?
  let r, g, b, hsp
  if (color.match(/^rgb/)) {
    // If HEX --> store the red, green, blue values in separate variables
    color = color.match(/^rgba?\((\d+),\s*(\d+),\s*(\d+)(?:,\s*(\d+(?:\.\d+)?))?\)$/)

    r = color[1]
    g = color[2]
    b = color[3]
  } else {
    // If RGB --> Convert it to HEX: http://gist.github.com/983661
    color = +('0x' + color.slice(1).replace(color.length < 5 && /./g, '$&$&'))

    r = color >> 16
    g = (color >> 8) & 255
    b = color & 255
  }

  // HSP (Highly Sensitive Poo) equation from http://alienryderflex.com/hsp.html
  hsp = Math.sqrt(0.299 * (r * r) + 0.587 * (g * g) + 0.114 * (b * b))

  // Using the HSP value, determine whether the color is light or dark
  return hsp > 127.5
}

const randomHex = () => {
  return '#' + Math.floor(Math.random() * 16777215).toString(16)
}
const hexRegExp = /(^#[0-9A-F])/i

const isHex = color => {
  return hexRegExp.test(color)
}

const isRgb = color => {
  return /^rgb/.test(color)
}
const isHsl = color => {
  return /^hsl/.test(color)
}

export { isLight, randomHex, isHex, hexRegExp, isRgb, isHsl }
