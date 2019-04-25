<template>
  <a-drawer
    title="附件详情"
    :width="isMobile()?'100%':'460'"
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
                  <a href="javascript:void(0);">
                    <a-icon
                      type="edit"
                      @click="handleEditName"
                    />
                  </a>
                </span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :description="attachment.mediaType">
                <span slot="title">附件类型：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :description="attachment.typeProperty">
                <span slot="title">存储位置：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <template slot="description">
                  {{ attachment.size | fileSizeFormat }}
                </template>
                <span slot="title">附件大小：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :description="attachment.height+'x'+attachment.width">
                <span slot="title">图片尺寸：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <template slot="description">
                  {{ attachment.createTime | moment }}
                </template>
                <span slot="title">上传日期：</span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta :description="attachment.path">
                <span slot="title">
                  普通链接：
                  <a href="javascript:void(0);">
                    <a-icon
                      type="copy"
                      @click="doCopyNormalLink"
                    />
                  </a>
                </span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-list-item-meta>
                <span slot="description">![{{ attachment.name }}]({{ attachment.path }})</span>
                <span slot="title">
                  Markdown 格式：
                  <a href="javascript:void(0);">
                    <a-icon
                      type="copy"
                      @click="doCopyMarkdownLink"
                    />
                  </a>
                </span>
              </a-list-item-meta>
            </a-list-item>
          </a-list>
        </a-skeleton>
      </a-col>
    </a-row>
    <a-divider />
    <div class="bottom-control">
      <a-popconfirm
        title="你确定要添加到图库？"
        @confirm="handleAddToPhoto"
        okText="确定"
        cancelText="取消"
        v-if="addToPhoto"
      >
        <a-button
          type="dashed"
          style="marginRight: 8px"
        >添加到图库</a-button>
      </a-popconfirm>
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
import photoApi from '@/api/photo'

export default {
  name: 'AttachmentDetailDrawer',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      detailLoading: true,
      editable: false,
      photo: {}
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
    addToPhoto: {
      type: Boolean,
      required: false,
      default: false
    },
    visiable: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  created() {
    this.loadSkeleton()
  },
  watch: {
    visiable: function(newValue, oldValue) {
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
      attachmentApi.update(this.attachment.id, this.attachment).then(response => {
        this.$log.debug('Updated attachment', response.data.data)
        this.$message.success('附件修改成功')
      })
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
    handleAddToPhoto() {
      this.photo['name'] = this.attachment.name
      this.photo['thumbnail'] = this.attachment.thumbPath
      this.photo['url'] = this.attachment.path
      this.photo['takeTime'] = new Date().getTime()
      photoApi.create(this.photo).then(response => {
        this.$message.success('添加成功！')
      })
    },
    onClose() {
      this.$emit('close', false)
    }
  }
}
</script>

<style scope>
.attach-detail-img img {
  width: 100%;
}
</style>
