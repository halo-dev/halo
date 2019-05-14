<template>
  <a-list
    itemLayout="horizontal"
    :dataSource="formmatedCommentData"
    :loading="loading"
  >
    <a-list-item
      slot="renderItem"
      slot-scope="item, index"
      :key="index"
    >
      <a-comment :avatar="'//gravatar.loli.net/avatar/'+item.gavatarMd5+'/?s=256&d=mp'">
        <template slot="author">
          {{ item.author }} 发表在 《<a
            href="javascript:void(0);"
            target="_blank"
          >{{ item.post.title }}</a>》
        </template>
        <template slot="actions">
          <span>回复</span>
        </template>
        <p
          class="comment-content-wrapper"
          slot="content"
          v-html="item.content"
        ></p>
        <a-tooltip
          slot="datetime"
          :title="item.createTime | moment"
        >
          <span>{{ item.createTime | timeAgo }}</span>
        </a-tooltip>
      </a-comment>
    </a-list-item>
  </a-list>
</template>

<script>
import commentApi from '@/api/comment'
import marked from 'marked'
export default {
  name: 'RecentCommentTab',
  props: {
    type: {
      type: String,
      required: false,
      default: 'post',
      validator: function(value) {
        return ['post', 'sheet', 'journal'].indexOf(value) !== -1
      }
    }
  },
  data() {
    return {
      comments: [],
      loading: false
    }
  },
  computed: {
    formmatedCommentData() {
      return this.comments.map(comment => {
        comment.content = marked(comment.content, { sanitize: true })
        return comment
      })
    }
  },
  created() {
    this.loadComments()
  },
  methods: {
    loadComments() {
      this.loading = true
      switch (this.type) {
        case 'post':
          commentApi.latestPostComment(5, 'PUBLISHED').then(response => {
            this.comments = response.data.data
            this.loading = false
          })
          break
        case 'sheet':
          commentApi.latestSheetComment(5, 'PUBLISHED').then(response => {
            this.comments = response.data.data
            this.loading = false
          })
          break
        case 'journal':
          commentApi.latestJournalComment(5, 'PUBLISHED').then(response => {
            this.comments = response.data.data
            this.loading = false
          })
          break
      }
    }
  }
}
</script>
