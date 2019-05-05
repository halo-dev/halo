import Vue from 'vue'

// pro components
import Ellipsis from '@/components/Ellipsis'
import FooterToolbar from '@/components/FooterToolbar'
import DescriptionList from '@/components/DescriptionList'
import Upload from '@/components/Upload/Upload'

const _components = {
  Ellipsis,
  FooterToolbar,
  DescriptionList,
  Upload
}

const components = {}

Object.keys(_components).forEach(key => {
  components[key] = Vue.component(key, _components[key])
})

export default components
