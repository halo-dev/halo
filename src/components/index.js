import Vue from 'vue'

// pro components
import AvatarList from '@/components/AvatarList'
import Ellipsis from '@/components/Ellipsis'
import FooterToolbar from '@/components/FooterToolbar'
import NumberInfo from '@/components/NumberInfo'
import DescriptionList from '@/components/DescriptionList'
import Tree from '@/components/Tree/Tree'
import Trend from '@/components/Trend'
import Result from '@/components/Result'
import ExceptionPage from '@/components/Exception'
import Upload from '@/components/Upload/Upload'

const _components = {
  AvatarList,
  Trend,
  Ellipsis,
  FooterToolbar,
  NumberInfo,
  DescriptionList,
  Tree,
  Result,
  ExceptionPage,
  Upload
}

const components = {}

Object.keys(_components).forEach(key => {
  components[key] = Vue.component(key, _components[key])
})

export default components
