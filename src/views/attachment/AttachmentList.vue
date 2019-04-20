<template>
  <page-view>
    <a-row :gutter="12" type="flex" align="middle">
      <a-col :span="24" class="search-box">
        <a-card :bordered="false">
          <div class="table-page-search-wrapper">
            <a-form layout="inline">
              <a-row :gutter="48">
                <a-col :md="6" :sm="24">
                  <a-form-item label="关键词">
                    <a-input v-model="queryParam.keyword"/>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="年月份">
                    <a-select placeholder="请选择年月">
                      <a-select-option value="2019-01">2019-01</a-select-option>
                      <a-select-option value="2019-02">2019-02</a-select-option>
                      <a-select-option value="2019-03">2019-03</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="类型">
                    <a-select placeholder="请选择类型">
                      <a-select-option value="image/png">image/png</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <span class="table-page-search-submitButtons">
                    <a-button type="primary" @click="loadAttachments(true)">查询</a-button>
                    <a-button style="margin-left: 8px;" @click="resetParam">重置</a-button>
                  </span>
                </a-col>
              </a-row>
            </a-form>
          </div>
          <div class="table-operator">
            <a-button type="primary" icon="plus" @click="showUploadModal">上传</a-button>
          </div>
        </a-card>
      </a-col>

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
    <a-row type="flex" justify="end">
      <a-pagination
        :total="pagination.total"
        :defaultPageSize="pagination.size"
        :pageSizeOptions="['18', '36', '54','72','90','108']"
        showSizeChanger
        @change="handlePaginationChange"
        @showSizeChange="handlePaginationChange"
      />
    </a-row>
    <a-modal title="上传附件" v-model="uploadVisible" :footer="null">
      <upload name="file" multiple :uploadHandler="uploadHandler" @success="handleUploadSuccess">
        <p class="ant-upload-drag-icon">
          <a-icon type="inbox"/>
        </p>
        <p class="ant-upload-text">点击选择文件或将文件拖拽到此处</p>
        <p class="ant-upload-hint">支持单个或批量上传</p>
      </upload>
    </a-modal>
    <AttachmentDetailDrawer
      v-model="drawerVisiable"
      v-if="selectAttachment"
      :attachment="selectAttachment"
      @delete="handleDelete"
    />
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import AttachmentDetailDrawer from './components/AttachmentDetailDrawer'
import attachmentApi from '@/api/attachment'

export default {
  components: {
    PageView,
    AttachmentDetailDrawer
  },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      uploadVisible: false,
      selectAttachment: {},
      attachments: [],
      editable: false,
      pagination: {
        page: 1,
        size: 18,
        sort: null
      },
      queryParam: {
        page: 0,
        size: 18,
        sort: null,
        keyword: null
      },
      uploadHandler: attachmentApi.upload,
      drawerVisiable: false
    }
  },
  created() {
    this.loadAttachments()
  },
  methods: {
    loadAttachments(isSearch) {
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      if (isSearch) {
        this.queryParam.page = 0
      }
      attachmentApi.query(this.queryParam).then(response => {
        this.attachments = response.data.data.content
        this.pagination.total = response.data.data.total
      })
    },
    showDetailDrawer(attachment) {
      this.selectAttachment = attachment
      this.drawerVisiable = true
    },
    showUploadModal() {
      this.uploadVisible = true
    },
    handleChange(info) {
      const status = info.file.status
      if (status === 'done') {
        this.$message.success(`${info.file.name} 文件上传成功`)
      } else if (status === 'error') {
        this.$message.error(`${info.file.name} 文件上传失败`)
      }
    },
    handleUploadSuccess() {
      this.loadAttachments()
    },
    handlePaginationChange(page, size) {
      this.$log.debug(`Current: ${page}, PageSize: ${size}`)
      this.pagination.page = page
      this.pagination.size = size
      this.loadAttachments()
    },
    resetParam() {
      this.queryParam.keyword = null
      this.loadAttachments()
    },
    handleDelete(attachment) {
      this.loadAttachments()
    }
  }
}
</script>

<style lang="less" scoped>
.ant-divider-horizontal {
  margin: 24px 0 12px 0;
}

.attachment-item,
.search-box {
  padding-bottom: 12px;
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

.table-operator {
  margin-bottom: 0;
}
</style>
