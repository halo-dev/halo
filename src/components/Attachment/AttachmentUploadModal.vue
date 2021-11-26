<template>
  <a-modal v-model="modalVisible" :afterClose="onClose" :footer="null" destroyOnClose title="上传附件">
    <FilePondUpload ref="upload" :uploadHandler="uploadHandler"></FilePondUpload>
  </a-modal>
</template>
<script>
import apiClient from '@/utils/api-client'

export default {
  name: 'AttachmentUploadModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      uploadHandler: (file, options) => apiClient.attachment.upload(file, options)
    }
  },
  computed: {
    modalVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    }
  },
  methods: {
    onClose() {
      this.$refs.upload.handleClearFileList()
      this.$emit('close')
    }
  }
}
</script>
