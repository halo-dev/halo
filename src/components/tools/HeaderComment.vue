<template>
  <a-popover
    v-model="visible"
    trigger="click"
    placement="bottomRight"
    :autoAdjustOverflow="true"
    :arrowPointAtCenter="true"
    overlayClassName="header-comment-wrapper"
    :overlayStyle="{ width: '300px', top: '50px' }"
  >
    <template slot="content">
      <a-spin :spinning="loadding">
        <a-list>
          <a-list-item v-for="comment in comments" :key="comment.id">
            <a-list-item-meta title="立个Flag，共同努力" description="一天前">
              <a-avatar
                style="background-color: white"
                slot="avatar"
                src="https://gravatar.loli.net/avatar/7cc7f29278071bd4dce995612d428834?s=256&d=mm"
              />
            </a-list-item-meta>
          </a-list-item>
          <a-list-item>
            <a-list-item-meta title="感谢！！！非常好的博客系统" description="一天前">
              <a-avatar
                style="background-color: white"
                slot="avatar"
                src="https://gravatar.loli.net/avatar/7cc7f29278071bd4dce995612d428834?s=256&d=mm"
              />
            </a-list-item-meta>
          </a-list-item>
          <a-list-item>
            <a-list-item-meta title="token就是P2DygQ1psV86JIag，百度验证是在百度资源平台绑定域名的时候验证用的。" description="一天前">
              <a-avatar
                style="background-color: white"
                slot="avatar"
                src="https://gravatar.loli.net/avatar/7cc7f29278071bd4dce995612d428834?s=256&d=mm"
              />
            </a-list-item-meta>
          </a-list-item>
        </a-list>
      </a-spin>
    </template>
    <span @click="fetchComment" class="header-comment">
      <a-badge dot>
        <a-icon type="bell"/>
      </a-badge>
    </span>
  </a-popover>
</template>

<script>
import commentApi from '@/api/comment'
export default {
  name: 'HeaderComment',
  data() {
    return {
      loadding: false,
      visible: false,
      comments: []
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
      commentApi.listLatest().then(response => {
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
