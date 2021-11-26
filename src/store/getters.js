const getters = {
  device: state => state.app.device,
  theme: state => state.app.theme,
  color: state => state.app.color,
  layoutSetting: state => state.app.layoutSetting,
  loginModal: state => state.app.loginModal,
  token: state => state.user.token,
  user: state => state.user.user,
  options: state => state.option.options
}

export default getters
