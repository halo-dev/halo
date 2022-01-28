<template>
  <CommentListView :comments="comments" :loading="loading" />
</template>

<script>
import apiClient from '@/utils/api-client'

export default {
  name: 'RecentCommentTab',
  props: {
    type: {
      type: String,
      required: false,
      default: 'posts',
      validator: function (value) {
        return ['posts', 'sheets', 'journals'].indexOf(value) !== -1
      }
    }
  },
  data() {
    return {
      comments: [],
      loading: false
    }
  },
  created() {
    this.handleListTargetComments()
  },
  methods: {
    async handleListTargetComments() {
      try {
        this.loading = true
        const { data } = await apiClient.comment.latest(this.type, 5, 'PUBLISHED')
        this.comments = data
      } catch (e) {
        this.$log.error('Failed to load comments', e)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>
