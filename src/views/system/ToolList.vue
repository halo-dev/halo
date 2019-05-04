<template>
  <div class="page-header-index-wide">
    <div class="card-content">
      <a-row :gutter="12">
        <a-col
          :xl="4"
          :lg="4"
          :md="12"
          :sm="24"
          :xs="24"
        >
          <a-card
            title="Markdown 文章导入"
            :bordered="false"
            :bodyStyle="{ padding: '16px' }"
          >
            <p>支持 Hexo/Jekyll 导入并解析元数据</p>
            <a-button
              type="primary"
              style="float:right"
              @click="importMarkDown"
            >导入</a-button>
          </a-card>
        </a-col>
      </a-row>
      <a-modal
        title="Markdown 文章导入"
        v-model="markdownUpload"
        :footer="null"
      >
        <a-upload-dragger
          name="file"
          :multiple="true"
          action="//jsonplaceholder.typicode.com/posts/"
          @change="handleChange"
        >
          <p class="ant-upload-drag-icon">
            <a-icon type="inbox" />
          </p>
          <p class="ant-upload-text">拖拽或点击选择 MarkDown 文件到此处</p>
        </a-upload-dragger>
      </a-modal>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      markdownUpload: false
    }
  },
  methods: {
    importMarkDown() {
      this.markdownUpload = true
    },
    handleChange(info) {
      const status = info.file.status
      if (status !== 'uploading') {
        console.log(info.file, info.fileList)
      }
      if (status === 'done') {
        this.$message.success(`${info.file.name} file uploaded successfully.`)
      } else if (status === 'error') {
        this.$message.error(`${info.file.name} file upload failed.`)
      }
    }
  }
}
</script>

<style scoped>
</style>
