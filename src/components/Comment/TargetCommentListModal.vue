<template>
  <a-modal v-model="modalVisible" :afterClose="onClose" :title="title" :width="1024" destroyOnClose>
    <a-spin :spinning="list.loading">
      <TargetCommentTreeNode
        v-for="(comment, index) in list.data"
        :key="index"
        :comment="comment"
        :target="target"
        :target-id="targetId"
        @reload="handleGetComments"
      />
    </a-spin>

    <a-empty v-if="!list.loading && !list.data.length" />

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

    <template #footer>
      <slot name="extraFooter" />
      <a-button type="primary" @click="replyModalVisible = true">创建评论</a-button>
      <a-button @click="modalVisible = false">关闭</a-button>
    </template>

    <CommentReplyModal
      :target="target"
      :target-id="targetId"
      :visible.sync="replyModalVisible"
      @succeed="handleGetComments"
    />
  </a-modal>
</template>
<script>
// components
import TargetCommentTreeNode from './TargetCommentTreeNode'
import CommentReplyModal from './CommentReplyModal'

import apiClient from '@/utils/api-client'

export default {
  name: 'TargetCommentListModal',
  components: {
    TargetCommentTreeNode,
    CommentReplyModal
  },
  props: {
    visible: {
      type: Boolean,
      default: true
    },
    title: {
      type: String,
      default: '评论'
    },
    target: {
      type: String,
      required: true,
      validator: value => {
        return ['post', 'sheet', 'journal'].indexOf(value) !== -1
      }
    },
    targetId: {
      type: Number,
      required: true,
      default: 0
    }
  },
  data() {
    return {
      list: {
        data: [],
        loading: false,
        params: {
          page: 0,
          size: 10
        },
        total: 0
      },
      replyModalVisible: false
    }
  },
  computed: {
    modalVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    },
    pagination() {
      return {
        page: this.list.params.page + 1,
        size: this.list.params.size,
        total: this.list.total
      }
    }
  },
  watch: {
    modalVisible(value) {
      if (value) {
        this.handleGetComments()
      }
    },
    targetId() {
      this.handleGetComments()
    }
  },
  methods: {
    async handleGetComments() {
      try {
        this.list.loading = true

        const response = await apiClient.comment.listAsTreeView(`${this.target}s`, this.targetId, this.list.params)
        this.list.data = response.data.content
        this.list.total = response.data.total
      } catch (e) {
        this.$log.error('Failed to get target comments', e)
      } finally {
        this.list.loading = false
      }
    },

    /**
     * Handle page change
     */
    handlePageChange(page = 1) {
      this.list.params.page = page - 1
      this.handleGetComments()
    },

    /**
     * Handle page size change
     */
    handlePageSizeChange(current, size) {
      this.list.params.page = 0
      this.list.params.size = size
      this.handleGetComments()
    },

    onClose() {
      this.$emit('close')
    }
  }
}
</script>
