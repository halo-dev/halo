import Vue from 'vue'

import Ellipsis from '@/components/Ellipsis'
import FooterToolbar from '@/components/FooterToolbar'
import FilePondUpload from '@/components/Upload/FilePondUpload'
import AttachmentUploadModal from './Attachment/AttachmentUploadModal'
import AttachmentSelectModal from './Attachment/AttachmentSelectModal'
import AttachmentDetailModal from './Attachment/AttachmentDetailModal'
import ReactiveButton from './Button/ReactiveButton'
import PostTag from './Post/PostTag'
import AttachmentInput from './Input/AttachmentInput'
import CommentListView from './Comment/CommentListView'

const _components = {
  Ellipsis,
  FooterToolbar,
  FilePondUpload,
  AttachmentUploadModal,
  AttachmentSelectModal,
  AttachmentDetailModal,
  ReactiveButton,
  PostTag,
  AttachmentInput,
  CommentListView
}

const components = {}

Object.keys(_components).forEach(key => {
  components[key] = Vue.component(key, _components[key])
})

export default components
