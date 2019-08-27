<template>
  <page-view>
    <a-row
      :gutter="12"
      type="flex"
      align="middle"
    >
      <a-col
        :span="24"
        class="search-box"
      >
        <a-card :bordered="false">
          <div class="table-page-search-wrapper">
            <a-form layout="inline">
              <a-row :gutter="48">
                <a-col
                  :md="6"
                  :sm="24"
                >
                  <a-form-item label="关键词">
                    <a-input v-model="queryParam.keyword" />
                  </a-form-item>
                </a-col>
                <a-col
                  :md="6"
                  :sm="24"
                >
                  <a-form-item label="存储位置">
                    <a-select
                      v-model="queryParam.attachmentType"
                      @change="handleQuery"
                    >
                      <a-select-option
                        v-for="item in Object.keys(attachmentType)"
                        :key="item"
                        :value="item"
                      >{{ attachmentType[item].text }}</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col
                  :md="6"
                  :sm="24"
                >
                  <a-form-item label="文件类型">
                    <a-select
                      v-model="queryParam.mediaType"
                      @change="handleQuery"
                    >
                      <a-select-option
                        v-for="(item,index) in mediaTypes"
                        :key="index"
                        :value="item"
                      >{{ item }}</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col
                  :md="6"
                  :sm="24"
                >
                  <span class="table-page-search-submitButtons">
                    <a-button
                      type="primary"
                      @click="handleQuery"
                    >查询</a-button>
                    <a-button
                      style="margin-left: 8px;"
                      @click="handleResetParam"
                    >重置</a-button>
                  </span>
                </a-col>
              </a-row>
            </a-form>
          </div>
          <div class="table-operator">
            <a-button
              type="primary"
              icon="plus"
              @click="()=>this.uploadVisible = true"
            >上传</a-button>
          </div>
        </a-card>
      </a-col>
      <a-col :span="24">
        <a-list
          :grid="{ gutter: 12, xs: 1, sm: 2, md: 4, lg: 6, xl: 6, xxl: 6 }"
          :dataSource="formattedDatas"
          :loading="listLoading"
        >
          <a-list-item
            slot="renderItem"
            slot-scope="item, index"
            :key="index"
          >
            <a-card
              :bodyStyle="{ padding: 0 }"
              hoverable
              @click="handleShowDetailDrawer(item)"
            >
              <div class="attach-thumb">
                <span v-show="!handleJudgeMediaType(item)">当前格式不支持预览</span>
                <img :src="item.thumbPath" v-show="handleJudgeMediaType(item)">
              </div>
              <a-card-meta>
                <ellipsis
                  :length="isMobile()?36:16"
                  tooltip
                  slot="description"
                >{{ item.name }}</ellipsis>
              </a-card-meta>
            </a-card>
          </a-list-item>
        </a-list>
      </a-col>
    </a-row>
    <div class="page-wrapper">
      <a-pagination
        class="pagination"
        :total="pagination.total"
        :defaultPageSize="pagination.size"
        :pageSizeOptions="['18', '36', '54','72','90','108']"
        showSizeChanger
        @change="handlePaginationChange"
        @showSizeChange="handlePaginationChange"
      />
    </div>
    <a-modal
      title="上传附件"
      v-model="uploadVisible"
      :footer="null"
      :afterClose="onUploadClose"
    >
      <upload
        name="file"
        multiple
        :uploadHandler="uploadHandler"
      >
        <p class="ant-upload-drag-icon">
          <a-icon type="inbox" />
        </p>
        <p class="ant-upload-text">点击选择文件或将文件拖拽到此处</p>
        <p class="ant-upload-hint">支持单个或批量上传</p>
      </upload>
    </a-modal>
    <AttachmentDetailDrawer
      v-model="drawerVisible"
      v-if="selectAttachment"
      :attachment="selectAttachment"
      :addToPhoto="true"
      @delete="()=>this.loadAttachments()"
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
      attachmentType: attachmentApi.type,
      listLoading: true,
      uploadVisible: false,
      selectAttachment: {},
      attachments: [],
      mediaTypes: [],
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
        keyword: null,
        mediaType: null,
        attachmentType: null
      },
      uploadHandler: attachmentApi.upload,
      drawerVisible: false
    }
  },
  computed: {
    formattedDatas() {
      return this.attachments.map(attachment => {
        attachment.typeProperty = this.attachmentType[attachment.type]
        return attachment
      })
    }
  },
  created() {
    this.loadAttachments()
    this.loadMediaTypes()
  },
  methods: {
    loadAttachments() {
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      this.listLoading = true
      attachmentApi.query(this.queryParam).then(response => {
        this.attachments = response.data.data.content
        this.pagination.total = response.data.data.total
        this.listLoading = false
      })
    },
    loadMediaTypes() {
      attachmentApi.getMediaTypes().then(response => {
        this.mediaTypes = response.data.data
      })
    },
    handleShowDetailDrawer(attachment) {
      this.selectAttachment = attachment
      this.drawerVisible = true
    },
    handlePaginationChange(page, size) {
      this.$log.debug(`Current: ${page}, PageSize: ${size}`)
      this.pagination.page = page
      this.pagination.size = size
      this.loadAttachments()
    },
    handleResetParam() {
      this.queryParam.keyword = null
      this.queryParam.mediaType = null
      this.queryParam.attachmentType = null
      this.loadAttachments()
    },
    handleQuery() {
      this.queryParam.page = 0
      this.loadAttachments()
    },
    onUploadClose() {
      this.loadAttachments()
      this.loadMediaTypes()
    },
    handleJudgeMediaType(attachment) {
      var mediaType = attachment.mediaType
      // 判断文件类型
      if (mediaType) {
        var prefix = mediaType.split('/')[0]

        if (prefix === 'image') {
          // 是图片
          return true
        } else {
          // 非图片
          return false
        }
      }
      // 没有获取到文件返回false
      return false
    }
  }
}
</script>

<style lang="less" scoped>
.ant-divider-horizontal {
  margin: 24px 0 12px 0;
}

.search-box {
  padding-bottom: 12px;
}

.attach-thumb {
  width: 100%;
  margin: 0 auto;
  position: relative;
  padding-bottom: 56%;
  overflow: hidden;
  img, span{
    width: 100%;
    height: 100%;
    position: absolute;
    top: 0;
    left: 0;
  }
  span {
    display: flex;
    font-size: 12px;
    align-items: center;
    justify-content: center;
    color: #9b9ea0;
  }
}

.ant-card-meta {
  padding: 0.8rem;
}

.attach-detail-img img {
  width: 100%;
}

.table-operator {
  margin-bottom: 0;
}
</style>
