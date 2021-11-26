<template>
  <page-view>
    <a-row :gutter="12">
      <a-col :lg="6" :md="6" :sm="24" :xl="6" :xs="24" class="pb-3">
        <a-card :bodyStyle="{ padding: '16px' }">
          <template slot="title">
            <a-select v-model="themes.selectedId" :loading="themes.loading" class="w-full" @change="onSelectTheme">
              <a-select-option v-for="(theme, index) in themes.data" :key="index" :value="theme.id">
                {{ theme.name }}{{ theme.activated ? '（当前启用）' : '' }}
              </a-select-option>
            </a-select>
          </template>
          <a-spin :spinning="files.loading">
            <theme-file v-if="files.data" :files="files.data" @listenToSelect="handleSelectFile" />
          </a-spin>
        </a-card>
      </a-col>
      <a-col :lg="18" :md="18" :sm="24" :xl="18" :xs="24" class="pb-3">
        <a-card :bodyStyle="{ padding: '16px' }">
          <a-form layout="vertical">
            <a-form-item>
              <Codemirror ref="editor" v-model="files.content" :extensions="editor.extensions" height="700px" />
            </a-form-item>
            <a-form-item>
              <ReactiveButton
                :disabled="!files.content"
                :errored="files.saveErrored"
                :loading="files.saving"
                erroredText="保存失败"
                loadedText="保存成功"
                text="保存"
                @callback="files.saveErrored = false"
                @click="handlerSaveContent"
              ></ReactiveButton>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
    </a-row>
  </page-view>
</template>

<script>
import apiClient from '@/utils/api-client'
import ThemeFile from './components/ThemeFile'
import { PageView } from '@/layouts'
import Codemirror from '@/components/Codemirror/Codemirror'
import { html } from '@codemirror/lang-html'
import { javascript } from '@codemirror/lang-javascript'
import { css } from '@codemirror/lang-css'

const languageExtensionsMap = {
  ftl: html(),
  css: css(),
  js: javascript()
}

export default {
  components: {
    Codemirror,
    ThemeFile,
    PageView
  },
  data() {
    return {
      themes: {
        data: [],
        loading: false,
        selectedId: null
      },

      files: {
        data: [],
        loading: false,
        selected: {},
        content: '',
        saving: false,
        saveErrored: false
      },

      editor: {
        languageExtensionsMap,
        extensions: []
      }
    }
  },
  created() {
    this.handleListThemes()
  },
  methods: {
    handleListThemes() {
      this.themes.loading = true
      apiClient.theme
        .list()
        .then(response => {
          this.themes.data = response.data

          const activatedTheme = this.themes.data.find(item => item.activated)

          if (activatedTheme) {
            this.themes.selectedId = activatedTheme.id
            this.onSelectTheme(activatedTheme.id)
          }
        })
        .finally(() => {
          this.themes.loading = false
        })
    },
    onSelectTheme(themeId) {
      this.files.data = []
      this.files.loading = true
      apiClient.theme
        .listFiles(themeId)
        .then(response => {
          this.files.data = response.data
          this.files.content = ''
          this.files.selected = {}
        })
        .finally(() => {
          this.files.loading = false
        })
    },
    handleSelectFile(file) {
      const _this = this
      if (!file.editable) {
        this.$message.info('该文件不支持修改！')
        this.files.content = ''
        this.files.selected = {}
        this.handleInitEditor()
        return
      }
      if (['settings.yaml', 'settings.yml', 'theme.yaml', 'theme.yml'].includes(file.name)) {
        this.$confirm({
          title: '警告：请谨慎修改该配置文件',
          content: '修改之后可能会产生不可预料的问题！',
          onCancel() {
            _this.files.content = ''
            _this.files.selected = {}
            _this.handleInitEditor()
          }
        })
      }
      apiClient.theme.getTemplateContent(this.themes.selectedId, file.path).then(response => {
        this.files.content = response.data
        this.files.selected = file
        this.handleInitEditor()
      })
    },
    handlerSaveContent() {
      this.files.saving = true
      apiClient.theme
        .updateTemplateContent(this.themes.selectedId, { path: this.files.selected.path, content: this.files.content })
        .catch(() => {
          this.files.saveErrored = true
        })
        .finally(() => {
          setTimeout(() => {
            this.files.saving = false
          }, 400)
        })
    },
    handleInitEditor() {
      this.$nextTick(() => {
        const filename = this.files.selected.name
        if (filename) {
          const fileExtension = filename.substring(filename.lastIndexOf('.') + 1)
          this.editor.extensions = [this.editor.languageExtensionsMap[fileExtension]]
        }
        this.$refs.editor.handleInitCodemirror()
      })
    }
  }
}
</script>
