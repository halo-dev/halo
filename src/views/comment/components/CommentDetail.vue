<template>
  <a-drawer
    title="评论详情"
    :width="isMobile()?'100%':'480'"
    closable
    :visible="visible"
    destroyOnClose
    @close="onClose"
  >
    <a-row
      type="flex"
      align="middle"
    >
      <a-col :span="24">
        <a-skeleton
          active
          :loading="detailLoading"
          :paragraph="{rows: 8}"
        >
          <a-list itemLayout="horizontal">
            <a-list-item>
              <a-list-item-meta :description="comment.author">
                <span slot="title">评论者昵称：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :description="comment.email">
                <span slot="title">评论者邮箱：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :description="comment.ipAddress">
                <span slot="title">评论者 IP：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <a
                  slot="description"
                  target="_blank"
                  :href="comment.authorUrl"
                >{{ comment.authorUrl }}</a>
                <span slot="title">评论者网址：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <span slot="description">
                  <a-badge :status="comment.statusProperty.status" :text="comment.statusProperty.text"/>
                </span>
                <span slot="title">评论状态：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <a
                  slot="description"
                  target="_blank"
                  :href="comment.post.fullPath"
                  v-if="this.type=='posts'"
                >{{ comment.post.title }}</a>
                <a
                  slot="description"
                  target="_blank"
                  :href="comment.sheet.fullPath"
                  v-else-if="this.type=='sheets'"
                >{{ comment.sheet.title }}</a>
                <span
                  slot="title"
                  v-if="this.type=='posts'"
                >评论文章：</span>
                <span
                  slot="title"
                  v-else-if="this.type=='sheets'"
                >评论页面：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <template
                  slot="description"
                  v-if="editable"
                >
                  <a-input
                    type="textarea"
                    :autoSize="{ minRows: 5 }"
                    v-model="comment.content"
                  />
                </template>
                <span
                  slot="description"
                  v-html="comment.content"
                  v-else
                ></span>
                <span slot="title">评论内容：</span>
              </a-list-item-meta>
            </a-list-item>
          </a-list>
        </a-skeleton>
      </a-col>
    </a-row>
    <a-divider class="divider-transparent" />
    <div class="bottom-control">
      <a-button
        type="dashed"
        style="marginRight: 8px"
        @click="handleEditComment"
        v-if="!editable"
      >编辑</a-button>
      <a-button
        type="primary"
        style="marginRight: 8px"
        @click="handleUpdateComment"
        v-if="editable"
      >保存</a-button>
      <a-popconfirm
        title="你确定要将此评论者加入黑名单？"
        okText="确定"
        cancelText="取消"
      >
        <a-button type="danger">加入黑名单</a-button>
      </a-popconfirm>
    </div>
  </a-drawer>
</template>
<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import commentApi from '@/api/comment'
export default {
  name: 'CommentDetail',
  mixins: [mixin, mixinDevice],
  components: {},
  data() {
    return {
      detailLoading: true,
      editable: false,
      commentStatus: commentApi.commentStatus,
      keys: ['blog_url']
    }
  },
  model: {
    prop: 'visible',
    event: 'close'
  },
  props: {
    comment: {
      type: Object,
      required: true
    },
    visible: {
      type: Boolean,
      required: false,
      default: true
    },
    type: {
      type: String,
      required: false,
      default: 'posts',
      validator: function(value) {
        return ['posts', 'sheets', 'journals'].indexOf(value) !== -1
      }
    }
  },
  watch: {
    visible: function(newValue, oldValue) {
      this.$log.debug('old value', oldValue)
      this.$log.debug('new value', newValue)
      if (newValue) {
        this.loadSkeleton()
      }
    }
  },
  methods: {
    loadSkeleton() {
      this.detailLoading = true
      setTimeout(() => {
        this.detailLoading = false
      }, 500)
    },
    handleEditComment() {
      this.editable = true
    },
    handleUpdateComment() {
      commentApi.update(this.type, this.comment.id, this.comment).then(response => {
        this.$log.debug('Updated comment', response.data.data)
        this.$message.success('评论修改成功！')
      })
      this.editable = false
    },
    onClose() {
      this.$emit('close', false)
    }
  }
}
</script>
