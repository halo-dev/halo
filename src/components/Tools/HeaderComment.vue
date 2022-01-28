<template>
  <a-popover
    :arrowPointAtCenter="true"
    :autoAdjustOverflow="true"
    :overlayStyle="{ width: '400px', top: '50px' }"
    overlayClassName="header-comment-popover"
    placement="bottomRight"
    title="待审核评论"
    trigger="click"
  >
    <template #content>
      <div class="custom-tab-wrapper">
        <a-tabs v-model="activeKey" :animated="{ inkBar: true, tabPane: false }" @change="handleListAuditingComments">
          <a-tab-pane v-for="target in targets" :key="target.key" :tab="target.label">
            <CommentListView :comments="comments[target.dataKey]" :loading="comments.loading" />
          </a-tab-pane>
        </a-tabs>
      </div>
    </template>
    <span class="inline-block transition-all">
      <a-badge v-if="comments.post.length || comments.sheet.length || comments.journal.length" dot>
        <a-icon type="bell" />
      </a-badge>
      <a-badge v-else>
        <a-icon type="bell" />
      </a-badge>
    </span>
  </a-popover>
</template>

<script>
import apiClient from '@/utils/api-client'

const targets = [
  {
    key: 'posts',
    dataKey: 'post',
    label: '文章'
  },
  {
    key: 'sheets',
    dataKey: 'sheet',
    label: '页面'
  },
  {
    key: 'journals',
    dataKey: 'journal',
    label: '日志'
  }
]

export default {
  name: 'HeaderComment',
  data() {
    return {
      targets: targets,
      activeKey: 'posts',
      comments: {
        post: [],
        sheet: [],
        journal: [],
        loading: false
      }
    }
  },
  created() {
    this.handleListAuditingComments()
  },
  methods: {
    async handleListAuditingComments() {
      try {
        this.comments.loading = true

        const params = { status: 'AUDITING', size: 20 }

        const responses = await Promise.all(
          targets.map(target => {
            return apiClient.comment.list(target.key, params)
          })
        )

        this.comments.post = responses[0].data.content
        this.comments.sheet = responses[1].data.content
        this.comments.journal = responses[2].data.content
      } catch (e) {
        this.$log.error('Failed to get auditing comments', e)
      } finally {
        this.comments.loading = false
      }
    }
  }
}
</script>
