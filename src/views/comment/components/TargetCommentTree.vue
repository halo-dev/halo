<template>
  <div>
    <a-comment>
      <template slot="actions">
        <a-dropdown
          :trigger="['click']"
          v-if="comment.status === 'AUDITING'"
        >
          <span href="javascript:void(0);">通过</span>
          <a-menu slot="overlay">
            <a-menu-item key="1">
              <span
                href="javascript:void(0);"
                @click="handleEditStatusClick('PUBLISHED')"
              >通过</span>
            </a-menu-item>
            <a-menu-item key="2">
              <span href="javascript:void(0);">通过并回复</span>
            </a-menu-item>
          </a-menu>
        </a-dropdown>

        <span
          v-else-if="comment.status === 'PUBLISHED'"
          @click="handleReplyClick"
        >回复</span>

        <a-popconfirm
          v-else-if="comment.status === 'RECYCLE'"
          :title="'你确定要还原该评论？'"
          @confirm="handleEditStatusClick('PUBLISHED')"
          okText="确定"
          cancelText="取消"
        >
          <span>还原</span>
        </a-popconfirm>

        <a-popconfirm
          v-if="comment.status === 'PUBLISHED' || comment.status === 'AUDITING'"
          :title="'你确定要将该评论移到回收站？'"
          @confirm="handleEditStatusClick('RECYCLE')"
          okText="确定"
          cancelText="取消"
        >
          <span>回收站</span>
        </a-popconfirm>

        <a-popconfirm
          :title="'你确定要永久删除该评论？'"
          @confirm="handleDeleteClick"
          okText="确定"
          cancelText="取消"
        >
          <span>删除</span>
        </a-popconfirm>
      </template>
      <a
        slot="author"
        :href="comment.authorUrl"
        target="_blank"
      >
        <a-icon
          type="user"
          v-if="comment.isAdmin"
          style="margin-right: 3px;"
        />
        {{ comment.author }}
      </a>
      <a-avatar
        size="large"
        slot="avatar"
        :src="avatar"
        :alt="comment.author"
      />
      <p
        slot="content"
        v-html="content"
      ></p>
      <a-tooltip slot="datetime">
        <span slot="title">{{ comment.createTime | moment }}</span>
        <span>{{ comment.createTime | timeAgo }}</span>
      </a-tooltip>
      <template v-if="comment.children">
        <TargetCommentTree
          v-for="(child, index) in comment.children"
          :key="index"
          :comment="child"
          v-on="$listeners"
          v-bind="$attrs"
          @reply="handleReplyClick"
          @delete="handleDeleteClick"
          @editStatus="handleEditStatusClick"
        />
      </template>
    </a-comment>
  </div>
</template>
<script>
import marked from 'marked'
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
    avatar() {
      return `//cn.gravatar.com/avatar/${this.comment.gravatarMd5}/?s=256&d=mp`
    },
    content() {
      return marked(this.comment.content)
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
