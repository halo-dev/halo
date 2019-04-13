<template>
  <page-view>
    <a-row :gutter="12" type="flex" align="middle">
      <a-col
        class="attachment-item"
        v-for="attachment in attachments"
        :key="attachment.id"
        :xl="4"
        :lg="4"
        :md="12"
        :sm="12"
        :xs="24"
      >
        <a-card :bodyStyle="{ padding: 0}" hoverable>
          <!-- <img :src="attachment.thumbPath" :alt="attachment.name" slot="cover"> -->
          <div class="box">
            <img :src="attachment.thumbPath"/>
          </div>
          <a-card-meta>
            <template slot="description">{{ attachment.name }}</template>
          </a-card-meta>
        </a-card>
      </a-col>
    </a-row>
    <a-row type="flex" justify="end" :gutter="12">
      <a-pagination
        v-model="pagination.page"
        :defaultPageSize="pagination.size"
        :total="pagination.total"
      ></a-pagination>
    </a-row>
    <div class="upload-button">
      <a-button type="primary" shape="circle" icon="plus" size="large" @click="showUploadModal"></a-button>
    </div>
    <a-modal title="上传附件" v-model="uploadVisible" :footer="null">
      <a-upload-dragger
        name="file"
        :multiple="true"
        action="http://localhost:8090/admin/api/attachments/upload"
        @change="handleChange"
      >
        <p class="ant-upload-drag-icon">
          <a-icon type="inbox"/>
        </p>
        <p class="ant-upload-text">点击选择文件或将文件拖拽到此处</p>
        <p
          class="ant-upload-hint"
        >支持单个或批量上传</p>
      </a-upload-dragger>
    </a-modal>
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import attachmentApi from '@/api/attachment'
export default {
  components: {
    PageView
  },
  data() {
    return {
      uploadVisible: false,
      attachments: [],
      pagination: {
        page: 1,
        size: 16,
        sort: ''
      }
    }
  },
  created() {
    this.loadAttachments()
  },
  methods: {
    loadAttachments() {
      const pagination = Object.assign({}, this.pagination)
      pagination.page--
      attachmentApi.list(pagination).then(response => {
        this.attachments = response.data.data.content
        this.pagination.total = response.data.data.total
      })
    },
    showUploadModal() {
      this.uploadVisible = true
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

<style lang="less" scoped>
.attachment-item {
  padding-bottom: 12px;
}

.upload-button {
  position: fixed;
  bottom: 80px;
  right: 20px;
}

.box{
    width: 100%;
    margin: 0 auto;
    background: skyblue;
    position: relative;
    padding-bottom: 56%;
    overflow: hidden;
}
.box>img{
    width: 100%;
    height: 100%;
    position: absolute;
    top: 0;
    left: 0;
}
</style>
