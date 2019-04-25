<template>
  <div class="page-header-index-wide">
    <div class="card-content">
      <a-row :gutter="12">
        <a-col
          :sm="24"
          :md="12"
          :lg="8"
        >
          <a-card
            title="Markdown 导入"
            :bordered="false"
          >
            <span style="font-size:18px">Markdown 文档导入</span>
            <a-button
              type="primary"
              style="float:right"
              @click="importMarkDown"
            >导入</a-button>
            <p>支持 Hexo/Jekyll 导入并解析元数据</p>
          </a-card>
        </a-col>
        <a-col
          :sm="24"
          :md="12"
          :lg="8"
        >
          <a-card
            title="WordPress 导入"
            :bordered="false"
          >
            <span style="font-size:18px">WordPress 数据导入</span>
            <a-button
              type="primary"
              style="float:right"
              @click="importWordPress"
            >导入</a-button>
            <p>尽请期待</p>
          </a-card>
        </a-col>
      </a-row>
      <a-modal
        title="Markdown 文档导入"
        v-model="ishow"
        @ok="sure"
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
          <p class="ant-upload-text">拖拽或点击选择MarkDown文件到此处</p>
        </a-upload-dragger>
      </a-modal>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      ishow: false
    }
  },
  methods: {
    importMarkDown() {
      this.ishow = true
    },
    sure() {
      this.ishow = false
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
    },
    importWordPress() {
      this.$message.info('程序猿正在努力开发呢！')
    }
  }
}
</script>

<style scoped>
</style>
