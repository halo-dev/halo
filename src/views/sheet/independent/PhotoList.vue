<template>
  <page-view>
    <a-row :gutter="12" align="middle" type="flex">
      <a-col :span="24" class="pb-3">
        <a-card :bodyStyle="{ padding: '16px' }" :bordered="false">
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
                      <a-select-option v-for="(item, index) in computedTeams" :key="index" :value="item"
                        >{{ item }}
                      </a-select-option>
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
            <a-button icon="plus" type="primary" @click="form.visible = true">添加</a-button>
          </div>
        </a-card>
      </a-col>
      <a-col :span="24">
        <a-list
          :dataSource="list.data"
          :grid="{ gutter: 12, xs: 2, sm: 2, md: 4, lg: 6, xl: 6, xxl: 6 }"
          :loading="list.loading"
        >
          <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
            <a-card :bodyStyle="{ padding: 0 }" hoverable @click="handleOpenEditForm(item)">
              <div class="photo-thumb">
                <img :src="item.thumbnail" loading="lazy" />
              </div>
              <a-card-meta class="p-3">
                <ellipsis slot="description" :length="isMobile() ? 12 : 16" tooltip>{{ item.name }}</ellipsis>
              </a-card-meta>
            </a-card>
          </a-list-item>
        </a-list>
      </a-col>
    </a-row>
    <div class="page-wrapper">
      <a-pagination
        :current="list.pagination.page"
        :defaultPageSize="list.pagination.size"
        :pageSizeOptions="['18', '36', '54', '72', '90', '108']"
        :total="list.pagination.total"
        showLessItems
        showSizeChanger
        @change="handlePaginationChange"
        @showSizeChange="handlePaginationChange"
      />
    </div>
    <div style="position: fixed; bottom: 30px; right: 30px">
      <a-button icon="setting" shape="circle" size="large" type="primary" @click="optionFormVisible = true"></a-button>
    </div>
    <a-modal v-model="optionFormVisible" :afterClose="() => (optionFormVisible = false)" title="页面设置">
      <template slot="footer">
        <a-button key="submit" type="primary" @click="handleSaveOptions()">保存</a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item help="* 需要主题进行适配" label="页面标题：">
          <a-input v-model="options.photos_title" />
        </a-form-item>
        <a-form-item label="每页显示条数：">
          <a-input-number v-model="options.photos_page_size" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-drawer
      :title="`图片${form.model.id ? '修改' : '添加'}`"
      :visible="form.visible"
      :width="isMobile() ? '100%' : '480'"
      closable
      destroyOnClose
      @close="onDrawerClose"
    >
      <a-form-model ref="photoForm" :model="form.model" :rules="form.rules" layout="vertical">
        <a-form-model-item label="图片地址：" prop="url">
          <div class="pb-2">
            <img
              :src="form.model.url || '/images/placeholder.jpg'"
              class="w-full cursor-pointer"
              style="border-radius: 4px"
              @click="attachmentSelectModal.visible = true"
            />
          </div>
          <a-input v-model="form.model.url" placeholder="点击封面图选择图片，或者输入外部链接" />
        </a-form-model-item>
        <a-form-model-item label="缩略图地址：" prop="thumbnail">
          <a-input v-model="form.model.thumbnail" />
        </a-form-model-item>
        <a-form-model-item label="图片名称：" prop="name">
          <a-input v-model="form.model.name" />
        </a-form-model-item>
        <a-form-model-item label="拍摄日期：" prop="takeTime">
          <a-date-picker
            :defaultValue="takeTimeDefaultValue"
            format="YYYY-MM-DD HH:mm:ss"
            showTime
            style="width: 100%"
            @change="onTakeTimeChange"
            @ok="onTakeTimeSelect"
          />
        </a-form-model-item>
        <a-form-model-item label="拍摄地点：" prop="location">
          <a-input v-model="form.model.location" />
        </a-form-model-item>
        <a-form-model-item label="分组：" prop="team">
          <a-auto-complete v-model="form.model.team" :dataSource="computedTeams" allowClear style="width: 100%" />
        </a-form-model-item>
        <a-form-model-item label="描述：" prop="description">
          <a-input v-model="form.model.description" :autoSize="{ minRows: 5 }" type="textarea" />
        </a-form-model-item>
      </a-form-model>
      <a-divider class="divider-transparent" />
      <div class="bottom-control">
        <a-space>
          <ReactiveButton
            :errored="form.saveErrored"
            :erroredText="`${form.model.id ? '修改' : '添加'}失败`"
            :loadedText="`${form.model.id ? '修改' : '添加'}成功`"
            :loading="form.saving"
            :text="`${form.model.id ? '修改' : '添加'}`"
            @callback="handleCreateOrUpdateCallback"
            @click="handleCreateOrUpdate"
          ></ReactiveButton>
          <a-popconfirm
            v-if="form.model.id"
            cancelText="取消"
            okText="确定"
            title="你确定要删除该图片？"
            @confirm="handleDelete"
          >
            <ReactiveButton
              :errored="form.deleteErrored"
              :loading="form.deleting"
              erroredText="删除失败"
              loadedText="删除成功"
              text="删除"
              type="danger"
              @callback="handleDeleteCallback"
              @click="() => {}"
            ></ReactiveButton>
          </a-popconfirm>
        </a-space>
      </div>
      <AttachmentSelectModal
        :multiSelect="false"
        :visible.sync="attachmentSelectModal.visible"
        @confirm="handleAttachmentSelected"
      />
    </a-drawer>
  </page-view>
</template>

<script>
import { PageView } from '@/layouts'
import { mapActions } from 'vuex'
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import apiClient from '@/utils/api-client'
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

      attachmentSelectModal: {
        visible: false
      },

      optionFormVisible: false,
      teams: [],
      options: []
    }
  },
  created() {
    this.handleListPhotos()
    this.handleListPhotoTeams()
    this.handleListOptions()
  },
  computed: {
    takeTimeDefaultValue() {
      if (this.form.model.takeTime) {
        const date = new Date(this.form.model.takeTime)
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
    handleListPhotos() {
      this.list.loading = true
      this.list.queryParam.page = this.list.pagination.page - 1
      this.list.queryParam.size = this.list.pagination.size
      this.list.queryParam.sort = this.list.pagination.sort
      apiClient.photo
        .list(this.list.queryParam)
        .then(response => {
          this.list.data = response.data.content
          this.list.pagination.total = response.data.total
        })
        .finally(() => {
          this.list.loading = false
        })
    },
    handleQuery() {
      this.handlePaginationChange(1, this.list.pagination.size)
    },
    handleListOptions() {
      apiClient.option.list().then(response => {
        this.options = response.data
      })
    },
    handleListPhotoTeams() {
      apiClient.photo.listTeams().then(response => {
        this.teams = response.data
      })
    },
    handleCreateOrUpdate() {
      const _this = this
      _this.$refs.photoForm.validate(valid => {
        if (valid) {
          _this.form.saving = true
          if (_this.form.model.id) {
            apiClient.photo
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
            apiClient.photo
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
        this.handleListPhotos()
        this.handleListPhotoTeams()
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
      this.handleListPhotos()
    },
    handleDelete() {
      this.form.deleting = true
      apiClient.photo
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
        this.handleListPhotos()
        this.handleListPhotoTeams()
      }
    },
    handleAttachmentSelected({ raw }) {
      if (raw.length) {
        this.form.model.url = encodeURI(raw[0].path)
        this.form.model.thumbnail = encodeURI(raw[0].thumbPath)
      }
      this.attachmentSelectModal.visible = false
    },
    handleResetParam() {
      this.list.queryParam.keyword = null
      this.list.queryParam.team = null
      this.handlePaginationChange(1, this.list.pagination.size)
      this.handleListPhotoTeams()
    },
    onDrawerClose() {
      this.form.visible = false
      this.form.model = {}
    },
    handleSaveOptions() {
      apiClient.option
        .save(this.options)
        .then(() => {
          this.$message.success('保存成功！')
          this.optionFormVisible = false
        })
        .finally(() => {
          this.handleListOptions()
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
