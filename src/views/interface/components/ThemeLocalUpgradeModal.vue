<template>
  <a-modal v-model="modalVisible" :afterClose="onModalClose" :footer="null" destroyOnClose title="更新主题">
    <FilePondUpload
      ref="updateByFile"
      :accepts="['application/x-zip', 'application/x-zip-compressed', 'application/zip']"
      :field="theme.id"
      :multiple="false"
      :uploadHandler="uploadHandler"
      label="点击选择主题更新包或将主题更新包拖拽到此处<br>仅支持 ZIP 格式的文件"
      name="file"
      @success="onThemeUploadSuccess"
    ></FilePondUpload>
  </a-modal>
</template>
<script>
import apiClient from '@/utils/api-client'

export default {
  name: 'ThemeLocalUpgradeModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    theme: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      uploadHandler: (file, options, field) => apiClient.theme.updateByUpload(file, options, field)
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
    onModalClose() {
      this.$refs.updateByFile.handleClearFileList()
      this.$emit('onAfterClose')
    },
    onThemeUploadSuccess() {
      this.modalVisible = false
      this.$emit('success')
    }
  }
}
</script>
