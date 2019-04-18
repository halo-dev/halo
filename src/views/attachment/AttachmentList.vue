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
                    <a-button type="primary" @click="loadAttachments">查询</a-button>
                    <a-button style="margin-left: 8px;">重置</a-button>
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
        :pageSizeOptions="['18', '36', '54']"
        showSizeChanger
        @change="handlePaginationChange"
        @showSizeChange="handlePaginationChange"
      />
    </a-row>
    <a-modal title="上传附件" v-model="uploadVisible" :footer="null">
      <a-upload-dragger
        name="file"
        :multiple="true"
        :customRequest="handleUpload"
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
      :width="isMobile()?'100%':'560'"
      closable
      :visible="drawerVisible"
      destroyOnClose
      @close="onChildClose"
    >
      <a-row type="flex" align="middle">
        <a-col :span="24">
          <a-skeleton active :loading="detailLoading" :paragraph="{rows: 8}">
            <div class="attach-detail-img">
              <img :src="selectAttachment.path">
            </div>
          </a-skeleton>
        </a-col>
        <a-divider/>
        <a-col :span="24">
          <a-skeleton active :loading="detailLoading" :paragraph="{rows: 8}">
            <a-list itemLayout="horizontal">
              <a-list-item>
                <a-list-item-meta>
                  <template slot="description" v-if="editable">
                    <a-input v-model="selectAttachment.name" @blur="updateAttachment"/>
                  </template>
                  <template slot="description" v-else>{{ selectAttachment.name }}</template>
                  <span slot="title">
                    附件名：
                    <a-icon type="edit" @click="handleEditName"/>
                  </span>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-list-item-meta :description="selectAttachment.mediaType">
                  <span slot="title">附件类型：</span>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-list-item-meta :description="selectAttachment.size">
                  <span slot="title">附件大小：</span>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-list-item-meta :description="selectAttachment.height+'x'+selectAttachment.width">
                  <span slot="title">图片尺寸：</span>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-list-item-meta :description="selectAttachment.createTime">
                  <span slot="title">上传日期：</span>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-list-item-meta :description="selectAttachment.path">
                  <span slot="title">
                    普通链接：
                    <a-icon type="copy" @click="doCopyNormalLink"/>
                  </span>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-list-item-meta
                  :description="'!['+selectAttachment.name+']('+selectAttachment.path+')'"
                >
                  <span slot="title">
                    Markdown 格式：
                    <a-icon type="copy" @click="doCopyMarkdownLink"/>
                  </span>
                </a-list-item-meta>
              </a-list-item>
            </a-list>
          </a-skeleton>
        </a-col>
      </a-row>
      <div class="attachment-control">
        <a-popconfirm
          title="你确定要删除该附件？"
          @confirm="deleteAttachment(selectAttachment.id)"
          okText="确定"
          cancelText="取消"
        >
          <a-button type="danger">删除</a-button>
        </a-popconfirm>
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
      detailLoading: false,
      selectAttachment: null,
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
      }
    }
  },
  created() {
    this.loadAttachments()
  },
  methods: {
    loadAttachments() {
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      attachmentApi.query(this.queryParam).then(response => {
        this.attachments = response.data.data.content
        this.pagination.total = response.data.data.total
      })
    },
    showDetailDrawer(attachment) {
      this.drawerVisible = true
      this.detailLoading = true
      this.selectAttachment = attachment
      setTimeout(() => {
        this.detailLoading = false
      }, 500)
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
    deleteAttachment(id) {
      attachmentApi.delete(id).then(response => {
        this.$message.success('删除成功！')
        this.drawerVisible = false
        this.loadAttachments()
      })
    },
    doCopyNormalLink() {
      const text = `${this.selectAttachment.path}`
      this.$copyText(text)
        .then(message => {
          console.log('copy', message)
          this.$message.success('复制成功')
        })
        .catch(err => {
          console.log('copy.err', err)
          this.$message.error('复制失败')
        })
    },
    doCopyMarkdownLink() {
      const text = `![${this.selectAttachment.name}](${this.selectAttachment.path})`
      this.$copyText(text)
        .then(message => {
          console.log('copy', message)
          this.$message.success('复制成功')
        })
        .catch(err => {
          console.log('copy.err', err)
          this.$message.error('复制失败')
        })
    },
    onChildClose() {
      this.drawerVisible = false
    },
    handlePaginationChange(page, size) {
      this.$log.debug(`Current: ${page}, PageSize: ${size}`)
      this.pagination.page = page
      this.pagination.size = size
      this.loadAttachments()
    },
    handleUpload(option) {
      this.$log.debug('Uploading option', option)
      const CancelToken = attachmentApi.CancelToken
      const source = CancelToken.source()

      const data = new FormData()
      data.append('file', option.file)
      attachmentApi
        .upload(
          data,
          progressEvent => {
            if (progressEvent.total > 0) {
              progressEvent.percent = (progressEvent.loaded / progressEvent.total) * 100
            }
            this.$log.debug('Uploading percent: ', progressEvent.percent)
            option.onProgress(progressEvent)
          },
          source.token
        )
        .then(response => {
          option.onSuccess(response, option.file)
          this.loadAttachments()
        })
        .catch(error => {
          option.onError(error, error.response)
        })

      return {
        abort: () => {
          source.cancel('Upload operation canceled by the user.')
        }
      }
    },
    handleEditName() {
      this.editable = !this.editable
    },
    updateAttachment() {
      this.$message.success('修改')
      this.editable = false
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
