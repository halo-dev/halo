import Vue from 'vue'
import {
  DEFAULT_COLOR,
  DEFAULT_CONTENT_WIDTH_TYPE,
  DEFAULT_FIXED_HEADER,
  DEFAULT_FIXED_HEADER_HIDDEN,
  DEFAULT_FIXED_SIDEBAR,
  DEFAULT_LAYOUT_MODE,
  DEFAULT_THEME,
  LAYOUT_SETTING,
  SIDEBAR_TYPE
} from '@/store/mutation-types'

const app = {
  state: {
    sidebar: true,
    device: 'desktop',
    theme: '',
    layout: '',
    contentWidth: '',
    fixedHeader: false,
    fixedSidebar: false,
    autoHideHeader: false,
    color: null,
    layoutSetting: false,
    loginModal: false
  },
  mutations: {
    SET_SIDEBAR_TYPE: (state, type) => {
      state.sidebar = type
      Vue.ls.set(SIDEBAR_TYPE, type)
    },
    CLOSE_SIDEBAR: state => {
      Vue.ls.set(SIDEBAR_TYPE, true)
      state.sidebar = false
    },
    TOGGLE_DEVICE: (state, device) => {
      state.device = device
    },
    TOGGLE_THEME: (state, theme) => {
      Vue.ls.set(DEFAULT_THEME, theme)
      state.theme = theme
    },
    TOGGLE_LAYOUT_MODE: (state, layout) => {
      Vue.ls.set(DEFAULT_LAYOUT_MODE, layout)
      state.layout = layout
    },
    TOGGLE_FIXED_HEADER: (state, fixed) => {
      Vue.ls.set(DEFAULT_FIXED_HEADER, fixed)
      state.fixedHeader = fixed
    },
    TOGGLE_FIXED_SIDEBAR: (state, fixed) => {
      Vue.ls.set(DEFAULT_FIXED_SIDEBAR, fixed)
      state.fixedSidebar = fixed
    },
    TOGGLE_FIXED_HEADER_HIDDEN: (state, show) => {
      Vue.ls.set(DEFAULT_FIXED_HEADER_HIDDEN, show)
      state.autoHideHeader = show
    },
    TOGGLE_CONTENT_WIDTH: (state, type) => {
      Vue.ls.set(DEFAULT_CONTENT_WIDTH_TYPE, type)
      state.contentWidth = type
    },
    TOGGLE_COLOR: (state, color) => {
      Vue.ls.set(DEFAULT_COLOR, color)
      state.color = color
    },
    TOGGLE_LAYOUT_SETTING: (state, show) => {
      Vue.ls.set(LAYOUT_SETTING, show)
      state.layoutSetting = show
    },
    TOGGLE_LOGIN_MODAL: (state, show) => {
      state.loginModal = show
    }
  },
  actions: {
    setSidebar({ commit }, type) {
      commit('SET_SIDEBAR_TYPE', type)
    },
    ToggleTheme({ commit }, theme) {
      commit('TOGGLE_THEME', theme)
    },
    ToggleLayoutMode({ commit }, mode) {
      commit('TOGGLE_LAYOUT_MODE', mode)
    },
    ToggleFixedHeader({ commit }, fixedHeader) {
      commit('TOGGLE_FIXED_HEADER', fixedHeader)
    },
    ToggleFixedSidebar({ commit }, fixedSidebar) {
      commit('TOGGLE_FIXED_SIDEBAR', fixedSidebar)
    },
    ToggleFixedHeaderHidden({ commit }, show) {
      commit('TOGGLE_FIXED_HEADER_HIDDEN', show)
    },
    ToggleContentWidth({ commit }, type) {
      commit('TOGGLE_CONTENT_WIDTH', type)
    },
    ToggleColor({ commit }, color) {
      commit('TOGGLE_COLOR', color)
    },
    ToggleLayoutSetting({ commit }, show) {
      commit('TOGGLE_LAYOUT_SETTING', show)
    },
    ToggleLoginModal({ commit }, show) {
      commit('TOGGLE_LOGIN_MODAL', show)
    }
  }
}

export default app
