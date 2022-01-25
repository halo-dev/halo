<template>
  <div class="h-full">
    <halo-editor
      ref="md"
      v-model="originalContentData"
      :boxShadow="false"
      :ishljs="true"
      :toolbars="toolbars"
      autofocus
      @imgAdd="handleAttachmentUpload"
      @openImagePicker="attachmentSelectVisible = true"
      @save="handleSaveDraft"
    />

    <AttachmentSelectModal :visible.sync="attachmentSelectVisible" @confirm="handleSelectAttachment" />
  </div>
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
      originalContentData: '',
      attachmentSelectVisible: false
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
    handleSelectAttachment({ markdown }) {
      this.$refs.md.insertText(this.$refs.md.getTextareaDom(), {
        prefix: '',
        subfix: '',
        str: markdown.join('\n')
      })
    },
    handleSaveDraft() {
      this.$emit('onSaveDraft')
    }
  }
}
</script>
