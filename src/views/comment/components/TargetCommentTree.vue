<template>
  <div>
    <a-comment>
      <template slot="actions">
        <a-dropdown v-if="comment.status === 'AUDITING'" :trigger="['click']">
          <span>通过</span>
          <a-menu slot="overlay">
            <a-menu-item key="1" @click="handleEditStatusClick('PUBLISHED')"> 通过</a-menu-item>
            <a-menu-item key="2"> 通过并回复</a-menu-item>
          </a-menu>
        </a-dropdown>

        <span v-else-if="comment.status === 'PUBLISHED'" @click="handleReplyClick">回复</span>

        <a-popconfirm
          v-else-if="comment.status === 'RECYCLE'"
          :title="'你确定要还原该评论？'"
          cancelText="取消"
          okText="确定"
          @confirm="handleEditStatusClick('PUBLISHED')"
        >
          还原
        </a-popconfirm>

        <a-popconfirm
          v-if="comment.status === 'PUBLISHED' || comment.status === 'AUDITING'"
          :title="'你确定要将该评论移到回收站？'"
          cancelText="取消"
          okText="确定"
          @confirm="handleEditStatusClick('RECYCLE')"
        >
          回收站
        </a-popconfirm>

        <a-popconfirm :title="'你确定要永久删除该评论？'" cancelText="取消" okText="确定" @confirm="handleDeleteClick">
          删除
        </a-popconfirm>
      </template>
      <a slot="author" :href="comment.authorUrl" target="_blank">
        <a-icon v-if="comment.isAdmin" style="margin-right: 3px" type="user" />
        {{ comment.author }}
      </a>
      <a-avatar slot="avatar" :alt="comment.author" :src="comment.avatar" size="large" />
      <p slot="content" v-html="content"></p>
      <a-tooltip slot="datetime">
        <span slot="title">{{ comment.createTime | moment }}</span>
        <span>{{ comment.createTime | timeAgo }}</span>
      </a-tooltip>
      <template v-if="comment.children">
        <TargetCommentTree
          v-for="(child, index) in comment.children"
          :key="index"
          :comment="child"
          v-bind="$attrs"
          @delete="handleDeleteClick"
          @editStatus="handleEditStatusClick"
          @reply="handleReplyClick"
          v-on="$listeners"
        />
      </template>
    </a-comment>
  </div>
</template>
<script>
import { marked } from 'marked'

export default {
  name: 'TargetCommentTree',
  props: {
    comment: {
      type: Object,
      required: false,
      default: null
    }
  },
  computed: {
    content() {
      return marked.parse(this.comment.content)
    }
  },
  methods: {
    handleReplyClick() {
      this.$emit('reply', this.comment)
    },
    handleEditStatusClick(status) {
      this.$emit('editStatus', this.comment, status)
    },
    handleDeleteClick() {
      this.$emit('delete', this.comment)
    }
  }
}
</script>
