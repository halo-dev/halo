<template>
  <page-view>
    <a-row :gutter="12">
      <a-col
        :xl="18"
        :lg="18"
        :md="18"
        :sm="24"
        :xs="24"
        :style="{'padding-bottom':'12px'}">
        <a-card>
          <a-form layout="vertical">
            <a-form-item>
              <codemirror :value="value"></codemirror>
            </a-form-item>
            <a-form-item>
              <a-button type="primary">保存</a-button>
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
        :style="{'padding-bottom':'12px'}">
        <a-card title="Anatole 主题">
          <theme-file :files="files" />
        </a-card>
      </a-col>
    </a-row>
  </page-view>
</template>

<script>
import themeApi from '@/api/theme'
import ThemeFile from './components/ThemeFile'
import { codemirror } from 'vue-codemirror-lite'
import { PageView } from '@/layouts'
export default {
  components: {
    PageView,
    codemirror,
    ThemeFile
  },
  data() {
    return {
      files: [],
      value: 'Hello world'
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
    }
  }
}
</script>

<style lang="less">
.CodeMirror-gutters {
  border-right: 1px solid #fff3f3;
  background-color: #ffffff;
}
.ant-card {
  .ant-card-body {
    padding: 16px;
  }
}
</style>
