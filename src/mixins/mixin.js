// import Vue from 'vue'
import { DEVICE_TYPE } from '@/utils/device'
import { mapState } from 'vuex'

// const mixinsComputed = Vue.config.optionMergeStrategies.computed
// const mixinsMethods = Vue.config.optionMergeStrategies.methods

const mixin = {
  computed: {
    ...mapState({
      layoutMode: state => state.app.layout,
      navTheme: state => state.app.theme,
      primaryColor: state => state.app.color,
      fixedHeader: state => state.app.fixedHeader,
      fixedSidebar: state => state.app.fixedSidebar,
      contentWidth: state => state.app.contentWidth,
      autoHideHeader: state => state.app.autoHideHeader,
      sidebarOpened: state => state.app.sidebar
    })
  },
  methods: {
    isTopMenu() {
      return this.layoutMode === 'topmenu'
    },
    isSideMenu() {
      return !this.isTopMenu()
    }
  }
}

const mixinDevice = {
  computed: {
    ...mapState({
      device: state => state.app.device
    })
  },
  methods: {
    isMobile() {
      return this.device === DEVICE_TYPE.MOBILE
    },
    isDesktop() {
      return this.device === DEVICE_TYPE.DESKTOP
    },
    isTablet() {
      return this.device === DEVICE_TYPE.TABLET
    }
  }
}

const mixinPostEdit = {
  data() {
    return {
      viewMetas: {
        pageHeaderHeight: 0,
        pageFooterHeight: 0
      }
    }
  },
  computed: {
    editorHeight() {
      const toolbarHeight = 64
      const contentMarginTop = 24
      const titleInputHeight = 40
      return `calc(100vh - ${
        toolbarHeight +
        contentMarginTop +
        titleInputHeight +
        this.viewMetas.pageHeaderHeight +
        this.viewMetas.pageFooterHeight +
        10
      }px - 1rem)`
    }
  },
  mounted() {
    this.handleGetViewMetas()
  },
  methods: {
    handleGetViewMetas() {
      const pageHeaderView = document.getElementsByClassName('page-header')
      if (pageHeaderView && pageHeaderView.length > 0) {
        this.viewMetas.pageHeaderHeight = pageHeaderView[0].clientHeight
      }
      const pageFooterView = document.getElementsByClassName('ant-layout-footer')
      if (pageFooterView && pageFooterView.length > 0) {
        this.viewMetas.pageFooterHeight = pageFooterView[0].clientHeight
      }
    }
  }
}

export { mixin, mixinDevice, mixinPostEdit }
