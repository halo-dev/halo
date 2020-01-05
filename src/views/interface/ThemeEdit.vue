<template>
  <div>
    <a-row :gutter="12">
      <a-col
        :xl="18"
        :lg="18"
        :md="18"
        :sm="24"
        :xs="24"
        :style="{'padding-bottom':'12px'}"
      >
        <a-card :bodyStyle="{ padding: '16px' }">
          <a-form layout="vertical">
            <a-form-item>
              <codemirror
                v-model="content"
                :options="codemirrorOptions"
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
        <a-card :bodyStyle="{ padding: '16px' }">
          <template slot="title">
            <a-select
              style="width: 100%"
              @change="onSelectTheme"
              v-model="selectedTheme.id"
            >
              <a-select-option
                v-for="(theme,index) in themes"
                :key="index"
                :value="theme.id"
              >{{ theme.name }}
                <a-icon
                  v-if="theme.activated"
                  type="check"
                />
              </a-select-option>
            </a-select>
          </template>
          <theme-file
            v-if="files"
            :files="files"
            @listenToSelect="handleSelectFile"
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
      codemirrorOptions: {
        tabSize: 4,
        mode: 'text/html',
        lineNumbers: true,
        line: true
      },
      files: null,
      file: {},
      content: '',
      themes: [],
      selectedTheme: {}
    }
  },
  created() {
    this.loadActivatedTheme()
    this.loadFiles()
    this.loadThemes()
  },
  methods: {
    loadActivatedTheme() {
      themeApi.getActivatedTheme().then(response => {
        this.selectedTheme = response.data.data
      })
    },
    loadFiles() {
      themeApi.listFilesActivated().then(response => {
        this.files = response.data.data
      })
    },
    loadThemes() {
      themeApi.listAll().then(response => {
        this.themes = response.data.data
      })
    },
    onSelectTheme(themeId) {
      this.files = null
      themeApi.listFiles(themeId).then(response => {
        this.files = response.data.data
      })
    },
    handleSelectFile(file) {
      const _this = this
      if (!file.editable) {
        this.$message.info('该文件不支持修改！')
        this.content = ''
        this.file = {}
        this.buttonDisabled = true
        return
      }
      if (
        file.name === 'settings.yaml' ||
        file.name === 'settings.yml' ||
        file.name === 'theme.yaml' ||
        file.name === 'theme.yml'
      ) {
        this.$confirm({
          title: '警告：请谨慎修改该配置文件',
          content: '修改之后可能会产生不可预料的问题！',
          onCancel() {
            _this.content = ''
            _this.file = {}
            _this.buttonDisabled = true
          }
        })
      }
      themeApi.getContent(this.selectedTheme.id, file.path).then(response => {
        this.content = response.data.data
        this.file = file
        this.buttonDisabled = false
      })
    },
    handlerSaveContent() {
      themeApi.saveContent(this.selectedTheme.id, this.file.path, this.content).then(response => {
        this.$message.success('保存成功！')
      })
    }
  }
}
</script>
