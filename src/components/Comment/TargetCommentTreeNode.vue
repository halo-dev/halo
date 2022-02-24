<template>
  <a-comment>
    <template #author>
      <a :href="comment.authorUrl" target="_blank">
        <a-icon v-if="comment.isAdmin" style="margin-right: 3px" type="user" />
        {{ comment.author }}
      </a>
    </template>

    <template #avatar>
      <a-avatar :alt="comment.author" :src="comment.avatar" size="large" />
    </template>

    <template #content>
      <p v-html="$options.filters.markdownRender(comment.content)"></p>
    </template>

    <template #datetime>
      <a-tooltip>
        <template #title>
          <span>{{ comment.createTime | moment }}</span>
        </template>
        <span>{{ comment.createTime | timeAgo }}</span>
      </a-tooltip>
    </template>

    <template #actions>
      <a-dropdown v-if="comment.status === 'AUDITING'" :trigger="['click']">
        <span>通过</span>

        <template #overlay>
          <a-menu>
            <a-menu-item key="1" @click="handleChangeStatus('PUBLISHED')"> 通过</a-menu-item>
            <a-menu-item key="2" @click="handlePublishAndReply"> 通过并回复</a-menu-item>
          </a-menu>
        </template>
      </a-dropdown>

      <span v-else-if="comment.status === 'PUBLISHED'" @click="replyModalVisible = true">回复</span>

      <a-popconfirm
        v-else-if="comment.status === 'RECYCLE'"
        :title="'你确定要还原该评论？'"
        cancelText="取消"
        okText="确定"
        @confirm="handleChangeStatus('PUBLISHED')"
      >
        还原
      </a-popconfirm>

      <a-popconfirm
        v-if="comment.status === 'PUBLISHED' || comment.status === 'AUDITING'"
        :title="'你确定要将该评论移到回收站？'"
        cancelText="取消"
        okText="确定"
        @confirm="handleChangeStatus('RECYCLE')"
      >
        回收站
      </a-popconfirm>

      <a-popconfirm :title="'你确定要永久删除该评论？'" cancelText="取消" okText="确定" @confirm="handleDelete">
        删除
      </a-popconfirm>
    </template>

    <template v-if="comment.children">
      <TargetCommentTreeNode
        v-for="(child, index) in comment.children"
        :key="index"
        :comment="child"
        :target="target"
        :target-id="targetId"
        @reload="$emit('reload')"
      />
    </template>

    <CommentReplyModal
      :comment="comment"
      :target="target"
      :target-id="targetId"
      :visible.sync="replyModalVisible"
      @succeed="$emit('reload')"
    />
  </a-comment>
</template>
<script>
import apiClient from '@/utils/api-client'
import CommentReplyModal from './CommentReplyModal'

export default {
  name: 'TargetCommentTreeNode',
  components: {
    CommentReplyModal
  },
  props: {
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
    },
    comment: {
      type: Object,
      required: false,
      default: null
    }
  },
  data() {
    return {
      replyModalVisible: false
    }
  },
  methods: {
    async handleChangeStatus(status) {
      try {
        await apiClient.comment.updateStatusById(`${this.target}s`, this.comment.id, status)
      } catch (e) {
        this.$log.error('Failed to change comment status', e)
      } finally {
        this.$emit('reload')
      }
    },

    async handlePublishAndReply() {
      await this.handleChangeStatus('PUBLISHED')
      this.replyModalVisible = true
    },

    async handleDelete() {
      try {
        await apiClient.comment.delete(`${this.target}s`, this.comment.id)
      } catch (e) {
        this.$log.error('Failed to delete comment', e)
      } finally {
        this.$emit('reload')
      }
    }
  }
}
</script>
