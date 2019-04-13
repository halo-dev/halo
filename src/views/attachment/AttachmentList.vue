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
        <a-card :bodyStyle="{ padding: 0 }" hoverable @click="showDetailDrawer(attachment)">
          <div class="attach-thumb">
            <img :src="attachment.thumbPath">
          </div>
          <a-card-meta>
            <template slot="description">{{ attachment.mediaType }}</template>
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
        <p class="ant-upload-hint">支持单个或批量上传</p>
      </a-upload-dragger>
    </a-modal>
    <a-drawer
      v-if="selectAttachment"
      title="附件详情"
      :width="drawerWidth"
      closable
      :visible="drawerVisible"
      destroyOnClose
      @close="onChildClose"
    >
      <a-row type="flex" align="middle">
        <a-col :span="24">
          <a-skeleton active :loading="detailImgLoading" :paragraph="{rows: 8}">
            <div class="attach-detail-img">
              <img :src="selectAttachment.path">
            </div>
          </a-skeleton>
        </a-col>
        <a-divider/>
        <a-col :span="24">
          <a-list itemLayout="horizontal">
            <a-list-item>
              <a-list-item-meta :description="selectAttachment.name">
                <span slot="title">附件名：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :description="selectAttachment.mediaType">
                <span slot="title">附件类型：</span>
              </a-list-item-meta>
            </a-list-item>
          </a-list>
          <a-tabs defaultActiveKey="1">
            <a-tab-pane tab="普通链接" key="1">
              {{ selectAttachment.path }}
            </a-tab-pane>
            <a-tab-pane tab="Markdown 格式" key="3">
              ![{{ selectAttachment.name }}]({{ selectAttachment.path }})
            </a-tab-pane>
          </a-tabs>
        </a-col>
      </a-row>
      <div class="attachment-control">
        <a-button type="danger">删除</a-button>
      </div>
    </a-drawer>
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import attachmentApi from '@/api/attachment'
export default {
  components: {
    PageView
  },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      uploadVisible: false,
      drawerVisible: false,
      detailImgLoading: false,
      selectAttachment: null,
      drawerWidth: '560',
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
  mounted() {
    if (this.isMobile()) {
      this.drawerWidth = '100%'
    } else {
      this.drawerWidth = '460'
    }
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
    showDetailDrawer(attachment) {
      this.drawerVisible = true
      this.detailImgLoading = true
      this.selectAttachment = attachment
      setTimeout(() => {
        this.detailImgLoading = false
      }, 500)
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
    },
    onChildClose() {
      this.drawerVisible = false
    }
  }
}
</script>

<style lang="less" scoped>
.ant-divider-horizontal{
  margin: 24px 0 12px 0;
}

.attachment-item {
  padding-bottom: 12px;
}

.upload-button {
  position: fixed;
  bottom: 80px;
  right: 20px;
}

.attach-thumb {
  width: 100%;
  margin: 0 auto;
  position: relative;
  padding-bottom: 56%;
  overflow: hidden;
}
.attach-thumb > img {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.ant-card-meta {
  padding: 0.8rem;
}

.attach-detail-img img {
  width: 100%;
}

.attachment-control {
  position: absolute;
  bottom: 0px;
  width: 100%;
  border-top: 1px solid rgb(232, 232, 232);
  padding: 10px 16px;
  text-align: right;
  left: 0px;
  background: rgb(255, 255, 255);
  border-radius: 0px 0px 4px 4px;
}
</style>
