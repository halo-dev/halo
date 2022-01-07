<template>
  <halo-editor
    ref="md"
    v-model="originalContentData"
    :boxShadow="false"
    :ishljs="true"
    :toolbars="toolbars"
    autofocus
    @imgAdd="handleAttachmentUpload"
    @save="handleSaveDraft"
  />
</template>
<script>
import { toolbars } from '@/core/const'
import { haloEditor } from 'halo-editor'
import 'halo-editor/dist/css/index.css'
import apiClient from '@/utils/api-client'

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
    async handleAttachmentUpload(pos, $file) {
      try {
        const response = await apiClient.attachment.upload($file)
        const responseObject = response.data
        const HaloEditor = this.$refs.md
        HaloEditor.$img2Url(pos, encodeURI(responseObject.path))
      } catch (e) {
        this.$log.error('update image error: ', e)
      }
    },
    handleSaveDraft() {
      this.$emit('onSaveDraft')
    }
  }
}
</script>
