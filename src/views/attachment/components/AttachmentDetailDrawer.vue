<template>
  <a-drawer
    title="附件详情"
    :width="isMobile()?'100%':'560'"
    closable
    :visible="visiable"
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
          <div class="attach-detail-img">
            <img :src="attachment.path">
          </div>
        </a-skeleton>
      </a-col>
      <a-divider />
      <a-col :span="24">
        <a-skeleton
          active
          :loading="detailLoading"
          :paragraph="{rows: 8}"
        >
          <a-list itemLayout="horizontal">
            <a-list-item>
              <a-list-item-meta>
                <template
                  slot="description"
                  v-if="editable"
                >
                  <a-input
                    v-model="attachment.name"
                    @blur="updateAttachment"
                  />
                </template>
                <template
                  slot="description"
                  v-else
                >{{ attachment.name }}</template>
                <span slot="title">
                  附件名：
                  <a-icon
                    type="edit"
                    @click="handleEditName"
                  />
                </span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :description="attachment.mediaType">
                <span slot="title">附件类型：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :description="attachment.size">
                <span slot="title">附件大小：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :description="attachment.height+'x'+attachment.width">
                <span slot="title">图片尺寸：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :description="attachment.createTime">
                <span slot="title">上传日期：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :description="attachment.path">
                <span slot="title">
                  普通链接：
                  <a-icon
                    type="copy"
                    @click="doCopyNormalLink"
                  />
                </span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :description="'!['+attachment.name+']('+attachment.path+')'">
                <span slot="title">
                  Markdown 格式：
                  <a-icon
                    type="copy"
                    @click="doCopyMarkdownLink"
                  />
                </span>
              </a-list-item-meta>
            </a-list-item>
          </a-list>
        </a-skeleton>
      </a-col>
    </a-row>
    <div class="attachment-control">
      <a-popconfirm
        title="你确定要删除该附件？"
        @confirm="deleteAttachment"
        okText="确定"
        cancelText="取消"
      >
        <a-button type="danger">删除</a-button>
      </a-popconfirm>
    </div>
  </a-drawer>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import attachmentApi from '@/api/attachment'

export default {
  name: 'AttachmentDetailDrawer',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      detailLoading: true,
      editable: false
    }
  },
  model: {
    prop: 'visiable',
    event: 'close'
  },
  props: {
    attachment: {
      type: Object,
      required: true
    },
    visiable: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  created() {
    setTimeout(() => {
      this.detailLoading = false
    }, 300)
  },
  methods: {
    deleteAttachment() {
      attachmentApi.delete(this.attachment.id).then(response => {
        this.$message.success('删除成功！')
        this.$emit('delete', this.attachment)
        this.onClose()
      })
    },
    handleEditName() {
      this.editable = !this.editable
    },
    updateAttachment() {
      this.$message.success('修改')
      this.editable = false
    },
    doCopyNormalLink() {
      const text = `${this.attachment.path}`
      this.$copyText(text)
        .then(message => {
          console.log('copy', message)
          this.$message.success('复制成功')
        })
        .catch(err => {
          console.log('copy.err', err)
          this.$message.error('复制失败')
        })
    },
    doCopyMarkdownLink() {
      const text = `![${this.attachment.name}](${this.attachment.path})`
      this.$copyText(text)
        .then(message => {
          console.log('copy', message)
          this.$message.success('复制成功')
        })
        .catch(err => {
          console.log('copy.err', err)
          this.$message.error('复制失败')
        })
    },
    onClose() {
      this.$emit('close', false)
    }
  }
}
</script>

<style>
.attach-detail-img img {
  width: 100%;
}
</style>
