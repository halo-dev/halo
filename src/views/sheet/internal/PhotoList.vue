<template>
  <div>
    <a-row
      :gutter="12"
      type="flex"
      align="middle"
    >
      <a-col
        :span="24"
        style="padding-bottom: 12px;"
      >
        <a-card
          :bordered="false"
          :bodyStyle="{ padding: '16px' }"
        >
          <div class="table-page-search-wrapper">
            <a-form layout="inline">
              <a-row :gutter="48">
                <a-col
                  :md="6"
                  :sm="24"
                >
                  <a-form-item label="关键词：">
                    <a-input v-model="queryParam.keyword" />
                  </a-form-item>
                </a-col>
                <a-col
                  :md="6"
                  :sm="24"
                >
                  <a-form-item label="分组：">
                    <a-select
                      v-model="queryParam.team"
                      @change="handleQuery()"
                    >
                      <a-select-option
                        v-for="(item,index) in teams"
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
                      @click="handleQuery()"
                    >查询</a-button>
                    <a-button
                      style="margin-left: 8px;"
                      @click="resetParam()"
                    >重置</a-button>
                  </span>
                </a-col>
              </a-row>
            </a-form>
          </div>
          <div
            class="table-operator"
            style="margin-bottom: 0;"
          >
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
          :grid="{ gutter: 12, xs: 2, sm: 2, md: 4, lg: 6, xl: 6, xxl: 6 }"
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
                <img
                  :src="item.thumbnail"
                  loading="lazy"
                >
              </div>
              <a-card-meta style="padding: 0.8rem;">
                <ellipsis
                  :length="isMobile()?12:16"
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
        :current="pagination.page"
        :total="pagination.total"
        :defaultPageSize="pagination.size"
        :pageSizeOptions="['18', '36', '54','72','90','108']"
        showSizeChanger
        @change="handlePaginationChange"
        @showSizeChange="handlePaginationChange"
      />
    </div>
    <div style="position: fixed;bottom: 30px;right: 30px;">
      <a-button
        type="primary"
        shape="circle"
        icon="setting"
        size="large"
        @click="optionFormVisible=true"
      ></a-button>
    </div>
    <a-modal
      v-model="optionFormVisible"
      title="页面设置"
      :afterClose="onOptionFormClose"
    >
      <template slot="footer">
        <a-button
          key="submit"
          type="primary"
          @click="handleSaveOptions()"
        >保存</a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item label="页面标题：" help="* 需要主题进行适配">
          <a-input v-model="options.photos_title" />
        </a-form-item>
        <a-form-item label="每页显示条数：">
          <a-input
            type="number"
            v-model="options.photos_page_size"
          />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-drawer
      title="图片详情"
      :width="isMobile()?'100%':'480'"
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
                :src="photo.url || '/images/placeholder.jpg'"
                @click="showThumbDrawer"
                style="width: 100%;"
              >
            </div>
          </a-skeleton>
        </a-col>
        <a-divider style="margin: 24px 0 12px 0;" />

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
                    <a-auto-complete
                      :dataSource="teams"
                      v-model="photo.team"
                      allowClear
                      style="width:100%"
                    />
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
                      :autoSize="{ minRows: 5 }"
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
        :drawerWidth="480"
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
import { mapActions } from 'vuex'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import AttachmentSelectDrawer from '../../attachment/components/AttachmentSelectDrawer'
import photoApi from '@/api/photo'
import optionApi from '@/api/option'

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
      optionFormVisible: false,
      photo: {},
      photos: [],
      teams: [],
      editable: false,
      pagination: {
        page: 1,
        size: 18,
        sort: null,
        total: 1
      },
      queryParam: {
        page: 0,
        size: 18,
        sort: null,
        keyword: null,
        team: null
      },
      options: []
    }
  },
  created() {
    this.loadPhotos()
    this.loadTeams()
    this.loadFormOptions()
  },
  methods: {
    ...mapActions(['loadOptions']),
    loadPhotos() {
      this.listLoading = true
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      photoApi.query(this.queryParam).then(response => {
        this.photos = response.data.data.content
        this.pagination.total = response.data.data.total
        this.listLoading = false
      })
    },
    loadFormOptions() {
      optionApi.listAll().then(response => {
        this.options = response.data.data
      })
    },
    handleQuery() {
      this.handlePaginationChange(1, this.pagination.size)
    },
    loadTeams() {
      photoApi.listTeams().then(response => {
        this.teams = response.data.data
      })
    },
    handleCreateOrUpdate() {
      if (this.photo.id) {
        photoApi.update(this.photo.id, this.photo).then(response => {
          this.$message.success('照片更新成功！')
          this.loadPhotos()
          this.loadTeams()
        })
      } else {
        photoApi.create(this.photo).then(response => {
          this.$message.success('照片添加成功！')
          this.loadPhotos()
          this.loadTeams()
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
        this.loadTeams()
      })
    },
    showThumbDrawer() {
      this.thumDrawerVisible = true
    },
    selectPhotoThumb(data) {
      this.photo.url = encodeURI(data.path)
      this.photo.thumbnail = encodeURI(data.thumbPath)
      this.thumDrawerVisible = false
    },
    resetParam() {
      this.queryParam.keyword = null
      this.queryParam.team = null
      this.handlePaginationChange(1, this.pagination.size)
      this.loadTeams()
    },
    onDrawerClose() {
      this.drawerVisible = false
      this.photo = {}
      this.editable = false
    },
    handleSaveOptions() {
      optionApi.save(this.options).then(response => {
        this.loadFormOptions()
        this.loadOptions()
        this.$message.success('保存成功！')
        this.optionFormVisible = false
      })
    },
    onOptionFormClose() {
      this.optionFormVisible = false
    }
  }
}
</script>
