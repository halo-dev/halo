<template>
  <a-modal v-model="modalVisible" :width="isMobile() ? '100%' : '50%'" title="附件详情">
    <a-row :gutter="24" type="flex">
      <a-col :lg="9" :md="24" :sm="24" :xl="9" :xs="24">
        <div class="attach-detail-img pb-3">
          <a v-if="isImage" :href="attachment.path" target="_blank">
            <img :src="attachment.path" class="w-full" loading="lazy" />
          </a>
          <div v-else>此文件不支持预览</div>
        </div>
      </a-col>
      <a-col :lg="15" :md="24" :sm="24" :xl="15" :xs="24">
        <a-list itemLayout="horizontal">
          <a-list-item style="padding-top: 0">
            <a-list-item-meta>
              <template v-if="editable" slot="description">
                <a-input ref="nameInput" v-model="attachment.name" @blur="handleUpdateName" />
              </template>
              <template v-else slot="description">{{ attachment.name }}</template>
              <span slot="title">
                附件名：
                <a href="javascript:void(0);">
                  <a-icon type="edit" @click="handleEditName" />
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
            <a-list-item-meta :description="attachment.type | typeText">
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
          <a-list-item v-if="isImage">
            <a-list-item-meta :description="attachment.height + 'x' + attachment.width">
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
            <a-list-item-meta>
              <template #description>
                <a :href="attachment.path" target="_blank">{{ attachment.path }}</a>
              </template>
              <span slot="title">
                普通链接：
                <a href="javascript:void(0);" @click="handleCopyLink(`${encodeURI(attachment.path)}`)">
                  <a-icon type="copy" />
                </a>
              </span>
            </a-list-item-meta>
          </a-list-item>
          <a-list-item v-if="isImage">
            <a-list-item-meta>
              <span slot="description">![{{ attachment.name }}]({{ attachment.path }})</span>
              <span slot="title">
                Markdown 格式：
                <a
                  href="javascript:void(0);"
                  @click="handleCopyLink(`![${attachment.name}](${encodeURI(attachment.path)})`)"
                >
                  <a-icon type="copy" />
                </a>
              </span>
            </a-list-item-meta>
          </a-list-item>
        </a-list>
      </a-col>
    </a-row>

    <template #footer>
      <slot name="extraFooter" />
      <a-popconfirm cancelText="取消" okText="确定" title="你确定要删除该附件？" @confirm="handleDelete">
        <ReactiveButton
          :errored="deleteErrored"
          :loading="deleting"
          erroredText="删除失败"
          icon="delete"
          loadedText="删除成功"
          text="删除"
          type="danger"
          @callback="handleDeletedCallback"
        ></ReactiveButton>
      </a-popconfirm>
    </template>
  </a-modal>
</template>

<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import apiClient from '@/utils/api-client'
import { attachmentTypes } from '@/core/constant'

export default {
  name: 'AttachmentDetailModal',
  mixins: [mixin, mixinDevice],
  filters: {
    typeText(type) {
      return type ? attachmentTypes[type].text : ''
    }
  },
  props: {
    visible: {
      type: Boolean,
      default: true
    },
    attachment: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      editable: false,
      deleting: false,
      deleteErrored: false
    }
  },
  computed: {
    modalVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    },
    isImage() {
      if (!this.attachment || !this.attachment.mediaType) {
        return false
      }
      return this.attachment.mediaType.startsWith('image')
    }
  },
  methods: {
    /**
     * Deletes the attachment
     */
    async handleDelete() {
      try {
        this.deleting = true

        await apiClient.attachment.delete(this.attachment.id)
      } catch (error) {
        this.$log.error(error)
        this.deleteErrored = true
      } finally {
        setTimeout(() => {
          this.deleting = false
        }, 400)
      }
    },

    /**
     * Handles the deletion callback event
     */
    handleDeletedCallback() {
      this.$emit('delete', this.attachment)
      this.deleteErrored = false
      this.modalVisible = false
    },

    /**
     * Shows the edit name input
     */
    handleEditName() {
      this.editable = !this.editable
      if (this.editable) {
        this.$nextTick(() => {
          this.$refs.nameInput.focus()
        })
      }
    },

    /**
     * Updates the attachment name
     */
    async handleUpdateName() {
      if (!this.attachment.name) {
        this.$notification['error']({
          message: '提示',
          description: '附件名称不能为空！'
        })
        return
      }
      try {
        await apiClient.attachment.update(this.attachment.id, this.attachment)
      } catch (error) {
        this.$log.error(error)
      } finally {
        this.editable = false
      }
    },

    /**
     * Handles the copy link event
     * @param {String} link
     */
    handleCopyLink(link) {
      this.$copyText(link)
        .then(message => {
          this.$log.debug('copy', message)
          this.$message.success('复制成功！')
        })
        .catch(err => {
          this.$log.debug('copy.err', err)
          this.$message.error('复制失败！')
        })
    }
  }
}
</script>
