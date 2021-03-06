<template>
  <a-form layout="vertical">
    <a-form-item>
      <a-spin :spinning="loading">
        <codemirror v-model="logContent" :options="codemirrorOptions"></codemirror>
      </a-spin>
    </a-form-item>
    <a-form-item>
      <a-space>
        <a-select defaultValue="200" v-model="logLines" @change="handleLoadLogsLines" style="width: 100px">
          <a-select-option value="200">200 行</a-select-option>
          <a-select-option value="500">500 行</a-select-option>
          <a-select-option value="800">800 行</a-select-option>
          <a-select-option value="1000">1000 行</a-select-option>
        </a-select>
        <a-button type="primary" @click="handleLoadLogsLines()" :loading="loading">刷新</a-button>
        <a-button type="dashed" @click="handleDownloadLogFile()" :loading="downloading">下载</a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>
<script>
import { codemirror } from 'vue-codemirror-lite'
import 'codemirror/mode/shell/shell.js'
import adminApi from '@/api/admin'
import { datetimeFormat } from '@/utils/datetime'
export default {
  name: 'RuntimeLogs',
  components: {
    codemirror
  },
  data() {
    return {
      codemirrorOptions: {
        tabSize: 4,
        mode: 'shell',
        lineNumbers: true,
        line: true
      },
      logContent: '',
      loading: false,
      logLines: 200,
      downloading: false
    }
  },
  beforeMount() {
    this.handleLoadLogsLines()
  },
  updated() {
    // 滚动条定位到底部
    this.$el.querySelector('.CodeMirror-scroll').scrollTop = this.$el.querySelector('.CodeMirror-scroll').scrollHeight
  },
  methods: {
    handleLoadLogsLines() {
      this.loading = true
      adminApi
        .getLogFiles(this.logLines)
        .then(response => {
          this.logContent = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.loading = false
          }, 400)
        })
    },
    handleDownloadLogFile() {
      const hide = this.$message.loading('下载中...', 0)
      this.downloading = true
      adminApi
        .getLogFiles(this.logLines)
        .then(response => {
          var blob = new Blob([response.data.data])
          var downloadElement = document.createElement('a')
          var href = window.URL.createObjectURL(blob)
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
