<template>
  <div>
    <a-comment>
      <span
        slot="actions"
        @click="handleReplyClick"
      >回复</span>
      <a-popconfirm
        :title="'你确定要永久删除该评论？'"
        @confirm="handleDeleteClick"
        okText="确定"
        cancelText="取消"
        slot="actions"
      >
        <span>删除</span>
      </a-popconfirm>
      <a slot="author"> {{ comment.author }} </a>
      <a-avatar
        slot="avatar"
        :src="avatar"
        :alt="comment.author"
      />
      <p slot="content">{{ comment.content }}</p>

      <template v-if="comment.children">
        <journal-comment-tree
          v-for="(child, index) in comment.children"
          :key="index"
          :comment="child"
          @reply="handleSubReply"
          @delete="handleSubDelete"
        />
      </template>

    </a-comment>
  </div>
</template>

<script>
export default {
  name: 'JournalCommentTree',
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
    }
  },
  methods: {
    handleReplyClick() {
      this.$emit('reply', this.comment)
    },
    handleSubReply(comment) {
      this.$emit('reply', comment)
    },
    handleDeleteClick() {
      this.$emit('delete', this.comment)
    },
    handleSubDelete(comment) {
      this.$emit('delete', comment)
    }
  }
}
</script>
