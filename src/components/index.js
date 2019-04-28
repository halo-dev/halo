import Vue from 'vue'

// pro components
import Ellipsis from '@/components/Ellipsis'
import FooterToolbar from '@/components/FooterToolbar'
import NumberInfo from '@/components/NumberInfo'
import DescriptionList from '@/components/DescriptionList'
import ExceptionPage from '@/components/Exception'
import Upload from '@/components/Upload/Upload'

const _components = {
  Ellipsis,
  FooterToolbar,
  NumberInfo,
  DescriptionList,
  ExceptionPage,
  Upload
}

const components = {}

Object.keys(_components).forEach(key => {
  components[key] = Vue.component(key, _components[key])
})

export default components
