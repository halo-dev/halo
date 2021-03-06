<template>
  <div>
    <halo-editor
      ref="md"
      v-model="originalContentData"
      :boxShadow="false"
      :toolbars="toolbars"
      :ishljs="true"
      autofocus
      @imgAdd="handleAttachmentUpload"
      @save="handleSaveDraft"
    />
  </div>
</template>
<script>
import { toolbars } from '@/core/const'
import { haloEditor } from 'halo-editor'
import 'halo-editor/dist/css/index.css'
import attachmentApi from '@/api/attachment'
export default {
  name: 'MarkdownEditor',
  components: {
    haloEditor
  },
  props: {
    originalContent: {
      type: String,
      required: false,
      default: ''
    }
  },
  data() {
    return {
      toolbars,
      originalContentData: ''
    }
  },
  watch: {
    originalContent(val) {
      this.originalContentData = val
    },
    originalContentData(val) {
      this.$emit('onContentChange', val)
    }
  },
  methods: {
    handleAttachmentUpload(pos, $file) {
      var formdata = new FormData()
      formdata.append('file', $file)
      attachmentApi.upload(formdata).then(response => {
        var responseObject = response.data
        var HaloEditor = this.$refs.md
        HaloEditor.$img2Url(pos, encodeURI(responseObject.data.path))
      })
    },
    handleSaveDraft() {
      this.$emit('onSaveDraft')
    }
  }
}
</script>
