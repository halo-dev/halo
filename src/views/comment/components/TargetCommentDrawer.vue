<template>
  <a-drawer
    title="评论列表"
    :width="isMobile() ? '100%' : '480'"
    closable
    :visible="visible"
    destroyOnClose
    @close="onClose"
  >
    <a-row
      type="flex"
      align="middle"
    >
      <a-col :span="24">
        <a-list itemLayout="horizontal">
          <a-list-item>
            <a-list-item-meta>
              <template slot="description">
                <p
                  v-html="description"
                  class="comment-drawer-content"
                ></p>
              </template>
              <h3 slot="title">{{ title }}</h3>
            </a-list-item-meta>
          </a-list-item>
        </a-list>
      </a-col>
      <a-divider />
      <a-col :span="24">
        <a-empty v-if="comments.length == 0" />
        <TargetCommentTree
          v-else
          v-for="(comment, index) in comments"
          :key="index"
          :comment="comment"
          @reply="handleCommentReply"
          @delete="handleCommentDelete"
          @editStatus="handleEditStatusClick"
        />
      </a-col>
    </a-row>
    <a-divider />
    <div class="page-wrapper">
      <a-pagination
        :current="pagination.page"
        :total="pagination.total"
        :defaultPageSize="pagination.size"
        @change="handlePaginationChange"
      ></a-pagination>
    </div>
    <a-divider class="divider-transparent" />
    <div class="bottom-control">
      <a-button
        type="primary"
        @click="handleComment"
      >评论</a-button>
    </div>
    <a-modal
      v-if="selectedComment"
      :title="'回复给：' + selectedComment.author"
      v-model="replyCommentVisible"
      @close="onReplyClose"
      destroyOnClose
    >
      <template slot="footer">
        <a-button
          key="submit"
          type="primary"
          @click="handleCreateClick"
        >
          回复
        </a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item>
          <a-input
            type="textarea"
            :autoSize="{ minRows: 8 }"
            v-model="replyComment.content"
          />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal
      title="评论"
      v-model="commentVisible"
      @close="onCommentClose"
      destroyOnClose
    >
      <template slot="footer">
        <a-button
          key="submit"
          type="primary"
          @click="handleCreateClick"
        >
          回复
        </a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item>
          <a-input
            type="textarea"
            :autoSize="{ minRows: 8 }"
            v-model="replyComment.content"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </a-drawer>
</template>
<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import TargetCommentTree from './TargetCommentTree'
import commentApi from '@/api/comment'
export default {
  name: 'TargetCommentDrawer',
  mixins: [mixin, mixinDevice],
  components: { TargetCommentTree },
  data() {
    return {
      comments: [],
      selectedComment: {},
      replyComment: {},
      replyCommentVisible: false,
      commentVisible: false,
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
        keyword: null
      }
    }
  },
  props: {
    visible: {
      type: Boolean,
      required: false,
      default: false
    },
    title: {
      type: String,
      required: false,
      default: ''
    },
    description: {
      type: String,
      required: false,
      default: ''
    },
    target: {
      type: String,
      required: false,
      default: ''
    },
    id: {
      type: Number,
      required: false,
      default: 0
    }
  },
  watch: {
    visible(newValue, oldValue) {
      this.$log.debug('old value', oldValue)
      this.$log.debug('new value', newValue)
      if (newValue) {
        this.loadComments()
      }
    }
  },
  methods: {
    loadComments() {
      this.queryParam.page = this.pagination.page - 1
      this.queryParam.size = this.pagination.size
      this.queryParam.sort = this.pagination.sort
      commentApi.commentTree(this.target, this.id, this.queryParam).then(response => {
        this.comments = response.data.data.content
        this.pagination.total = response.data.data.total
      })
    },
    handlePaginationChange(page, pageSize) {
      this.pagination.page = page
      this.pagination.size = pageSize
      this.loadComments()
    },
    handleCommentReply(comment) {
      this.selectedComment = comment
      this.replyCommentVisible = true
      this.replyComment.parentId = comment.id
      this.replyComment.postId = this.id
    },
    handleComment() {
      this.replyComment.postId = this.id
      this.commentVisible = true
    },
    handleCreateClick() {
      if (!this.replyComment.content) {
        this.$notification['error']({
          message: '提示',
          description: '评论内容不能为空！'
        })
        return
      }
      commentApi.create(this.target, this.replyComment).then(response => {
        this.$message.success('回复成功！')
        this.replyComment = {}
        this.selectedComment = {}
        this.replyCommentVisible = false
        this.commentVisible = false
        this.loadComments()
      })
    },
    handleEditStatusClick(comment, status) {
      commentApi.updateStatus(this.target, comment.id, status).then(response => {
        this.$message.success('操作成功！')
        this.loadComments()
      })
    },
    handleCommentDelete(comment) {
      commentApi.delete(this.target, comment.id).then(response => {
        this.$message.success('删除成功！')
        this.loadComments()
      })
    },
    onReplyClose() {
      this.replyComment = {}
      this.selectedComment = {}
      this.replyCommentVisible = false
    },
    onCommentClose() {
      this.replyComment = {}
      this.commentVisible = false
    },
    onClose() {
      this.comments = []
      this.pagination = {
        page: 1,
        size: 10,
        sort: ''
      }
      this.$emit('close', false)
    }
  }
}
</script>
