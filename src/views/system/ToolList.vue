<template>
  <div class="page-header-index-wide">
    <div class="card-content">
      <a-row :gutter="12">
        <a-col
          :xl="6"
          :lg="6"
          :md="12"
          :sm="24"
          :xs="24"
        >
          <a-card
            title="Markdown 文章导入"
            :bordered="false"
            :bodyStyle="{ padding: '16px' }"
          >
            <p>支持 Hexo/Jekyll 文章导入并解析元数据</p>
            <a-button
              type="primary"
              style="float:right"
              @click="handleImportMarkdown"
            >导入</a-button>
          </a-card>
        </a-col>
      </a-row>
      <a-modal
        title="Markdown 文章导入"
        v-model="markdownUpload"
        :footer="null"
      >
        <upload
          name="files"
          multiple
          accept="text/markdown"
          :uploadHandler="uploadHandler"
          @change="handleChange"
        >
          <p class="ant-upload-drag-icon">
            <a-icon type="inbox" />
          </p>
          <p class="ant-upload-text">拖拽或点击选择 Markdown 文件到此处</p>
          <p class="ant-upload-hint">支持多个文件同时上传</p>
        </upload>
      </a-modal>
    </div>
  </div>
</template>

<script>
import backupApi from '@/api/backup'
export default {
  data() {
    return {
      markdownUpload: false,
      uploadHandler: backupApi.importMarkdown
    }
  },
  methods: {
    handleImportMarkdown() {
      this.markdownUpload = true
    },
    handleChange(info) {
      const status = info.file.status
      if (status !== 'uploading') {
        console.log(info.file, info.fileList)
      }
      if (status === 'done') {
        this.$message.success(`${info.file.name} 导入成功！`)
      } else if (status === 'error') {
        this.$message.error(`${info.file.name} 导入失败！`)
      }
    }
  }
}
</script>
