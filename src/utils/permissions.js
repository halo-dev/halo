export function actionToObject(json) {
  try {
    return JSON.parse(json)
  } catch (e) {
    this.$log.debug('err', e.message)
  }
  return []
}
