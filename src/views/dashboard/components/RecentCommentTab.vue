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
      <a-comment :avatar="'//cn.gravatar.com/avatar/'+item.gravatarMd5+'/?s=256&d=mp'">
        <template
          slot="author"
          v-if="type==='posts'"
        >
          <a
            :href="item.authorUrl"
            target="_blank"
          >{{ item.author }}</a> 发表在 《<a
            v-if="item.post.status=='PUBLISHED' || item.post.status=='INTIMATE'"
            :href="item.post.fullPath"
            target="_blank"
          >{{ item.post.title }}</a><a
            v-else-if="item.post.status=='DRAFT'"
            href="javascript:void(0)"
            @click="handlePostPreview(item.post.id)"
          >{{ item.post.title }}</a><a
            v-else
            href="javascript:void(0)"
          >{{ item.post.title }}</a>
          》
        </template>
        <template
          slot="author"
          v-else-if="type==='sheets'"
        >
          <a
            :href="item.authorUrl"
            target="_blank"
          >{{ item.author }}</a> 发表在 《<a
            v-if="item.sheet.status=='PUBLISHED'"
            :href="item.sheet.fullPath"
            target="_blank"
          >{{ item.sheet.title }}</a><a
            v-else-if="item.sheet.status=='DRAFT'"
            href="javascript:void(0)"
            @click="handleSheetPreview(item.sheet.id)"
          >{{ item.sheet.title }}</a><a
            v-else
            href="javascript:void(0)"
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
import postApi from '@/api/post'
import sheetApi from '@/api/sheet'

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
      loading: false
    }
  },
  computed: {
    formmatedCommentData() {
      return this.comments.map(comment => {
        comment.content = marked(comment.content)
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
      commentApi.latestComment(this.type, 5, 'PUBLISHED').then(response => {
        this.comments = response.data.data
        this.loading = false
      })
    },
    handlePostPreview(postId) {
      postApi.preview(postId).then(response => {
        window.open(response.data, '_blank')
      })
    },
    handleSheetPreview(sheetId) {
      sheetApi.preview(sheetId).then(response => {
        window.open(response.data, '_blank')
      })
    }
  }
}
</script>
