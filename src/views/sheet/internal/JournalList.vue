<template>
  <div class="page-header-index-wide">
    <a-row>
      <a-col :span="24">
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
                  <a-form-item label="状态">
                    <a-select placeholder="请选择状态">
                      <a-select-option value="1">公开</a-select-option>
                      <a-select-option value="0">私密</a-select-option>
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
                      @click="loadJournals(true)"
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
              @click="handleNew"
            >写日志</a-button>
          </div>
          <a-divider />
          <div style="margin-top:15px">
            <a-list
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
                      <a-icon
                        type="like-o"
                        style="margin-right: 8px"
                      />{{ item.likes }}
                    </a>
                  </span>
                  <span>
                    <a
                      href="javascript:void(0);"
                      @click="handleCommentShow(item)"
                    >

                      <a-icon
                        type="message"
                        style="margin-right: 8px"
                      />{{ item.commentCount }}
                    </a>
                  </span>
                  <!-- <span>
                    From 微信
                  </span> -->
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
                <a-list-item-meta :description="item.content">
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
                  :total="pagination.total"
                  :defaultPageSize="pagination.size"
                  :pageSizeOptions="['1', '2', '5', '10', '20', '50', '100']"
                  showSizeChanger
                  @showSizeChange="onPaginationChange"
                  @change="onPaginationChange"
                />
              </div>
            </a-list>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 编辑日志弹窗 -->
    <a-modal v-model="visible">
      <template slot="title">
        {{ title }} <a-tooltip
          slot="action"
          title="只能输入250字"
        >
          <a-icon type="info-circle-o" />
        </a-tooltip>
      </template>
      <template slot="footer">
        <a-button
          key="submit"
          type="primary"
          @click="createOrUpdateJournal"
        >
          发布
        </a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item>
          <a-input
            type="textarea"
            :autosize="{ minRows: 8 }"
            v-model="journal.content"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 评论回复弹窗 -->
    <a-modal
      v-if="selectComment"
      :title="'回复给：'+selectComment.author"
      v-model="selectCommentVisible"
    >
      <template slot="footer">
        <a-button
          key="submit"
          type="primary"
          @click="handleReplyComment"
        >
          回复
        </a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item>
          <a-input
            type="textarea"
            :autosize="{ minRows: 8 }"
            v-model="replyComment.content"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 评论列表抽屉 -->
    <a-drawer
      title="评论列表"
      :width="isMobile()?'100%':'460'"
      closable
      :visible="commentVisiable"
      destroyOnClose
      @close="()=>this.commentVisiable = false"
    >
      <a-row
        type="flex"
        align="middle"
      >
        <a-col :span="24">
          <a-comment>
            <a-avatar
              :src="user.avatar"
              :alt="user.nickname"
              slot="avatar"
            />
            <p slot="content">{{ journal.content }}</p>

            <span slot="datetime">{{ journal.createTime | moment }}</span>
          </a-comment>
        </a-col>
        <a-divider />
        <a-col :span="24">
          <journal-comment-tree
            v-for="(comment,index) in comments"
            :key="index"
            :comment="comment"
            @reply="handleCommentReplyClick"
            @delete="handleCommentDelete"
          />
        </a-col>
      </a-row>
    </a-drawer>
  </div>
</template>

<script>
import JournalCommentTree from './components/JournalCommentTree'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import journalApi from '@/api/journal'
import journalCommentApi from '@/api/journalComment'
import userApi from '@/api/user'

export default {
  mixins: [mixin, mixinDevice],
  components: { JournalCommentTree },
  data() {
    return {
      title: '发表',
      listLoading: false,
      visible: false,
      commentVisiable: false,
      selectCommentVisible: false,
      pagination: {
        page: 1,
        size: 10,
        sort: null
      },
      queryParam: {
        page: 0,
        size: 10,
        sort: null,
        keyword: null
      },
      journals: [],
      comments: [],
      journal: {},
      selectComment: null,
      replyComment: {},
      user: {}
    }
  },
  created() {
    this.loadJournals()
    this.loadUser()
  },
  methods: {
    loadJournals(isSearch) {
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      if (isSearch) {
        this.queryParam.page = 0
      }
      this.listLoading = true
      journalApi.query(this.queryParam).then(response => {
        this.journals = response.data.data.content
        this.pagination.total = response.data.data.total
        this.listLoading = false
      })
    },
    loadUser() {
      userApi.getProfile().then(response => {
        this.user = response.data.data
      })
    },
    handleNew() {
      this.title = '新建'
      this.visible = true
      this.journal = {}
    },
    handleEdit(item) {
      this.title = '编辑'
      this.journal = item
      this.visible = true
    },
    handleDelete(id) {
      journalApi.delete(id).then(response => {
        this.$message.success('删除成功！')
        this.loadJournals()
      })
    },
    handleCommentShow(journal) {
      this.journal = journal
      journalApi.commentTree(this.journal.id).then(response => {
        this.comments = response.data.data.content
        this.commentVisiable = true
      })
    },
    handleCommentReplyClick(comment) {
      this.selectComment = comment
      this.selectCommentVisible = true
      this.replyComment.parentId = comment.id
      this.replyComment.postId = this.journal.id
    },
    handleReplyComment() {
      journalCommentApi.create(this.replyComment).then(response => {
        this.$message.success('回复成功！')
        this.replyComment = {}
        this.selectComment = {}
        this.selectCommentVisible = false
        this.handleCommentShow(this.journal)
      })
    },
    handleCommentDelete(comment) {
      journalCommentApi.delete(comment.id).then(response => {
        this.$message.success('删除成功！')
        this.handleCommentShow(this.journal)
      })
    },
    createOrUpdateJournal() {
      if (this.journal.id) {
        journalApi.update(this.journal.id, this.journal).then(response => {
          this.$message.success('更新成功！')
          this.loadJournals()
        })
      } else {
        journalApi.create(this.journal).then(response => {
          this.$message.success('发表成功！')
          this.loadJournals()
        })
      }
      this.visible = false
    },
    onPaginationChange(page, pageSize) {
      this.$log.debug(`Current: ${page}, PageSize: ${pageSize}`)
      this.pagination.page = page
      this.pagination.size = pageSize
      this.loadJournals()
    },
    resetParam() {
      this.queryParam.keyword = null
      this.loadJournals()
    }
  }
}
</script>
