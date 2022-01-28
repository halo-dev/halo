<template>
  <a-list :dataSource="comments" :loading="loading" item-layout="vertical">
    <template #renderItem="item, index">
      <a-list-item :key="index" class="!p-0">
        <a-comment :avatar="item.avatar">
          <template #author>
            <a v-if="item.authorUrl" :href="item.authorUrl" class="!text-gray-800 hover:!text-blue-500" target="_blank">
              {{ item.author }}
            </a>
            <span v-else class="!text-gray-500">{{ item.author }}</span>
            发表在
            <span class="hover:!text-blue-500 cursor-pointer" @click="handleOpenTarget(item)">
              《{{ targetTitle(item) }}》
            </span>
          </template>
          <template #content>
            <p v-html="$options.filters.markdownRender(item.content)" />
          </template>
          <template #datetime>
            <a-tooltip :title="item.createTime | moment">
              <span>{{ item.createTime | timeAgo }}</span>
            </a-tooltip>
          </template>
        </a-comment>
      </a-list-item>
    </template>
  </a-list>
</template>
<script>
import { datetimeFormat } from '@/utils/datetime'
import apiClient from '@/utils/api-client'

export default {
  name: 'CommentListView',
  props: {
    comments: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    targetTitle() {
      return function (comment) {
        if (comment.post) {
          return comment.post.title
        }
        if (comment.sheet) {
          return comment.sheet.title
        }
        if (comment.journal) {
          return datetimeFormat(comment.journal.createTime)
        }
        return ''
      }
    }
  },
  methods: {
    async handleOpenTarget(comment) {
      const { post, sheet } = comment
      if (post || sheet) {
        const { status, fullPath, id } = post || sheet
        if (['PUBLISHED', 'INTIMATE'].includes(status)) {
          window.open(fullPath, '_blank')
          return
        }
        if (status === 'DRAFT') {
          const target = post ? 'post' : 'sheet'
          const link = await apiClient[target].getPreviewLinkById(id)
          window.open(link, '_blank')
        }
      }
    }
  }
}
</script>
