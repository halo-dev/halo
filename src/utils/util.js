export function triggerWindowResizeEvent() {
  const event = document.createEvent('HTMLEvents')
  event.initEvent('resize', true, true)
  event.eventType = 'message'
  window.dispatchEvent(event)
}

export function isObject(value) {
  return value && typeof value === 'object' && value.constructor === Object
}

/**
 * decode html tag
 * @param {*} html
 * @returns
 * @deprecated
 */
export function decodeHTML(html) {
  let elem = document.createElement('div')
  elem.innerHTML = html
  const output = elem.innerText || elem.textContent
  elem = null
  return output
}

export function deepClone(source) {
  if (!source && typeof source !== 'object') {
    throw new Error('error arguments', 'deepClone')
  }
  const targetObj = source.constructor === Array ? [] : {}
  Object.keys(source).forEach(keys => {
    if (source[keys] && typeof source[keys] === 'object') {
      targetObj[keys] = deepClone(source[keys])
    } else {
      targetObj[keys] = source[keys]
    }
  })
  return targetObj
}
