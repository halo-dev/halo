<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col
        :xl="18"
        :lg="18"
        :md="18"
        :sm="24"
        :xs="24"
        :style="{'padding-bottom':'12px'}"
      >
        <a-card>
          <a-form layout="vertical">
            <a-form-item>
              <codemirror
                v-model="content"
                :options="options"
              ></codemirror>
            </a-form-item>
            <a-form-item>
              <a-button
                type="primary"
                @click="handlerSaveContent"
                :disabled="buttonDisabled"
              >保存</a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
      <a-col
        :xl="6"
        :lg="6"
        :md="6"
        :sm="24"
        :xs="24"
        :style="{'padding-bottom':'12px'}"
      >
        <a-card title="Anatole 主题">
          <theme-file
            :files="files"
            @listenToSelect="catchSelectFile"
          />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import themeApi from '@/api/theme'
import ThemeFile from './components/ThemeFile'
import { codemirror } from 'vue-codemirror-lite'
import 'codemirror/mode/htmlmixed/htmlmixed.js'
export default {
  components: {
    codemirror,
    ThemeFile
  },
  data() {
    return {
      buttonDisabled: true,
      options: {
        tabSize: 4,
        mode: 'text/html',
        lineNumbers: true,
        line: true
      },
      files: [],
      file: {},
      content: ''
    }
  },
  created() {
    this.loadFiles()
  },
  methods: {
    onSelect(keys) {
      console.log('Trigger Select', keys)
    },
    onExpand() {
      console.log('Trigger Expand')
    },
    loadFiles() {
      themeApi.listFiles().then(response => {
        this.files = response.data.data
      })
    },
    catchSelectFile(file) {
      const _this = this
      if (!file.editable) {
        this.$message.info('该文件不支持修改')
        this.content = ''
        this.file = {}
        this.buttonDisabled = true
        return
      }
      if (file.name === 'options.yaml' || file.name === 'options.yml') {
        this.$confirm({
          title: '警告：请谨慎修改该配置文件',
          content: '修改之后可能会产生不可预料的问题',
          onCancel() {
            _this.content = ''
            _this.file = {}
            _this.buttonDisabled = true
          }
        })
      }
      themeApi.getContent(file.path).then(response => {
        this.content = response.data.data
        this.file = file
        this.buttonDisabled = false
      })
    },
    handlerSaveContent() {
      themeApi.saveContent(this.file.path, this.content).then(response => {
        this.$message.success('保存成功')
      })
    }
  }
}
</script>

<style lang="less">
.CodeMirror {
  height: 560px;
}
.CodeMirror-gutters {
  border-right: 1px solid #fff3f3;
  background-color: #ffffff;
}
</style>
