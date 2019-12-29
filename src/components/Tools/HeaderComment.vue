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
      <a-spin :spinning="loading">
        <div class="custom-tab-wrapper">
          <a-tabs>
            <a-tab-pane
              tab="文章"
              key="1"
            >
              <a-list :dataSource="converttedPostComments">
                <a-list-item
                  slot="renderItem"
                  slot-scope="item"
                >
                  <a-list-item-meta>
                    <a-avatar
                      style="background-color: white"
                      slot="avatar"
                      :src="'//cn.gravatar.com/avatar/' + item.gravatarMd5 + '&d=mm'"
                      size="large"
                    />
                    <template slot="title">
                      <a
                        :href="item.authorUrl"
                        target="_blank"
                      >{{ item.author }}</a>：<span v-html="item.content"></span>
                    </template>
                    <template slot="description">
                      {{ item.createTime | timeAgo }}
                    </template>
                  </a-list-item-meta>
                </a-list-item>
              </a-list>
            </a-tab-pane>
            <a-tab-pane
              tab="页面"
              key="2"
            >
              <a-list :dataSource="converttedSheetComments">
                <a-list-item
                  slot="renderItem"
                  slot-scope="item"
                >
                  <a-list-item-meta>
                    <a-avatar
                      style="background-color: white"
                      slot="avatar"
                      :src="'//cn.gravatar.com/avatar/' + item.gravatarMd5 + '&d=mm'"
                      size="large"
                    />
                    <template slot="title">
                      <a
                        :href="item.authorUrl"
                        target="_blank"
                      >{{ item.author }}</a>：<span v-html="item.content"></span>
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
      </a-spin>
    </template>
    <span
      @click="fetchComment"
      class="header-comment"
    >
      <a-badge
        dot
        v-if="postComments.length > 0 || sheetComments.length > 0"
      >
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
      loading: false,
      visible: false,
      postComments: [],
      sheetComments: []
    }
  },
  created() {
    this.getComment()
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
  methods: {
    fetchComment() {
      if (!this.visible) {
        this.loading = true
        this.getComment()
      } else {
        this.loading = false
      }
      this.visible = !this.visible
    },
    getComment() {
      commentApi.latestComment('posts', 5, 'AUDITING').then(response => {
        this.postComments = response.data.data
        this.loading = false
      })
      commentApi.latestComment('sheets', 5, 'AUDITING').then(response => {
        this.sheetComments = response.data.data
        this.loading = false
      })
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
