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
                    <a-input v-model="list.params.keyword" allowClear @keyup.enter="handleQuery" />
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="分组：">
                    <a-select v-model="list.params.team" allowClear @change="handleQuery()">
                      <a-select-option v-for="(item, index) in computedTeams" :key="index" :value="item">
                        {{ item }}
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
            <a-dropdown>
              <template #overlay>
                <a-menu>
                  <a-menu-item key="single" @click="handleOpenForm({})"> 添加</a-menu-item>
                  <a-menu-item key="batch" @click="attachmentSelectModal.visible = true"> 批量添加</a-menu-item>
                </a-menu>
              </template>
              <a-button icon="plus" type="primary">
                添加
                <a-icon type="down" />
              </a-button>
            </a-dropdown>
            <a-button v-show="list.selected.length" icon="check-circle" type="primary" @click="handleSelectAll">
              全选
            </a-button>
            <a-button v-show="list.selected.length" icon="delete" type="danger" @click="handleDeletePhotoInBatch">
              删除
            </a-button>
            <a-button v-show="list.selected.length" icon="delete" @click="handleOpenUpdateTeamForm">
              更改分组
            </a-button>
            <a-button v-show="list.selected.length" icon="close" @click="list.selected = []"> 取消</a-button>
          </div>
        </a-card>
      </a-col>
      <a-col :span="24">
        <a-list
          :dataSource="list.data"
          :grid="{ gutter: 6, xs: 2, sm: 2, md: 4, lg: 6, xl: 6, xxl: 6 }"
          :loading="list.loading"
          class="photos-group"
        >
          <template #renderItem="item, index">
            <a-list-item
              :key="index"
              @click="handleItemClick(item)"
              @mouseenter="$set(item, 'hover', true)"
              @mouseleave="$set(item, 'hover', false)"
            >
              <div
                :class="`${isItemSelect(item) ? 'border-blue-600' : 'border-slate-200'}`"
                class="border border-solid"
              >
                <div class="photo-thumb photos-group-item">
                  <span
                    :style="`background-image:url(${encodeURI(item.thumbnail)})`"
                    class="photos-group-item-img"
                    loading="lazy"
                  />
                </div>
                <a-card-meta class="p-2 cursor-pointer">
                  <template #description>
                    <a-tooltip :title="item.name">
                      <div class="truncate">
                        <span class="mr-1">{{ item.name }}</span>
                        <span v-if="item.team" class="text-gray-500 text-xs">#{{ item.team }}</span>
                      </div>
                    </a-tooltip>
                  </template>
                </a-card-meta>

                <a-icon
                  v-show="!isItemSelect(item) && item.hover"
                  :style="{ fontSize: '18px', color: 'rgb(37 99 235)' }"
                  class="absolute top-1 right-2 font-bold cursor-pointer transition-all"
                  theme="twoTone"
                  type="plus-circle"
                  @click.stop="handleSelect(item)"
                />
                <a-icon
                  v-show="isItemSelect(item)"
                  :style="{ fontSize: '18px', color: 'rgb(37 99 235)' }"
                  class="absolute top-1 right-2 font-bold cursor-pointer transition-all"
                  theme="twoTone"
                  type="check-circle"
                />
              </div>
            </a-list-item>
          </template>
        </a-list>
      </a-col>
    </a-row>

    <div class="page-wrapper">
      <a-pagination
        :current="pagination.page"
        :defaultPageSize="pagination.size"
        :pageSizeOptions="['18', '36', '54', '72', '90', '108']"
        :total="pagination.total"
        showLessItems
        showSizeChanger
        @change="handlePageChange"
        @showSizeChange="handlePageSizeChange"
      />
    </div>

    <div style="position: fixed; bottom: 30px; right: 30px">
      <a-button icon="setting" shape="circle" size="large" type="primary" @click="optionFormVisible = true"></a-button>
    </div>

    <a-modal v-model="optionFormVisible" :afterClose="() => (optionFormVisible = false)" title="页面设置">
      <template #footer>
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

    <a-modal v-model="updateTeamForm.visible" title="更改分组">
      <a-form layout="vertical">
        <a-form-item label="分组名称：">
          <a-auto-complete
            ref="teamInput"
            v-model="updateTeamForm.team"
            :dataSource="computedTeams"
            allowClear
            style="width: 100%"
          />
        </a-form-item>
      </a-form>

      <template #footer>
        <ReactiveButton
          :errored="updateTeamForm.saveErrored"
          :loading="updateTeamForm.saving"
          erroredText="更改失败"
          loadedText="更改成功"
          text="确定"
          @callback="handleUpdateTeamInBatchCallback"
          @click="handleUpdateTeamInBatch"
        ></ReactiveButton>
        <a-button @click="updateTeamForm.visible = false">关闭</a-button>
      </template>
    </a-modal>

    <PhotoFormModal :photo="list.current" :teams="computedTeams" :visible.sync="formVisible" @succeed="onSaveSucceed">
      <template #extraFooter>
        <a-button :disabled="selectPreviousButtonDisabled" @click="handleSelectPrevious">上一项</a-button>
        <a-button :disabled="selectNextButtonDisabled" @click="handleSelectNext">下一项</a-button>
      </template>
    </PhotoFormModal>

    <AttachmentSelectModal :visible.sync="attachmentSelectModal.visible" @confirm="handleAttachmentSelected" />
  </page-view>
</template>

<script>
// components
import { PageView } from '@/layouts'
import PhotoFormModal from './components/PhotoFormModal'

import { mapActions } from 'vuex'
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import apiClient from '@/utils/api-client'

export default {
  mixins: [mixin, mixinDevice],
  components: { PageView, PhotoFormModal },
  data() {
    return {
      list: {
        data: [],
        loading: false,
        params: {
          page: 0,
          size: 18,
          sort: ['createTime,desc', 'id,asc'],
          keyword: null,
          team: undefined
        },
        total: 0,
        hasPrevious: false,
        hasNext: false,
        selected: [],
        current: {}
      },

      attachmentSelectModal: {
        visible: false
      },

      updateTeamForm: {
        team: undefined,
        visible: false,
        saving: false,
        saveErrored: false
      },

      formVisible: false,

      teams: [],
      options: [],
      optionFormVisible: false
    }
  },
  created() {
    this.handleListPhotos()
    this.handleListPhotoTeams()
    this.handleListOptions()
  },
  computed: {
    pagination() {
      return {
        page: this.list.params.page + 1,
        size: this.list.params.size,
        total: this.list.total
      }
    },
    computedTeams() {
      return this.teams.filter(item => {
        return item !== ''
      })
    },
    isItemSelect() {
      return function (photo) {
        return this.list.selected.findIndex(item => item.id === photo.id) > -1
      }
    },
    selectPreviousButtonDisabled() {
      const index = this.list.data.findIndex(photo => photo.id === this.list.current.id)
      return index === 0 && !this.list.hasPrevious
    },
    selectNextButtonDisabled() {
      const index = this.list.data.findIndex(photo => photo.id === this.list.current.id)
      return index === this.list.data.length - 1 && !this.list.hasNext
    }
  },
  methods: {
    ...mapActions(['refreshOptionsCache']),

    async handleListPhotos() {
      try {
        this.list.loading = true

        const response = await apiClient.photo.list(this.list.params)

        this.list.data = response.data.content
        this.list.total = response.data.total
        this.list.hasPrevious = response.data.hasPrevious
        this.list.hasNext = response.data.hasNext
      } catch (e) {
        this.$log.error('Failed to get photos', e)
      } finally {
        this.list.loading = false
      }
    },

    handleListPhotoTeams() {
      apiClient.photo.listTeams().then(response => {
        this.teams = response.data
      })
    },

    /**
     * Handle page change
     */
    handlePageChange(page = 1) {
      this.list.params.page = page - 1
      this.handleListPhotos()
    },

    /**
     * Handle page size change
     */
    handlePageSizeChange(current, size) {
      this.$log.debug(`Current: ${current}, PageSize: ${size}`)
      this.list.params.page = 0
      this.list.params.size = size
      this.handleListPhotos()
    },

    handleQuery() {
      this.handlePageChange(1)
    },

    handleResetParam() {
      this.list.params.keyword = undefined
      this.list.params.team = undefined
      this.handlePageChange(1)
      this.handleListPhotoTeams()
    },

    handleItemClick(photo) {
      if (this.list.selected.length <= 0) {
        this.handleOpenForm(photo)
        return
      }
      this.isItemSelect(photo) ? this.handleUnselect(photo) : this.handleSelect(photo)
    },

    handleOpenForm(photo) {
      this.list.current = photo
      this.formVisible = true
    },

    handleSelect(photo) {
      this.list.selected = [...this.list.selected, photo]
    },

    handleUnselect(photo) {
      this.list.selected = this.list.selected.filter(item => item.id !== photo.id)
    },

    handleSelectAll() {
      this.list.selected = this.list.data
    },

    async handleAttachmentSelected({ raw }) {
      if (!raw.length) {
        return
      }
      const photosToStage = raw.map(attachment => {
        return {
          name: attachment.name,
          url: attachment.path,
          thumbnail: attachment.thumbPath
        }
      })
      try {
        await apiClient.photo.createInBatch(photosToStage)
        this.$message.success('添加成功')
      } catch (e) {
        this.$log.error('Failed to create photos in batch', e)
      } finally {
        await this.handleListPhotos()
        this.handleListPhotoTeams()
      }
    },

    async handleDeletePhotoInBatch() {
      if (this.list.selected.length <= 0) {
        this.$message.warn('你还未选择任何图片，请至少选择一个！')
        return
      }
      const _this = this
      this.$confirm({
        title: '确定要批量删除选中的图片吗？',
        content: '一旦删除不可恢复，请谨慎操作',
        async onOk() {
          try {
            const photoIds = _this.list.selected.map(photo => photo.id)

            await apiClient.photo.deleteInBatch(photoIds)

            _this.list.selected = []
            _this.$message.success('删除成功')
          } catch (e) {
            _this.$log.error('Failed to delete selected photos', e)
          } finally {
            await _this.handleListPhotos()
            _this.handleListPhotoTeams()
          }
        }
      })
    },

    async handleUpdateTeamInBatch() {
      const photosToStage = this.list.selected.map(photo => {
        return {
          ...photo,
          team: this.updateTeamForm.team
        }
      })
      try {
        this.updateTeamForm.saving = true

        await apiClient.photo.updateInBatch(photosToStage)

        this.$message.success('更改成功')
      } catch (e) {
        this.updateTeamForm.saveErrored = true
        this.$log.error('Failed to change team in batch', e)
      } finally {
        setTimeout(() => {
          this.updateTeamForm.saving = false
        }, 400)
      }
    },

    handleUpdateTeamInBatchCallback() {
      if (this.updateTeamForm.saveErrored) {
        this.updateTeamForm.saveErrored = false
      } else {
        this.updateTeamForm.visible = false
        this.updateTeamForm.team = undefined
        this.list.selected = []
        this.handleListPhotos()
      }
    },

    handleOpenUpdateTeamForm() {
      this.updateTeamForm.visible = true
      this.$nextTick(() => {
        this.$refs.teamInput.focus()
      })
    },

    async onSaveSucceed(photo) {
      await this.handleListPhotos()
      this.list.current = photo
      this.handleListPhotoTeams()
    },

    handleListOptions() {
      apiClient.option.list().then(response => {
        this.options = response.data
      })
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

    /**
     * Select previous photo
     */
    async handleSelectPrevious() {
      const index = this.list.data.findIndex(item => item.id === this.list.current.id)
      if (index > 0) {
        this.list.current = this.list.data[index - 1]
        return
      }
      if (index === 0 && this.list.hasPrevious) {
        this.list.params.page--
        await this.handleListPhotos()

        this.list.current = this.list.data[this.list.data.length - 1]
      }
    },

    /**
     * Select next photo
     */
    async handleSelectNext() {
      const index = this.list.data.findIndex(item => item.id === this.list.current.id)
      if (index < this.list.data.length - 1) {
        this.list.current = this.list.data[index + 1]
        return
      }
      if (index === this.list.data.length - 1 && this.list.hasNext) {
        this.list.params.page++
        await this.handleListPhotos()

        this.list.current = this.list.data[0]
      }
    }
  }
}
</script>
