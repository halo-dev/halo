const getters = {
  device: state => state.app.device,
  theme: state => state.app.theme,
  color: state => state.app.color,
  token: state => state.user.token,
  user: state => state.user.user,
  avatar: state => state.user.avatar,
  nickname: state => state.user.name,
  roles: state => state.user.roles,
  addRouters: state => state.permission.addRouters
}

export default getters
