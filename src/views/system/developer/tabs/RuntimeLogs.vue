<template>
  <a-form layout="vertical">
    <a-form-item>
      <a-skeleton
        active
        :loading="loading"
        :paragraph="{rows: 12}"
      >
        <codemirror
          v-model="logContent"
          :options="codemirrorOptions"
        ></codemirror>
      </a-skeleton>
    </a-form-item>
    <a-form-item>
      <a-select
        defaultValue="200"
        style="margin-right: 8px;width: 100px"
        @change="handleLinesChange"
      >
        <a-select-option value="200">200 行</a-select-option>
        <a-select-option value="500">500 行</a-select-option>
        <a-select-option value="800">800 行</a-select-option>
        <a-select-option value="1000">1000 行</a-select-option>
      </a-select>
      <a-button
        type="primary"
        style="margin-right: 8px;"
        @click="loadLogs()"
      >刷新</a-button>
      <a-button
        type="dashed"
        @click="handleDownloadLogFile()"
      >下载</a-button>
    </a-form-item>
  </a-form>
</template>
<script>
import { codemirror } from 'vue-codemirror-lite'
import 'codemirror/mode/shell/shell.js'
import adminApi from '@/api/admin'
import moment from 'moment'
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
      loading: true,
      logLines: 200
    }
  },
  created() {
    this.loadLogs()
  },
  updated() {
    // 滚动条定位到底部
    this.$el.querySelector('.CodeMirror-scroll').scrollTop = this.$el.querySelector('.CodeMirror-scroll').scrollHeight
  },
  methods: {
    loadLogs() {
      this.loading = true
      adminApi.getLogFiles(this.logLines).then(response => {
        this.logContent = response.data.data
        this.loading = false
      })
    },
    handleDownloadLogFile() {
      const hide = this.$message.loading('下载中...', 0)
      adminApi
        .getLogFiles(this.logLines)
        .then(response => {
          var blob = new Blob([response.data.data])
          var downloadElement = document.createElement('a')
          var href = window.URL.createObjectURL(blob)
          downloadElement.href = href
          downloadElement.download = 'halo-log-' + moment(new Date(), 'YYYY-MM-DD-HH-mm-ss') + '.log'
          document.body.appendChild(downloadElement)
          downloadElement.click()
          document.body.removeChild(downloadElement)
          window.URL.revokeObjectURL(href)
          this.$message.success('下载成功！')
        })
        .catch(() => {
          this.$message.error('下载失败！')
        })
        .finally(() => {
          hide()
        })
    },
    handleLinesChange(value) {
      this.logLines = value
    }
  }
}
</script>
