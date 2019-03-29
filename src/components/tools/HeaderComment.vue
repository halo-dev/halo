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
        <a-list :dataSource="comments">
          <a-list-item slot="renderItem" slot-scope="item">
            <a-list-item-meta :title="item.content" :description="item.createTime">
              <a-avatar
                style="background-color: white"
                slot="avatar"
                :src="'https://gravatar.loli.net/avatar/' + item.gavatarMd5 + '&d=mm'"
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
