<template>
  <a-list :dataSource="formmatedCommentData" :loading="loading" itemLayout="horizontal">
    <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
      <a-comment :avatar="item.avatar">
        <template v-if="type === 'posts'" slot="author">
          <a :href="item.authorUrl" target="_blank">{{ item.author }}</a> 发表在 《<a
            v-if="['PUBLISHED', 'INTIMATE'].includes(item.post.status)"
            :href="item.post.fullPath"
            target="_blank"
            >{{ item.post.title }}</a
          ><a
            v-else-if="item.post.status === 'DRAFT'"
            href="javascript:void(0)"
            @click="handlePostPreview(item.post.id)"
            >{{ item.post.title }}</a
          ><a v-else href="javascript:void(0)">{{ item.post.title }}</a>
          》
        </template>
        <template v-else-if="type === 'sheets'" slot="author">
          <a :href="item.authorUrl" target="_blank">{{ item.author }}</a> 发表在 《<a
            v-if="item.sheet.status === 'PUBLISHED'"
            :href="item.sheet.fullPath"
            target="_blank"
            >{{ item.sheet.title }}</a
          ><a
            v-else-if="item.sheet.status === 'DRAFT'"
            href="javascript:void(0)"
            @click="handleSheetPreview(item.sheet.id)"
            >{{ item.sheet.title }}</a
          ><a v-else href="javascript:void(0)">{{ item.sheet.title }}</a
          >》
        </template>
        <!-- <template slot="actions">
          <span>回复</span>
        </template> -->
        <p slot="content" class="comment-content-wrapper" v-html="item.content"></p>
        <a-tooltip slot="datetime" :title="item.createTime | moment">
          <span>{{ item.createTime | timeAgo }}</span>
        </a-tooltip>
      </a-comment>
    </a-list-item>
  </a-list>
</template>

<script>
import apiClient from '@/utils/api-client'

import { marked } from 'marked'

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
        comment.content = marked.parse(comment.content)
        return comment
      })
    }
  },
  created() {
    this.handleListTargetComments()
  },
  methods: {
    handleListTargetComments() {
      this.loading = true
      apiClient.comment
        .latest(this.type, 5, 'PUBLISHED')
        .then(response => {
          this.comments = response.data
        })
        .finally(() => {
          this.loading = false
        })
    },
    handlePostPreview(postId) {
      apiClient.post.getPreviewLinkById(postId).then(response => {
        window.open(response.data, '_blank')
      })
    },
    handleSheetPreview(sheetId) {
      apiClient.post.getPreviewLinkById(sheetId).then(response => {
        window.open(response.data, '_blank')
      })
    }
  }
}
</script>
