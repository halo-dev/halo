<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col :xl="18" :lg="18" :md="18" :sm="24" :xs="24">
        <a-card hoverable>
          <codemirror :value="value"></codemirror>
        </a-card>
      </a-col>
      <a-col :xl="6" :lg="6" :md="6" :sm="24" :xs="24">
        <a-card hoverable title="Anatole 主题">
          <a-directory-tree multiple @select="onSelect" @expand="onExpand">
            <theme-file :list="files"></theme-file>
          </a-directory-tree>
          <!-- <theme-file :list="files"></theme-file> -->
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import themeApi from '@/api/theme'
import ThemeFile from '@/components/Tree/ThemeFile'
import { codemirror } from 'vue-codemirror-lite'
export default {
  components: {
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

<style>
.CodeMirror-gutters {
  border-right: 1px solid #fff3f3;
  background-color: #ffffff;
}
</style>
