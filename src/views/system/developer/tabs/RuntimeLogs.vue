<template>
  <a-form layout="vertical">
    <a-form-item>
      <a-spin :spinning="loading">
        <Codemirror ref="editor" v-model="logContent" :extensions="editor.extensions" height="700px" />
      </a-spin>
    </a-form-item>
    <a-form-item>
      <a-space>
        <a-select v-model="logLines" defaultValue="200" style="width: 100px" @change="handleLoadLogsLines">
          <a-select-option value="200">200 行</a-select-option>
          <a-select-option value="500">500 行</a-select-option>
          <a-select-option value="800">800 行</a-select-option>
          <a-select-option value="1000">1000 行</a-select-option>
        </a-select>
        <a-button :loading="loading" type="primary" @click="handleLoadLogsLines()">刷新</a-button>
        <a-button :loading="downloading" type="dashed" @click="handleDownloadLogFile()">下载</a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>
<script>
import Codemirror from '@/components/Codemirror/Codemirror'
import { java } from '@codemirror/lang-java'
import { datetimeFormat } from '@/utils/datetime'
import apiClient from '@/utils/api-client'

export default {
  name: 'RuntimeLogs',
  components: {
    Codemirror
  },
  data() {
    return {
      logContent: '',
      loading: false,
      logLines: 200,
      downloading: false,
      editor: {
        extensions: [java()]
      }
    }
  },
  beforeMount() {
    this.handleLoadLogsLines()
  },
  methods: {
    handleLoadLogsLines() {
      this.loading = true
      apiClient
        .getLogFile(this.logLines)
        .then(response => {
          this.logContent = response.data
          this.$nextTick(() => {
            this.$refs.editor.handleInitCodemirror()
            const scrollerView = this.$el.querySelector('.cm-scroller')
            scrollerView.scrollTop = scrollerView.scrollHeight - scrollerView.clientHeight
          })
        })
        .finally(() => {
          this.loading = false
        })
    },
    handleDownloadLogFile() {
      const hide = this.$message.loading('下载中...', 0)
      this.downloading = true
      apiClient
        .getLogFile(this.logLines)
        .then(response => {
          const blob = new Blob([response.data])
          const downloadElement = document.createElement('a')
          const href = window.URL.createObjectURL(blob)
          downloadElement.href = href
          downloadElement.download = 'halo-log-' + datetimeFormat(new Date(), 'YYYY-MM-DD-HH-mm-ss') + '.log'
          document.body.appendChild(downloadElement)
          downloadElement.click()
          document.body.removeChild(downloadElement)
          window.URL.revokeObjectURL(href)
        })
        .catch(() => {
          this.$message.error('下载失败！')
        })
        .finally(() => {
          setTimeout(() => {
            this.downloading = false
            hide()
          }, 400)
        })
    }
  }
}
</script>
