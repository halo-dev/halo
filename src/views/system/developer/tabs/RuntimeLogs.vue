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
import actuatorApi from '@/api/actuator'
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
      loading: true
    }
  },
  created() {
    this.loadLogs()
  },
  methods: {
    loadLogs() {
      this.loading = true
      actuatorApi.logfile().then(response => {
        this.logContent = response.data
        this.loading = false
      })
    }
  }
}
</script>
