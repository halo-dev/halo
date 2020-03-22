const getters = {
  device: state => state.app.device,
  theme: state => state.app.theme,
  color: state => state.app.color,
  layoutSetting: state => state.app.layoutSetting,
  token: state => state.user.token,
  user: state => state.user.user,
  addRouters: state => state.permission.addRouters,
  apiUrl: state => {
    if (state.app.apiUrl) {
      return state.app.apiUrl
    }
    return `${window.location.protocol}//${window.location.host}`
  },
  options: state => state.option.options
}

export default getters
