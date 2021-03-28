<template>
  <a-popover
    v-model="visible"
    trigger="click"
    placement="bottomRight"
    :autoAdjustOverflow="true"
    :arrowPointAtCenter="true"
    :overlayStyle="{ width: '300px', top: '50px' }"
    title="待审核评论"
  >
    <template slot="content">
      <div class="custom-tab-wrapper">
        <a-tabs v-model="activeKey" @change="handleTabsChanged">
          <a-tab-pane tab="文章" key="post">
            <a-list :loading="postCommentsLoading" :dataSource="converttedPostComments">
              <a-list-item slot="renderItem" slot-scope="item">
                <a-list-item-meta>
                  <a-avatar class="bg-white" slot="avatar" :src="item.avatar" size="large" />
                  <template slot="title">
                    <a :href="item.authorUrl" target="_blank">{{ item.author }}</a
                    >：<span v-html="item.content"></span>
                  </template>
                  <template slot="description">
                    {{ item.createTime | timeAgo }}
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </a-list>
          </a-tab-pane>
          <a-tab-pane tab="页面" key="sheet">
            <a-list :loading="sheetCommentsLoading" :dataSource="converttedSheetComments">
              <a-list-item slot="renderItem" slot-scope="item">
                <a-list-item-meta>
                  <a-avatar class="bg-white" slot="avatar" :src="item.avatar" size="large" />
                  <template slot="title">
                    <a :href="item.authorUrl" target="_blank">{{ item.author }}</a
                    >：<span v-html="item.content"></span>
                  </template>
                  <template slot="description">
                    {{ item.createTime | timeAgo }}
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </a-list>
          </a-tab-pane>
        </a-tabs>
      </div>
    </template>
    <span class="header-comment">
      <a-badge dot v-if="postComments.length > 0 || sheetComments.length > 0">
        <a-icon type="bell" />
      </a-badge>
      <a-badge v-else>
        <a-icon type="bell" />
      </a-badge>
    </span>
  </a-popover>
</template>

<script>
import commentApi from '@/api/comment'
import marked from 'marked'

export default {
  name: 'HeaderComment',
  data() {
    return {
      activeKey: 'post',
      visible: false,
      postComments: [],
      postCommentsLoading: false,
      sheetComments: [],
      sheetCommentsLoading: false
    }
  },
  computed: {
    converttedPostComments() {
      return this.postComments.map(comment => {
        comment.content = marked(comment.content)
        return comment
      })
    },
    converttedSheetComments() {
      return this.sheetComments.map(comment => {
        comment.content = marked(comment.content)
        return comment
      })
    }
  },
  created() {
    this.handleListPostAuditingComments(false)
    this.handleListSheetAuditingComments(false)
  },
  watch: {
    visible(value) {
      if (value) {
        if (this.activeKey === 'post') {
          this.handleListPostAuditingComments(false)
        } else if (this.activeKey === 'sheet') {
          this.handleListSheetAuditingComments(false)
        }
      }
    }
  },
  methods: {
    handleListPostAuditingComments(enableLoading = true) {
      if (enableLoading) {
        this.postCommentsLoading = true
      }
      commentApi
        .latestComment('posts', 5, 'AUDITING')
        .then(response => {
          this.postComments = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.postCommentsLoading = false
          }, 200)
        })
    },
    handleListSheetAuditingComments(enableLoading = true) {
      if (enableLoading) {
        this.sheetCommentsLoading = true
      }
      commentApi
        .latestComment('sheets', 5, 'AUDITING')
        .then(response => {
          this.sheetComments = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.sheetCommentsLoading = false
          }, 200)
        })
    },
    handleTabsChanged(activeKey) {
      if (activeKey === 'post') {
        this.handleListPostAuditingComments()
      } else if (activeKey === 'sheet') {
        this.handleListSheetAuditingComments()
      }
    }
  }
}
</script>
<style lang="less" scoped>
.header-comment {
  display: inline-block;
  transition: all 0.3s;

  span {
    vertical-align: initial;
  }
}
</style>
