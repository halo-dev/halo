<template>
  <page-view>
    <a-row :gutter="12">
      <a-col :xl="6" :lg="6" :md="6" :sm="24" :xs="24" class="pb-3">
        <a-card :bodyStyle="{ padding: '16px' }">
          <template slot="title">
            <a-select class="w-full" @change="onSelectTheme" v-model="selectedTheme.id" :loading="themesLoading">
              <a-select-option v-for="(theme, index) in themes" :key="index" :value="theme.id"
                >{{ theme.name }}
                <a-icon v-if="theme.activated" type="check" />
              </a-select-option>
            </a-select>
          </template>
          <a-spin :spinning="filesLoading">
            <theme-file v-if="files" :files="files" @listenToSelect="handleSelectFile" />
          </a-spin>
        </a-card>
      </a-col>
      <a-col :xl="18" :lg="18" :md="18" :sm="24" :xs="24" class="pb-3">
        <a-card :bodyStyle="{ padding: '16px' }">
          <a-form layout="vertical">
            <a-form-item>
              <codemirror v-model="content" :options="codemirrorOptions"></codemirror>
            </a-form-item>
            <a-form-item>
              <ReactiveButton
                @click="handlerSaveContent"
                @callback="saveErrored = false"
                :loading="saving"
                :errored="saveErrored"
                :disabled="buttonDisabled"
                text="保存"
                loadedText="保存成功"
                erroredText="保存失败"
              ></ReactiveButton>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
    </a-row>
  </page-view>
</template>

<script>
import themeApi from '@/api/theme'
import ThemeFile from './components/ThemeFile'
import { PageView } from '@/layouts'
import { codemirror } from 'vue-codemirror-lite'
import 'codemirror/mode/htmlmixed/htmlmixed.js'
export default {
  components: {
    codemirror,
    ThemeFile,
    PageView
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
      files: [],
      filesLoading: false,
      file: {},
      content: '',
      themes: [],
      themesLoading: false,
      selectedTheme: {},
      saving: false,
      saveErrored: false
    }
  },
  created() {
    this.handleGetActivatedTheme()
    this.handleListThemeFiles()
    this.handleListThemes()
  },
  methods: {
    handleGetActivatedTheme() {
      themeApi.getActivatedTheme().then(response => {
        this.selectedTheme = response.data.data
      })
    },
    handleListThemeFiles() {
      this.filesLoading = true
      themeApi
        .listFilesActivated()
        .then(response => {
          this.files = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.filesLoading = false
          }, 200)
        })
    },
    handleListThemes() {
      this.themesLoading = true
      themeApi
        .list()
        .then(response => {
          this.themes = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.themesLoading = false
          }, 200)
        })
    },
    onSelectTheme(themeId) {
      this.files = []
      this.filesLoading = true
      themeApi
        .listFiles(themeId)
        .then(response => {
          this.files = response.data.data
          this.content = ''
          this.file = {}
        })
        .finally(() => {
          setTimeout(() => {
            this.filesLoading = false
          }, 200)
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
      this.saving = true
      themeApi
        .saveContent(this.selectedTheme.id, this.file.path, this.content)
        .catch(() => {
          this.saveErrored = true
        })
        .finally(() => {
          setTimeout(() => {
            this.saving = false
          }, 400)
        })
    }
  }
}
</script>
