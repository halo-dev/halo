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
      <a-button
        type="primary"
        style="margin-right: 8px;"
      >下载</a-button>
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
        type="dash"
        @click="()=>this.loadLogs()"
      >刷新</a-button>
    </a-form-item>
  </a-form>
</template>
<script>
import { codemirror } from 'vue-codemirror-lite'
import 'codemirror/mode/shell/shell.js'
import adminApi from '@/api/admin'
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
  methods: {
    loadLogs() {
      this.loading = true
      adminApi.getLogFiles(this.logLines).then(response => {
        this.logContent = response.data.data
        this.loading = false
      })
    },
    handleLinesChange(value) {
      this.logLines = value
    }
  }
}
</script>
