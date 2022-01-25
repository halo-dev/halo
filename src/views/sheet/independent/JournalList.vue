<template>
  <page-view>
    <a-row>
      <a-col :span="24">
        <a-card :bodyStyle="{ padding: '16px' }" :bordered="false">
          <div class="table-page-search-wrapper">
            <a-form layout="inline">
              <a-row :gutter="48">
                <a-col :md="6" :sm="24">
                  <a-form-item label="关键词：">
                    <a-input v-model="list.params.keyword" @keyup.enter="handleQuery()" />
                  </a-form-item>
                </a-col>
                <a-col :md="6" :sm="24">
                  <a-form-item label="状态：">
                    <a-select v-model="list.params.type" placeholder="请选择状态" @change="handleQuery()">
                      <a-select-option v-for="type in Object.keys(list.journalType)" :key="type" :value="type">
                        {{ list.journalType[type].text }}
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
          <div class="table-operator">
            <a-button icon="plus" type="primary" @click="handleOpenPublishModal">写日志</a-button>
          </div>
          <a-divider />
          <div class="mt-4">
            <a-empty v-if="!list.loading && list.data.length === 0" />
            <a-list v-else :dataSource="list.data" :loading="list.loading" :pagination="false" itemLayout="vertical">
              <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
                <template slot="actions">
                  <span>
                    <a href="javascript:void(0);">
                      <a-icon type="like-o" />
                      {{ item.likes }}
                    </a>
                  </span>
                  <span>
                    <a href="javascript:void(0);" @click="handleOpenJournalCommentsDrawer(item)">
                      <a-icon type="message" />
                      {{ item.commentCount }}
                    </a>
                  </span>
                  <span v-if="item.type === 'INTIMATE'">
                    <a disabled href="javascript:void(0);">
                      <a-icon type="lock" />
                    </a>
                  </span>
                  <span v-else>
                    <a href="javascript:void(0);">
                      <a-icon type="unlock" />
                    </a>
                  </span>
                </template>
                <template slot="extra">
                  <a href="javascript:void(0);" @click="handleOpenEditModal(item)">编辑</a>
                  <a-divider type="vertical" />
                  <a-popconfirm
                    cancelText="取消"
                    okText="确定"
                    title="你确定要删除这条日志？"
                    @confirm="handleDelete(item.id)"
                  >
                    <a href="javascript:void(0);">删除</a>
                  </a-popconfirm>
                </template>

                <a-list-item-meta>
                  <template slot="description">
                    <p class="journal-list-content" v-html="item.content"></p>
                  </template>
                  <span slot="title">{{ item.createTime | moment }}</span>
                  <a-avatar slot="avatar" :src="user.avatar" size="large" />
                </a-list-item-meta>
              </a-list-item>
              <div class="page-wrapper">
                <a-pagination
                  :current="pagination.page"
                  :defaultPageSize="pagination.size"
                  :pageSizeOptions="['10', '20', '50', '100']"
                  :total="pagination.total"
                  class="pagination"
                  showLessItems
                  showSizeChanger
                  @change="handlePageChange"
                  @showSizeChange="handlePageSizeChange"
                />
              </div>
            </a-list>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <div style="position: fixed; bottom: 30px; right: 30px">
      <a-button
        icon="setting"
        shape="circle"
        size="large"
        type="primary"
        @click="optionModal.visible = true"
      ></a-button>
    </div>
    <a-modal v-model="optionModal.visible" :afterClose="() => (optionModal.visible = false)" title="页面设置">
      <template slot="footer">
        <a-button key="submit" type="primary" @click="handleSaveOptions()">保存</a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item help="* 需要主题进行适配" label="页面标题：">
          <a-input v-model="optionModal.options.journals_title" />
        </a-form-item>
        <a-form-item label="每页显示条数：">
          <a-input-number v-model="optionModal.options.journals_page_size" style="width: 100%" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 编辑日志弹窗 -->
    <a-modal v-model="form.visible">
      <template slot="title">
        {{ formTitle }}
        <a-tooltip slot="action" title="只能输入250字">
          <a-icon type="info-circle-o" />
        </a-tooltip>
      </template>
      <template slot="footer">
        <a-button type="dashed" @click="attachmentSelect.visible = true">附件库</a-button>
        <ReactiveButton
          :errored="form.saveErrored"
          :loading="form.saving"
          erroredText="发布失败"
          loadedText="发布成功"
          text="发布"
          type="primary"
          @callback="handleSaveOrUpdateCallback"
          @click="handleSaveOrUpdate"
        ></ReactiveButton>
      </template>
      <a-form-model ref="journalForm" :model="form.model" :rules="form.rules" layout="vertical">
        <a-form-model-item prop="sourceContent">
          <a-input
            ref="sourceContentInput"
            v-model="form.model.sourceContent"
            :autoSize="{ minRows: 8 }"
            type="textarea"
          />
        </a-form-model-item>
        <a-form-model-item>
          <a-switch v-model="form.isPublic" checkedChildren="公开" defaultChecked unCheckedChildren="私密" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>

    <TargetCommentDrawer
      :id="list.selected.id"
      :description="list.selected.content"
      :target="`journals`"
      :visible="journalCommentDrawer.visible"
      @close="onJournalCommentsDrawerClose"
    />

    <AttachmentSelectModal :visible.sync="attachmentSelect.visible" @confirm="handleSelectAttachment" />
  </page-view>
</template>

<script>
// components
import { PageView } from '@/layouts'
import TargetCommentDrawer from '../../comment/components/TargetCommentDrawer'

// libs
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import { mapActions, mapGetters } from 'vuex'
import apiClient from '@/utils/api-client'

export default {
  mixins: [mixin, mixinDevice],
  components: { PageView, TargetCommentDrawer },
  data() {
    return {
      list: {
        data: [],
        loading: false,
        total: 0,
        params: {
          page: 0,
          size: 10,
          keyword: undefined,
          type: undefined
        },
        selected: {},
        journalType: {
          PUBLIC: {
            text: '公开'
          },
          INTIMATE: {
            text: '私密'
          }
        }
      },

      form: {
        model: {},
        rules: {
          sourceContent: [{ required: true, message: '* 内容不能为空', trigger: ['change'] }]
        },
        visible: false,
        saving: false,
        saveErrored: false,
        isPublic: true
      },
      journalCommentDrawer: {
        visible: false
      },
      attachmentSelect: {
        visible: false
      },
      optionModal: {
        visible: false,
        options: []
      }
    }
  },
  beforeMount() {
    this.handleListJournals()
    this.handleListOptions()
  },
  computed: {
    ...mapGetters(['user']),
    formTitle() {
      return this.form.model.id ? '编辑' : '发表'
    },
    pagination() {
      return {
        page: this.list.params.page + 1,
        size: this.list.params.size,
        total: this.list.total
      }
    }
  },
  methods: {
    ...mapActions(['refreshOptionsCache']),
    async handleListJournals() {
      try {
        this.list.loading = true

        const { data } = await apiClient.journal.list(this.list.params)

        this.list.data = data.content
        this.list.total = data.total
      } catch (e) {
        this.$log.error(e)
      } finally {
        this.list.loading = false
      }
    },
    handleListOptions() {
      apiClient.option.list().then(response => {
        this.optionModal.options = response.data
      })
    },
    handleQuery() {
      this.handlePageChange(1)
    },
    handleResetParam() {
      this.list.params.keyword = undefined
      this.list.params.type = undefined
      this.handlePageChange(1)
    },
    handleOpenPublishModal() {
      this.form.visible = true
      this.form.model = {}
      this.$nextTick(() => {
        this.$refs.sourceContentInput.focus()
      })
    },
    handleOpenEditModal(item) {
      this.form.model = item
      this.form.isPublic = item.type !== 'INTIMATE'
      this.form.visible = true
      this.$nextTick(() => {
        this.$refs.sourceContentInput.focus()
      })
    },
    handleDelete(id) {
      apiClient.journal.delete(id).finally(() => {
        this.handleListJournals()
      })
    },
    handleOpenJournalCommentsDrawer(journal) {
      this.list.selected = journal
      this.journalCommentDrawer.visible = true
    },
    handleSaveOrUpdate() {
      const _this = this
      _this.$refs.journalForm.validate(valid => {
        if (valid) {
          _this.form.model.type = _this.form.isPublic ? 'PUBLIC' : 'INTIMATE'
          _this.form.saving = true
          if (_this.form.model.id) {
            apiClient.journal
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
            apiClient.journal
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
    handleSaveOrUpdateCallback() {
      if (this.form.saveErrored) {
        this.form.saveErrored = false
      } else {
        this.form.isPublic = true
        this.form.visible = false
        this.handleListJournals()
      }
    },

    /**
     * Handle page change
     */
    handlePageChange(page = 1) {
      this.list.params.page = page - 1
      this.handleListJournals()
    },

    /**
     * Handle page size change
     */
    handlePageSizeChange(current, size) {
      this.$log.debug(`Current: ${current}, PageSize: ${size}`)
      this.list.params.page = 0
      this.list.params.size = size
      this.handleListJournals()
    },
    onJournalCommentsDrawerClose() {
      this.form.model = {}
      this.journalCommentDrawer.visible = false
    },
    handleSaveOptions() {
      apiClient.option
        .save(this.optionModal.options)
        .then(() => {
          this.$message.success('保存成功！')
          this.optionModal.visible = false
        })
        .finally(() => {
          this.handleListOptions()
          this.refreshOptionsCache()
        })
    },
    handleSelectAttachment({ markdown }) {
      this.$set(this.form.model, 'sourceContent', (this.form.model.sourceContent || '') + '\n' + markdown.join('\n'))
    }
  }
}
</script>
