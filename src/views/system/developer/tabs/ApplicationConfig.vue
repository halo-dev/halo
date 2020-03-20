<template>
  <div>
    <a-alert
      message="注意：配置文件严格要求代码格式，上下文必须对齐，属性与值之间必须以英文冒号和空格隔开。如格式有误，将无法启动。"
      banner
      closable
    />
    <a-form layout="vertical">
      <a-form-item>
        <a-skeleton
          active
          :loading="loading"
          :paragraph="{rows: 12}"
        >
          <codemirror
            v-model="content"
            :options="codemirrorOptions"
          ></codemirror>
        </a-skeleton>
      </a-form-item>
      <a-form-item>
        <a-popconfirm
          :title="'修改配置文件之后需重启才能生效，是否继续？'"
          okText="确定"
          cancelText="取消"
          @confirm="handleUpdateConfig()"
        >
          <a-button
            type="primary"
            style="margin-right: 8px;"
          >保存</a-button>
        </a-popconfirm>
        <a-popconfirm
          :title="'你确定要重启吗？'"
          okText="确定"
          cancelText="取消"
          @confirm="handleRestartApplication()"
        >
          <a-button type="danger">重启</a-button>
        </a-popconfirm>
      </a-form-item>
    </a-form>
  </div>
</template>
<script>
import { codemirror } from 'vue-codemirror-lite'
import 'codemirror/mode/yaml/yaml.js'
import adminApi from '@/api/admin'
export default {
  name: 'ApplicationConfig',
  components: {
    codemirror
  },
  data() {
    return {
      codemirrorOptions: {
        tabSize: 4,
        mode: 'text/x-yaml',
        lineNumbers: true,
        line: true
      },
      content: '',
      loading: true
    }
  },
  created() {
    this.loadConfig()
  },
  methods: {
    loadConfig() {
      this.loading = true
      adminApi.getApplicationConfig().then(response => {
        this.content = response.data.data
        this.loading = false
      })
    },
    handleUpdateConfig() {
      adminApi.updateApplicationConfig(this.content).then(response => {
        this.$message.success(`配置保存成功！`)
        this.loadConfig()
      })
    },
    handleRestartApplication() {
      adminApi.restartApplication().then(response => {
        this.$message.info(`重启中...`)
      })
    }
  }
}
</script>
