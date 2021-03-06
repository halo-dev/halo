<template>
  <page-view>
    <a-row :gutter="12" type="flex" align="middle">
      <a-col :span="24" class="pb-3">
        <a-card :bordered="false" :bodyStyle="{ padding: '16px' }">
          <div class="table-page-search-wrapper">
            <a-form layout="inline">
              <a-row :gutter="48">
                <a-col :md="6" :sm="24">
                  <a-form-item label="关键词：">
                    <a-input v-model="list.queryParam.keyword" />
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="分组：">
                    <a-select v-model="list.queryParam.team" @change="handleQuery()">
                      <a-select-option v-for="(item, index) in computedTeams" :key="index" :value="item">{{
                        item
                      }}</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <span class="table-page-search-submitButtons">
                    <a-space>
                      <a-button type="primary" @click="handleQuery()">查询</a-button>
                      <a-button @click="handleResetParam()">重置</a-button>
                    </a-space>
                  </span>
                </a-col>
              </a-row>
            </a-form>
          </div>
          <div class="mb-0 table-operator">
            <a-button type="primary" icon="plus" @click="form.visible = true">添加</a-button>
          </div>
        </a-card>
      </a-col>
      <a-col :span="24">
        <a-list
          :grid="{ gutter: 12, xs: 2, sm: 2, md: 4, lg: 6, xl: 6, xxl: 6 }"
          :dataSource="list.data"
          :loading="list.loading"
        >
          <a-list-item slot="renderItem" slot-scope="item, index" :key="index">
            <a-card :bodyStyle="{ padding: 0 }" hoverable @click="handleOpenEditForm(item)">
              <div class="photo-thumb">
                <img :src="item.thumbnail" loading="lazy" />
              </div>
              <a-card-meta class="p-3">
                <ellipsis :length="isMobile() ? 12 : 16" tooltip slot="description">{{ item.name }}</ellipsis>
              </a-card-meta>
            </a-card>
          </a-list-item>
        </a-list>
      </a-col>
    </a-row>
    <div class="page-wrapper">
      <a-pagination
        :current="list.pagination.page"
        :total="list.pagination.total"
        :defaultPageSize="list.pagination.size"
        :pageSizeOptions="['18', '36', '54', '72', '90', '108']"
        showSizeChanger
        @change="handlePaginationChange"
        @showSizeChange="handlePaginationChange"
        showLessItems
      />
    </div>
    <div style="position: fixed;bottom: 30px;right: 30px;">
      <a-button type="primary" shape="circle" icon="setting" size="large" @click="optionFormVisible = true"></a-button>
    </div>
    <a-modal v-model="optionFormVisible" title="页面设置" :afterClose="() => (optionFormVisible = false)">
      <template slot="footer">
        <a-button key="submit" type="primary" @click="handleSaveOptions()">保存</a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item label="页面标题：" help="* 需要主题进行适配">
          <a-input v-model="options.photos_title" />
        </a-form-item>
        <a-form-item label="每页显示条数：">
          <a-input-number v-model="options.photos_page_size" style="width:100%" />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-drawer
      :title="`图片${form.model.id ? '修改' : '添加'}`"
      :width="isMobile() ? '100%' : '480'"
      closable
      :visible="form.visible"
      destroyOnClose
      @close="onDrawerClose"
    >
      <a-form-model ref="photoForm" :model="form.model" :rules="form.rules" layout="vertical">
        <a-form-model-item prop="url" label="图片地址：">
          <div class="pb-2">
            <img
              :src="form.model.url || '/images/placeholder.jpg'"
              @click="attachmentSelectDrawer.visible = true"
              class="w-full cursor-pointer"
              style="border-radius:4px"
            />
          </div>
          <a-input v-model="form.model.url" placeholder="点击封面图选择图片，或者输入外部链接" />
        </a-form-model-item>
        <a-form-model-item prop="thumbnail" label="缩略图地址：">
          <a-input v-model="form.model.thumbnail" />
        </a-form-model-item>
        <a-form-model-item prop="name" label="图片名称：">
          <a-input v-model="form.model.name" />
        </a-form-model-item>
        <a-form-model-item prop="takeTime" label="拍摄日期：">
          <a-date-picker
            showTime
            :defaultValue="takeTimeDefaultValue"
            format="YYYY-MM-DD HH:mm:ss"
            style="width:100%"
            @change="onTakeTimeChange"
            @ok="onTakeTimeSelect"
          />
        </a-form-model-item>
        <a-form-model-item prop="location" label="拍摄地点：">
          <a-input v-model="form.model.location" />
        </a-form-model-item>
        <a-form-model-item prop="team" label="分组：">
          <a-auto-complete :dataSource="computedTeams" v-model="form.model.team" allowClear style="width:100%" />
        </a-form-model-item>
        <a-form-model-item prop="description" label="描述：">
          <a-input v-model="form.model.description" type="textarea" :autoSize="{ minRows: 5 }" />
        </a-form-model-item>
      </a-form-model>
      <a-divider class="divider-transparent" />
      <div class="bottom-control">
        <a-space>
          <ReactiveButton
            @click="handleCreateOrUpdate"
            @callback="handleCreateOrUpdateCallback"
            :loading="form.saving"
            :errored="form.saveErrored"
            :text="`${form.model.id ? '修改' : '添加'}`"
            :loadedText="`${form.model.id ? '修改' : '添加'}成功`"
            :erroredText="`${form.model.id ? '修改' : '添加'}失败`"
          ></ReactiveButton>
          <a-popconfirm
            title="你确定要删除该图片？"
            okText="确定"
            cancelText="取消"
            @confirm="handleDelete"
            v-if="form.model.id"
          >
            <ReactiveButton
              type="danger"
              @click="() => {}"
              @callback="handleDeleteCallback"
              :loading="form.deleting"
              :errored="form.deleteErrored"
              text="删除"
              loadedText="删除成功"
              erroredText="删除失败"
            ></ReactiveButton>
          </a-popconfirm>
        </a-space>
      </div>

      <AttachmentSelectDrawer
        v-model="attachmentSelectDrawer.visible"
        @listenToSelect="handleAttachmentSelected"
        :drawerWidth="480"
      />
    </a-drawer>
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import { mapActions } from 'vuex'
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import photoApi from '@/api/photo'
import optionApi from '@/api/option'
import { datetimeFormat } from '@/utils/datetime'

export default {
  mixins: [mixin, mixinDevice],
  components: { PageView },
  data() {
    return {
      list: {
        data: [],
        loading: false,
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
        }
      },

      form: {
        model: {},
        visible: false,
        rules: {
          url: [{ required: true, message: '* 图片地址不能为空', trigger: ['change'] }],
          thumbnail: [{ required: true, message: '* 缩略图地址不能为空', trigger: ['change'] }],
          name: [{ required: true, message: '* 图片名称不能为空', trigger: ['change'] }]
        },
        saving: false,
        saveErrored: false,
        deleting: false,
        deleteErrored: false
      },

      attachmentSelectDrawer: {
        visible: false
      },

      optionFormVisible: false,
      teams: [],
      options: []
    }
  },
  created() {
    this.hanldeListPhotos()
    this.hanldeListPhotoTeams()
    this.hanldeListOptions()
  },
  computed: {
    takeTimeDefaultValue() {
      if (this.form.model.takeTime) {
        var date = new Date(this.form.model.takeTime)
        return datetimeFormat(date, 'YYYY-MM-DD HH:mm:ss')
      }
      return datetimeFormat(new Date(), 'YYYY-MM-DD HH:mm:ss')
    },
    computedTeams() {
      return this.teams.filter(item => {
        return item !== ''
      })
    }
  },
  methods: {
    ...mapActions(['refreshOptionsCache']),
    hanldeListPhotos() {
      this.list.loading = true
      this.list.queryParam.page = this.list.pagination.page - 1
      this.list.queryParam.size = this.list.pagination.size
      this.list.queryParam.sort = this.list.pagination.sort
      photoApi
        .query(this.list.queryParam)
        .then(response => {
          this.list.data = response.data.data.content
          this.list.pagination.total = response.data.data.total
        })
        .finally(() => {
          setTimeout(() => {
            this.list.loading = false
          }, 200)
        })
    },
    handleQuery() {
      this.handlePaginationChange(1, this.list.pagination.size)
    },
    hanldeListOptions() {
      optionApi.listAll().then(response => {
        this.options = response.data.data
      })
    },
    hanldeListPhotoTeams() {
      photoApi.listTeams().then(response => {
        this.teams = response.data.data
      })
    },
    handleCreateOrUpdate() {
      const _this = this
      _this.$refs.photoForm.validate(valid => {
        if (valid) {
          _this.form.saving = true
          if (_this.form.model.id) {
            photoApi
              .update(_this.form.model.id, _this.form.model)
              .catch(() => {
                _this.form.saveErrored = true
              })
              .finally(() => {
                setTimeout(() => {
                  _this.form.saving = false
                }, 400)
              })
          } else {
            photoApi
              .create(_this.form.model)
              .catch(() => {
                _this.form.saveErrored = true
              })
              .finally(() => {
                setTimeout(() => {
                  _this.form.saving = false
                }, 400)
              })
          }
        }
      })
    },
    handleCreateOrUpdateCallback() {
      if (this.form.saveErrored) {
        this.form.saveErrored = false
      } else {
        this.form.model = {}
        this.form.visible = false
        this.hanldeListPhotos()
        this.hanldeListPhotoTeams()
      }
    },
    handleOpenEditForm(photo) {
      this.form.model = photo
      this.form.visible = true
    },
    handlePaginationChange(page, size) {
      this.$log.debug(`Current: ${page}, PageSize: ${size}`)
      this.list.pagination.page = page
      this.list.pagination.size = size
      this.hanldeListPhotos()
    },
    handleDelete() {
      this.form.deleting = true
      photoApi
        .delete(this.form.model.id)
        .catch(() => {
          this.form.deleteErrored = true
        })
        .finally(() => {
          setTimeout(() => {
            this.form.deleting = false
          }, 400)
        })
    },
    handleDeleteCallback() {
      if (this.form.deleteErrored) {
        this.form.deleteErrored = false
      } else {
        this.form.model = {}
        this.form.visible = false
        this.hanldeListPhotos()
        this.hanldeListPhotoTeams()
      }
    },
    handleAttachmentSelected(data) {
      this.form.model.url = encodeURI(data.path)
      this.form.model.thumbnail = encodeURI(data.thumbPath)
      this.attachmentSelectDrawer.visible = false
    },
    handleResetParam() {
      this.list.queryParam.keyword = null
      this.list.queryParam.team = null
      this.handlePaginationChange(1, this.list.pagination.size)
      this.hanldeListPhotoTeams()
    },
    onDrawerClose() {
      this.form.visible = false
      this.form.model = {}
    },
    handleSaveOptions() {
      optionApi
        .save(this.options)
        .then(() => {
          this.$message.success('保存成功！')
          this.optionFormVisible = false
        })
        .finally(() => {
          this.hanldeListOptions()
          this.refreshOptionsCache()
        })
    },
    onTakeTimeChange(value) {
      this.form.model.takeTime = value.valueOf()
    },
    onTakeTimeSelect(value) {
      this.form.model.takeTime = value.valueOf()
    }
  }
}
</script>
