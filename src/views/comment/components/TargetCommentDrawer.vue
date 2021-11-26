<template>
  <a-drawer
    :afterVisibleChange="handleAfterVisibleChanged"
    :visible="visible"
    :width="isMobile() ? '100%' : '480'"
    closable
    destroyOnClose
    title="评论列表"
    @close="onClose"
  >
    <a-row align="middle" type="flex">
      <a-col :span="24">
        <a-list itemLayout="horizontal">
          <a-list-item>
            <a-list-item-meta>
              <template slot="description">
                <p class="comment-drawer-content" v-html="description"></p>
              </template>
              <h3 slot="title">{{ title }}</h3>
            </a-list-item-meta>
          </a-list-item>
        </a-list>
      </a-col>
      <a-divider />
      <a-col :span="24">
        <a-spin :spinning="list.loading">
          <a-empty v-if="list.data.length === 0" />
          <TargetCommentTree
            v-for="(comment, index) in list.data"
            v-else
            :key="index"
            :comment="comment"
            @delete="handleCommentDelete"
            @editStatus="handleEditStatusClick"
            @reply="handleCommentReply"
          />
        </a-spin>
      </a-col>
    </a-row>
    <a-divider />
    <div class="page-wrapper">
      <a-pagination
        :current="list.pagination.page"
        :defaultPageSize="list.pagination.size"
        :total="list.pagination.total"
        showLessItems
        @change="handlePaginationChange"
      ></a-pagination>
    </div>
    <a-divider class="divider-transparent" />
    <div class="bottom-control">
      <a-button type="primary" @click="handleCommentReply({})">评论</a-button>
    </div>
    <a-modal v-model="replyModal.visible" :title="replyModalTitle" destroyOnClose @close="onReplyModalClose">
      <template slot="footer">
        <ReactiveButton
          :errored="replyModal.saveErrored"
          :loading="replyModal.saving"
          erroredText="回复失败"
          loadedText="回复成功"
          text="回复"
          type="primary"
          @callback="handleReplyCallback"
          @click="handleReplyClick"
        ></ReactiveButton>
      </template>
      <a-form-model ref="replyCommentForm" :model="replyModal.model" :rules="replyModal.rules" layout="vertical">
        <a-form-model-item prop="content">
          <a-input ref="contentInput" v-model="replyModal.model.content" :autoSize="{ minRows: 8 }" type="textarea" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
  </a-drawer>
</template>
<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import TargetCommentTree from './TargetCommentTree'
import apiClient from '@/utils/api-client'

export default {
  name: 'TargetCommentDrawer',
  mixins: [mixin, mixinDevice],
  components: { TargetCommentTree },
  data() {
    return {
      list: {
        data: [],
        loading: false,
        selected: {},
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
      },
      replyModal: {
        model: {},
        visible: false,
        saving: false,
        saveErrored: false,
        rules: {
          content: [{ required: true, message: '* 内容不能为空', trigger: ['change'] }]
        }
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
  computed: {
    replyModalTitle() {
      return this.list.selected.id ? `回复给：${this.list.selected.author}` : '评论'
    }
  },
  methods: {
    handleListComments() {
      this.list.loading = true
      this.list.queryParam.page = this.list.pagination.page - 1
      this.list.queryParam.size = this.list.pagination.size
      this.list.queryParam.sort = this.list.pagination.sort
      apiClient.comment
        .listAsTreeView(this.target, this.id, this.list.queryParam)
        .then(response => {
          this.list.data = response.data.content
          this.list.pagination.total = response.data.total
        })
        .finally(() => {
          this.list.loading = false
        })
    },
    handlePaginationChange(page, pageSize) {
      this.list.pagination.page = page
      this.list.pagination.size = pageSize
      this.handleListComments()
    },
    handleCommentReply(comment) {
      this.list.selected = comment
      this.replyModal.visible = true
      this.replyModal.model.parentId = comment.id
      this.replyModal.model.postId = this.id
      this.$nextTick(() => {
        this.$refs.contentInput.focus()
      })
    },
    handleReplyClick() {
      const _this = this
      _this.$refs.replyCommentForm.validate(valid => {
        if (valid) {
          _this.replyModal.saving = true
          apiClient.comment
            .create(_this.target, _this.replyModal.model)
            .catch(() => {
              _this.replyModal.saveErrored = true
            })
            .finally(() => {
              setTimeout(() => {
                _this.replyModal.saving = false
              }, 400)
            })
        }
      })
    },
    handleReplyCallback() {
      if (this.replyModal.saveErrored) {
        this.replyModal.saveErrored = false
      } else {
        this.replyModal.model = {}
        this.list.selected = {}
        this.replyModal.visible = false
        this.handleListComments()
      }
    },
    handleEditStatusClick(comment, status) {
      apiClient.comment
        .updateStatusById(this.target, comment.id, status)
        .then(() => {
          this.$message.success('操作成功！')
        })
        .finally(() => {
          this.handleListComments()
        })
    },
    handleCommentDelete(comment) {
      apiClient.comment
        .delete(this.target, comment.id)
        .then(() => {
          this.$message.success('删除成功！')
        })
        .finally(() => {
          this.handleListComments()
        })
    },
    onReplyModalClose() {
      this.replyModal.model = {}
      this.list.selected = {}
      this.replyModal.visible = false
    },
    onClose() {
      this.list.data = []
      this.list.pagination = {
        page: 1,
        size: 10,
        sort: ''
      }
      this.$emit('close', false)
    },
    handleAfterVisibleChanged(visible) {
      if (visible) {
        this.handleListComments()
      }
    }
  }
}
</script>
