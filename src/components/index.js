import Vue from 'vue'

// pro components
import Ellipsis from '@/components/Ellipsis'
import FooterToolbar from '@/components/FooterToolbar'
import FilePondUpload from '@/components/Upload/FilePondUpload'

const _components = {
  Ellipsis,
  FooterToolbar,
  FilePondUpload
}

const components = {}

Object.keys(_components).forEach(key => {
  components[key] = Vue.component(key, _components[key])
})

export default components
