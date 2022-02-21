<template>
  <div class="h-full">
    <halo-editor
      ref="editor"
      v-model="originalContentData"
      :boxShadow="false"
      :toolbars="markdownEditorToolbars"
      :uploadRequest="handleAttachmentUpload"
      autofocus
      @change="handleChange"
      @openImagePicker="attachmentSelectVisible = true"
      @save="handleSave"
    />

    <AttachmentSelectModal :visible.sync="attachmentSelectVisible" @confirm="handleSelectAttachment" />
  </div>
</template>
<script>
import haloEditor from '@halo-dev/editor'
import '@halo-dev/editor/dist/lib/style.css'
import apiClient from '@/utils/api-client'
import { markdownEditorToolbars } from '@/core/constant'

export default {
  name: 'MarkdownEditor',
  components: {
    haloEditor: haloEditor.editor
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
      markdownEditorToolbars,
      attachmentSelectVisible: false
    }
  },
  computed: {
    originalContentData: {
      get() {
        return this.originalContent
      },
      set(value) {
        this.$emit('update:originalContent', value)
      }
    }
  },
  methods: {
    handleAttachmentUpload(file) {
      return new Promise((resolve, reject) => {
        const hideLoading = this.$message.loading('上传中...', 0)
        apiClient.attachment
          .upload(file)
          .then(response => {
            const attachment = response.data
            resolve({
              name: attachment.name,
              path: encodeURI(attachment.path)
            })
          })
          .catch(e => {
            this.$log.error('upload image error: ', e)
            reject(e)
          })
          .finally(() => {
            hideLoading()
          })
      })
    },
    handleSelectAttachment({ markdown }) {
      this.$refs.editor.insetAtCursor(markdown.join('\n'))
    },
    handleSave() {
      this.$emit('save')
    },
    handleChange({ originalContent, renderContent }) {
      this.$emit('change', { originalContent, renderContent })
    }
  }
}
</script>
