<template>
  <a-drawer
    title="附件详情"
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
          <div class="attach-detail-img">
            <div v-show="nonsupportPreviewVisible">此文件不支持预览</div>
            <a
              :href="attachment.path"
              target="_blank"
            >
              <img
                :src="attachment.path"
                v-show="photoPreviewVisible"
                style="width: 100%;"
                loading="lazy"
              >
            </a>
            <d-player
              ref="player"
              :options="videoOptions"
              v-show="videoPreviewVisible"
              class="video-player-box"
              style="width: 100%;"
            >
            </d-player>
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
                    @blur="doUpdateAttachment"
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
            <a-list-item v-if="photoPreviewVisible">
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
                  <a
                    href="javascript:void(0);"
                    @click="handleCopyNormalLink"
                  >
                    <a-icon type="copy" />
                  </a>
                </span>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item v-if="photoPreviewVisible">
              <a-list-item-meta>
                <span slot="description">![{{ attachment.name }}]({{ attachment.path }})</span>
                <span slot="title">
                  Markdown 格式：
                  <a
                    href="javascript:void(0);"
                    @click="handleCopyMarkdownLink"
                  >
                    <a-icon type="copy" />
                  </a>
                </span>
              </a-list-item-meta>
            </a-list-item>
          </a-list>
        </a-skeleton>
      </a-col>
    </a-row>
    <a-divider class="divider-transparent" />
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
        @confirm="handleDeleteAttachment"
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
import 'vue-dplayer/dist/vue-dplayer.css'
import VueDPlayer from 'vue-dplayer'
import flvjs from 'flv.js'
window.flvjs = flvjs

export default {
  name: 'AttachmentDetailDrawer',
  mixins: [mixin, mixinDevice],
  components: {
    'd-player': VueDPlayer
  },
  data() {
    return {
      detailLoading: true,
      editable: false,
      photo: {},
      photoPreviewVisible: false,
      videoPreviewVisible: false,
      nonsupportPreviewVisible: false,
      player: {},
      videoOptions: {
        lang: 'zh-cn',
        video: {
          url: '',
          type: 'auto'
        }
      }
    }
  },
  model: {
    prop: 'visible',
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
    visible: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  mounted() {
    this.player = this.$refs.player
  },
  watch: {
    visible: function(newValue, oldValue) {
      this.$log.debug('old value', oldValue)
      this.$log.debug('new value', newValue)
      if (newValue) {
        this.loadSkeleton()
      }
    },
    attachment: function(newValue, oldValue) {
      if (newValue) {
        var attachment = newValue
        this.handleJudgeMediaType(attachment)
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
    handleDeleteAttachment() {
      attachmentApi.delete(this.attachment.id).then(response => {
        this.$message.success('删除成功！')
        this.$emit('delete', this.attachment)
        this.onClose()
      })
    },
    handleEditName() {
      this.editable = !this.editable
    },
    doUpdateAttachment() {
      if (!this.attachment.name) {
        this.$notification['error']({
          message: '提示',
          description: '附件名称不能为空！'
        })
        return
      }
      attachmentApi.update(this.attachment.id, this.attachment).then(response => {
        this.$log.debug('Updated attachment', response.data.data)
        this.$message.success('附件修改成功！')
      })
      this.editable = false
    },
    handleCopyNormalLink() {
      const text = `${encodeURI(this.attachment.path)}`
      this.$copyText(text)
        .then(message => {
          this.$log.debug('copy', message)
          this.$message.success('复制成功！')
        })
        .catch(err => {
          this.$log.debug('copy.err', err)
          this.$message.error('复制失败！')
        })
    },
    handleCopyMarkdownLink() {
      const text = `![${this.attachment.name}](${encodeURI(this.attachment.path)})`
      this.$copyText(text)
        .then(message => {
          this.$log.debug('copy', message)
          this.$message.success('复制成功！')
        })
        .catch(err => {
          this.$log.debug('copy.err', err)
          this.$message.error('复制失败！')
        })
    },
    handleAddToPhoto() {
      this.photo['name'] = this.attachment.name
      this.photo['thumbnail'] = encodeURI(this.attachment.thumbPath)
      this.photo['url'] = encodeURI(this.attachment.path)
      this.photo['takeTime'] = new Date().getTime()
      photoApi.create(this.photo).then(response => {
        this.$message.success('添加成功！')
        this.photo = {}
      })
    },
    onClose() {
      this.$emit('close', false)
    },
    handleJudgeMediaType(attachment) {
      var mediaType = attachment.mediaType
      // 判断文件类型
      if (mediaType) {
        var prefix = mediaType.split('/')[0]

        if (prefix === 'video' || prefix === 'flv') {
          // 控制各个组件的显示
          this.handlePreviewVisible(false, true, false)

          // 去除视频地址后面的参数
          var lastIndex = attachment.path.lastIndexOf('?')
          var path = attachment.path.substring(0, lastIndex)

          // 设置视频地址
          this.$set(this.videoOptions.video, 'url', path)
          this.$log.debug('video url', path)
        } else if (prefix === 'image') {
          this.handlePreviewVisible(true, false, false)
        } else {
          this.handlePreviewVisible(false, false, true)
        }
      }
    },
    handlePreviewVisible(photo, video, nonsupport) {
      // 为了更好的使vue监听到组件变化及时刷新,方式修改组件后需要刷新才能显示一下部分
      this.$set(this, 'photoPreviewVisible', photo)
      this.$set(this, 'videoPreviewVisible', video)
      this.$set(this, 'nonsupportPreviewVisible', nonsupport)
    }
  }
}
</script>
