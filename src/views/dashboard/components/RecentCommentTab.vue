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
        <template
          slot="author"
          v-if="type==='posts'"
        >
          <a
            :href="item.authorUrl"
            target="_blank"
          >{{ item.author }}</a> 发表在 《<a
            :href="options.blog_url+'/archives/'+item.post.url"
            target="_blank"
          >{{ item.post.title }}</a>》
        </template>
        <template
          slot="author"
          v-else-if="type==='sheets'"
        >
          <a
            :href="item.authorUrl"
            target="_blank"
          >{{ item.author }}</a> 发表在 《<a
            :href="options.blog_url+'/s/'+item.sheet.url"
            target="_blank"
          >{{ item.sheet.title }}</a>》
        </template>
        <!-- <template slot="actions">
          <span>回复</span>
        </template> -->
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
import optionApi from '@/api/option'

import marked from 'marked'
export default {
  name: 'RecentCommentTab',
  props: {
    type: {
      type: String,
      required: false,
      default: 'posts',
      validator: function(value) {
        return ['posts', 'sheets', 'journals'].indexOf(value) !== -1
      }
    }
  },
  data() {
    return {
      comments: [],
      loading: false,
      options: [],
      keys: ['blog_url']
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
    this.loadOptions()
  },
  methods: {
    loadOptions() {
      optionApi.listAll(this.keys).then(response => {
        this.options = response.data.data
      })
    },
    loadComments() {
      this.loading = true
      commentApi.latestComment(this.type, 5, 'PUBLISHED').then(response => {
        this.comments = response.data.data
        this.loading = false
      })
    }
  }
}
</script>
