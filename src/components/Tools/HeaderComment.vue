<template>
  <a-popover
    v-model="visible"
    trigger="click"
    placement="bottomRight"
    :autoAdjustOverflow="true"
    :arrowPointAtCenter="true"
    overlayClassName="header-comment-wrapper"
    :overlayStyle="{ width: '300px', top: '50px' }"
    title="待审核评论"
  >
    <template slot="content">
      <a-spin :spinning="loadding">
        <a-list :dataSource="converttedComments">
          <a-list-item
            slot="renderItem"
            slot-scope="item"
          >
            <a-list-item-meta>
              <a-avatar
                style="background-color: white"
                slot="avatar"
                :src="'https://gravatar.loli.net/avatar/' + item.gavatarMd5 + '&d=mm'"
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
      </a-spin>
    </template>
    <span
      @click="fetchComment"
      class="header-comment"
    >
      <a-badge
        dot
        v-if="comments.length > 0"
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
import commentApi from '@/api/postComment'
import marked from 'marked'

export default {
  name: 'HeaderComment',
  data() {
    return {
      loadding: false,
      visible: false,
      comments: []
    }
  },
  created() {
    this.getComment()
  },
  computed: {
    converttedComments() {
      return this.comments.map(comment => {
        comment.content = marked(comment.content, { sanitize: true })
        return comment
      })
    }
  },
  methods: {
    fetchComment() {
      if (!this.visible) {
        this.loadding = true
        this.getComment()
      } else {
        this.loadding = false
      }
      this.visible = !this.visible
    },
    getComment() {
      commentApi.listLatest(5, 'AUDITING').then(response => {
        this.comments = response.data.data
        this.loadding = false
      })
    }
  }
}
</script>

<style lang="css">
.header-comment-wrapper {
  top: 50px !important;
}
</style>
<style lang="less" scoped>
.header-comment {
  display: inline-block;
  transition: all 0.3s;

  span {
    vertical-align: initial;
  }
}
</style>
