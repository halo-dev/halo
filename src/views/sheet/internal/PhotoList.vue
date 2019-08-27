<template>
  <div class="page-header-index-wide">
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
                  <a-form-item label="分组">
                    <a-select>
                      <a-select-option value="11">11</a-select-option>
                      <a-select-option value="22">22</a-select-option>
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
                      @click="loadPhotos(true)"
                    >查询</a-button>
                    <a-button
                      style="margin-left: 8px;"
                      @click="resetParam"
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
              @click="handleAddClick"
            >添加</a-button>
          </div>
        </a-card>
      </a-col>
      <a-col :span="24">
        <a-list
          :grid="{ gutter: 12, xs: 1, sm: 2, md: 4, lg: 6, xl: 6, xxl: 6 }"
          :dataSource="photos"
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
              @click="showDrawer(item)"
            >
              <div class="photo-thumb">
                <img :src="item.thumbnail">
              </div>
              <a-card-meta>
                <ellipsis
                  :length="isMobile()?36:18"
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
        :total="pagination.total"
        :defaultPageSize="pagination.size"
        :pageSizeOptions="['18', '36', '54','72','90','108']"
        showSizeChanger
        @change="handlePaginationChange"
        @showSizeChange="handlePaginationChange"
      />
    </div>
    <a-drawer
      title="图片详情"
      :width="isMobile()?'100%':'460'"
      closable
      :visible="drawerVisible"
      destroyOnClose
      @close="onDrawerClose"
    >
      <a-row
        type="flex"
        align="middle"
      >
        <a-col :span="24">
          <a-skeleton
            active
            :loading="drawerLoading"
            :paragraph="{rows: 8}"
          >
            <div class="photo-detail-img">
              <img
                :src="photo.url || '//i.loli.net/2019/05/05/5ccf007c0a01d.png'"
                @click="showThumbDrawer"
              >
            </div>
          </a-skeleton>
        </a-col>
        <a-divider />
        <a-col :span="24">
          <a-skeleton
            active
            :loading="drawerLoading"
            :paragraph="{rows: 8}"
          >
            <a-list itemLayout="horizontal">
              <a-list-item>
                <a-list-item-meta>
                  <template
                    slot="description"
                    v-if="editable"
                  >
                    <a-input v-model="photo.thumbnail" />
                  </template>
                  <template
                    slot="description"
                    v-else
                  >{{ photo.thumbnail }}</template>
                  <span slot="title">
                    缩略图地址：
                  </span>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-list-item-meta>
                  <template
                    slot="description"
                    v-if="editable"
                  >
                    <a-input v-model="photo.name" />
                  </template>
                  <template
                    slot="description"
                    v-else
                  >{{ photo.name }}</template>
                  <span slot="title">
                    图片名称：
                  </span>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-list-item-meta>
                  <template
                    slot="description"
                    v-if="editable"
                  >
                    <a-date-picker
                      v-model="photo.takeTime"
                      style="width:100%"
                    />
                  </template>
                  <span
                    slot="description"
                    v-else
                  >{{ photo.takeTime | moment }}</span>
                  <span slot="title">拍摄日期：</span>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-list-item-meta>
                  <template
                    slot="description"
                    v-if="editable"
                  >
                    <a-input v-model="photo.location" />
                  </template>
                  <span
                    slot="description"
                    v-else
                  >{{ photo.location || '无' }}</span>
                  <span slot="title">拍摄地点：</span>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-list-item-meta>
                  <template
                    slot="description"
                    v-if="editable"
                  >
                    <a-input v-model="photo.team" />
                  </template>
                  <span
                    slot="description"
                    v-else
                  >{{ photo.team || '无' }}</span>
                  <span slot="title">分组：</span>
                </a-list-item-meta>
              </a-list-item>
              <a-list-item>
                <a-list-item-meta>
                  <template
                    slot="description"
                    v-if="editable"
                  >
                    <a-input
                      v-model="photo.description"
                      type="textarea"
                      :autosize="{ minRows: 5 }"
                    />
                  </template>
                  <span
                    slot="description"
                    v-else
                  >{{ photo.description || '无' }}</span>
                  <span slot="title">描述：</span>
                </a-list-item-meta>
              </a-list-item>
            </a-list>
          </a-skeleton>
        </a-col>
      </a-row>
      <AttachmentSelectDrawer
        v-model="thumDrawerVisible"
        @listenToSelect="selectPhotoThumb"
        :drawerWidth="460"
      />
      <a-divider class="divider-transparent" />
      <div class="bottom-control">
        <a-button
          type="dashed"
          style="marginRight: 8px"
          @click="handleEditClick"
          v-if="!editable"
        >编辑</a-button>
        <a-button
          type="primary"
          style="marginRight: 8px"
          @click="handleCreateOrUpdate"
          v-else
        >保存</a-button>
        <a-popconfirm
          title="你确定要删除该图片？"
          okText="确定"
          cancelText="取消"
          @confirm="handleDeletePhoto"
        >
          <a-button type="danger">删除</a-button>
        </a-popconfirm>
      </div>
    </a-drawer>
  </div>
</template>

<script>
import AttachmentSelectDrawer from '../../attachment/components/AttachmentSelectDrawer'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import photoApi from '@/api/photo'

export default {
  components: {
    AttachmentSelectDrawer
  },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      drawerVisible: false,
      drawerLoading: false,
      listLoading: true,
      thumDrawerVisible: false,
      photo: {},
      photos: [],
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
    this.loadPhotos()
  },
  methods: {
    loadPhotos(isSearch) {
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      if (isSearch) {
        this.queryParam.page = 0
      }
      this.listLoading = true
      photoApi.query(this.queryParam).then(response => {
        this.photos = response.data.data.content
        this.pagination.total = response.data.data.total
        this.listLoading = false
      })
    },
    handleCreateOrUpdate() {
      if (this.photo.id) {
        photoApi.update(this.photo.id, this.photo).then(response => {
          this.$message.success('照片更新成功！')
          this.loadPhotos()
        })
      } else {
        photoApi.create(this.photo).then(response => {
          this.$message.success('照片添加成功！')
          this.loadPhotos()
          this.photo = response.data.data
        })
      }
      this.editable = false
    },
    showDrawer(photo) {
      this.photo = photo
      this.drawerVisible = true
    },
    handlePaginationChange(page, size) {
      this.$log.debug(`Current: ${page}, PageSize: ${size}`)
      this.pagination.page = page
      this.pagination.size = size
      this.loadPhotos()
    },
    handleAddClick() {
      this.editable = true
      this.drawerVisible = true
    },
    handleEditClick() {
      this.editable = true
    },
    handleDeletePhoto() {
      photoApi.delete(this.photo.id).then(response => {
        this.$message.success('删除成功！')
        this.onDrawerClose()
        this.loadPhotos()
      })
    },
    showThumbDrawer() {
      this.thumDrawerVisible = true
    },
    selectPhotoThumb(data) {
      this.photo.url = encodeURI(data.path)
      this.thumDrawerVisible = false
    },
    resetParam() {
      this.queryParam.keyword = null
      this.loadPhotos()
    },
    onDrawerClose() {
      this.drawerVisible = false
      this.photo = {}
      this.editable = false
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

.photo-thumb {
  width: 100%;
  margin: 0 auto;
  position: relative;
  padding-bottom: 56%;
  overflow: hidden;
  img {
    width: 100%;
    height: 100%;
    position: absolute;
    top: 0;
    left: 0;
  }
}

.ant-card-meta {
  padding: 0.8rem;
}

.photo-detail-img img {
  width: 100%;
}

.table-operator {
  margin-bottom: 0;
}
</style>
