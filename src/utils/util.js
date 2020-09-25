
export function triggerWindowResizeEvent() {
  const event = document.createEvent('HTMLEvents')
  event.initEvent('resize', true, true)
  event.eventType = 'message'
  window.dispatchEvent(event)
}

export function isObject(value) {
  return value && typeof value === 'object' && value.constructor === Object
}

// decode html tag
export function decodeHTML(html) {
  let elem = document.createElement('div')
  elem.innerHTML = html
  const output = elem.innerText || elem.textContent
  elem = null
  return output
}
