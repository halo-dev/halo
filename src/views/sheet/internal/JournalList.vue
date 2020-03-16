<template>
  <div>
    <a-row>
      <a-col :span="24">
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
                    <a-input
                      v-model="queryParam.keyword"
                      @keyup.enter="handleQuery()"
                    />
                  </a-form-item>
                </a-col>
                <a-col
                  :md="6"
                  :sm="24"
                >
                  <a-form-item label="状态：">
                    <a-select
                      placeholder="请选择状态"
                      v-model="queryParam.type"
                      @change="handleQuery()"
                    >
                      <a-select-option
                        v-for="type in Object.keys(journalType)"
                        :key="type"
                        :value="type"
                      >{{ journalType[type].text }}</a-select-option>
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
          <div class="table-operator">
            <a-button
              type="primary"
              icon="plus"
              @click="handleNew"
            >写日志</a-button>
          </div>
          <a-divider />
          <div style="margin-top:15px">
            <a-empty v-if="journals.length==0" />
            <a-list
              v-else
              itemLayout="vertical"
              :pagination="false"
              :dataSource="journals"
              :loading="listLoading"
            >
              <a-list-item
                slot="renderItem"
                slot-scope="item, index"
                :key="index"
              >
                <template slot="actions">
                  <span>
                    <a href="javascript:void(0);">
                      <a-icon type="like-o" />
                      {{ item.likes }}
                    </a>
                  </span>
                  <span>
                    <a
                      href="javascript:void(0);"
                      @click="handleShowJournalComments(item)"
                    >
                      <a-icon type="message" />
                      {{ item.commentCount }}
                    </a>
                  </span>
                  <span v-if="item.type=='INTIMATE'">
                    <a
                      href="javascript:void(0);"
                      disabled
                    >
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
                  <a
                    href="javascript:void(0);"
                    @click="handleEdit(item)"
                  >编辑</a>
                  <a-divider type="vertical" />
                  <a-popconfirm
                    title="你确定要删除这条日志？"
                    @confirm="handleDelete(item.id)"
                    okText="确定"
                    cancelText="取消"
                  >
                    <a href="javascript:void(0);">删除</a>
                  </a-popconfirm>
                </template>

                <a-list-item-meta>
                  <template slot="description">
                    <p
                      v-html="item.content"
                      class="journal-list-content"
                    ></p>
                  </template>
                  <span slot="title">{{ item.createTime | moment }}</span>
                  <a-avatar
                    slot="avatar"
                    size="large"
                    :src="user.avatar"
                  />
                </a-list-item-meta>
              </a-list-item>
              <div class="page-wrapper">
                <a-pagination
                  class="pagination"
                  :current="pagination.page"
                  :total="pagination.total"
                  :defaultPageSize="pagination.size"
                  :pageSizeOptions="['1', '2', '5', '10', '20', '50', '100']"
                  showSizeChanger
                  @showSizeChange="handlePaginationChange"
                  @change="handlePaginationChange"
                />
              </div>
            </a-list>
          </div>
        </a-card>
      </a-col>
    </a-row>

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
          <a-input v-model="options.journals_title" />
        </a-form-item>
        <a-form-item label="每页显示条数：">
          <a-input
            type="number"
            v-model="options.journals_page_size"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 编辑日志弹窗 -->
    <a-modal v-model="visible">
      <template slot="title">
        {{ title }}
        <a-tooltip
          slot="action"
          title="只能输入250字"
        >
          <a-icon type="info-circle-o" />
        </a-tooltip>
      </template>
      <template slot="footer">
        <a-button
          type="dashed"
          @click="attachmentDrawerVisible = true"
        >附件库</a-button>
        <a-button
          key="submit"
          type="primary"
          @click="createOrUpdateJournal"
        >发布</a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item>
          <a-input
            type="textarea"
            :autoSize="{ minRows: 8 }"
            v-model="journal.sourceContent"
          />
        </a-form-item>
        <a-form-item>
          <a-switch
            checkedChildren="公开"
            unCheckedChildren="私密"
            v-model="isPublic"
            defaultChecked
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <TargetCommentDrawer
      :visible="journalCommentVisible"
      :description="journal.content"
      :target="`journals`"
      :id="journal.id"
      @close="onJournalCommentsClose"
    />

    <AttachmentDrawer v-model="attachmentDrawerVisible" />
  </div>
</template>

<script>
import TargetCommentDrawer from '../../comment/components/TargetCommentDrawer'
import AttachmentDrawer from '../../attachment/components/AttachmentDrawer'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import { mapGetters, mapActions } from 'vuex'
import journalApi from '@/api/journal'
import journalCommentApi from '@/api/journalComment'
import optionApi from '@/api/option'
export default {
  mixins: [mixin, mixinDevice],
  components: { TargetCommentDrawer, AttachmentDrawer },
  data() {
    return {
      journalType: journalApi.journalType,
      title: '发表',
      listLoading: false,
      visible: false,
      journalCommentVisible: false,
      attachmentDrawerVisible: false,
      optionFormVisible: false,
      pagination: {
        page: 1,
        size: 10,
        sort: null,
        total: 1
      },
      queryParam: {
        page: 0,
        size: 10,
        sort: null,
        keyword: null,
        type: null
      },
      journals: [],
      comments: [],
      journal: {},
      isPublic: true,
      replyComment: {},
      options: []
    }
  },
  created() {
    this.loadJournals()
    this.loadFormOptions()
  },
  computed: {
    ...mapGetters(['user'])
  },
  methods: {
    ...mapActions(['loadOptions']),
    loadJournals() {
      this.listLoading = true
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      journalApi.query(this.queryParam).then(response => {
        this.journals = response.data.data.content
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
    handleNew() {
      this.title = '新建'
      this.visible = true
      this.journal = {}
    },
    handleEdit(item) {
      this.title = '编辑'
      this.journal = item
      this.isPublic = item.type !== 'INTIMATE'
      this.visible = true
    },
    handleDelete(id) {
      journalApi.delete(id).then(response => {
        this.$message.success('删除成功！')
        this.loadJournals()
      })
    },
    handleShowJournalComments(journal) {
      this.journal = journal
      this.journalCommentVisible = true
    },
    handleCommentDelete(comment) {
      journalCommentApi.delete(comment.id).then(response => {
        this.$message.success('删除成功！')
        this.handleCommentShow(this.journal)
      })
    },
    createOrUpdateJournal() {
      this.journal.type = this.isPublic ? 'PUBLIC' : 'INTIMATE'

      if (!this.journal.sourceContent) {
        this.$notification['error']({
          message: '提示',
          description: '发布内容不能为空！'
        })
        return
      }

      if (this.journal.id) {
        journalApi.update(this.journal.id, this.journal).then(response => {
          this.$message.success('更新成功！')
          this.loadJournals()
          this.isPublic = true
        })
      } else {
        journalApi.create(this.journal).then(response => {
          this.$message.success('发表成功！')
          this.loadJournals()
          this.isPublic = true
        })
      }
      this.visible = false
    },
    handlePaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.pagination.page = page
      this.pagination.size = pageSize
      this.loadJournals()
    },
    onJournalCommentsClose() {
      this.journal = {}
      this.journalCommentVisible = false
    },
    resetParam() {
      this.queryParam.keyword = null
      this.queryParam.type = null
      this.handlePaginationChange(1, this.pagination.size)
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
