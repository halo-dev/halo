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
            <div v-show="nonsupportPreviewVisible">此文件不支持预览</div>
            <img :src="attachment.path" v-show="photoPreviewVisible">
            <video-player
              class="video-player-box"
              v-show="videoPreviewVisible"
              ref="videoPlayer"
              :options="playerOptions"
              :playsinline="true">
            </video-player>
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
import 'video.js/dist/video-js.css'
import { videoPlayer } from 'vue-video-player'

export default {
  name: 'AttachmentDetailDrawer',
  mixins: [mixin, mixinDevice],
  components: {
    videoPlayer
  },
  data() {
    return {
      detailLoading: true,
      editable: false,
      photo: {},
      photoPreviewVisible: false,
      videoPreviewVisible: false,
      nonsupportPreviewVisible: false,
      playerOptions: {
        // videojs options
        muted: true,
        language: 'zh-CN',
        aspectRatio: '16:9',
        fluid: true,
        controls: true,
        loop: false,
        playbackRates: [0.7, 1.0, 1.5, 2.0],
        sources: [{
          type: 'video/mp4',
          src: 'https://cdn.theguardian.tv/webM/2015/07/20/150716YesMen_synd_768k_vp8.webm'
        }],
        poster: '/static/images/author.jpg',
        width: document.documentElement.clientWidth,
        notSupportedMessage: '此视频暂无法播放，请稍后再试'
      }
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
  computed: {
    player() {
      return this.$refs.videoPlayer.player
    }
  },
  watch: {
    visiable: function(newValue, oldValue) {
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
          console.log('copy', message)
          this.$message.success('复制成功！')
        })
        .catch(err => {
          console.log('copy.err', err)
          this.$message.error('复制失败！')
        })
    },
    handleCopyMarkdownLink() {
      const text = `![${this.attachment.name}](${encodeURI(this.attachment.path)})`
      this.$copyText(text)
        .then(message => {
          console.log('copy', message)
          this.$message.success('复制成功！')
        })
        .catch(err => {
          console.log('copy.err', err)
          this.$message.error('复制失败！')
        })
    },
    handleAddToPhoto() {
      this.photo['name'] = this.attachment.name
      this.photo['thumbnail'] = this.attachment.thumbPath
      this.photo['url'] = this.attachment.path
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
          this.videoPreviewVisible = true
          this.photoPreviewVisible = false
          this.nonsupportPreviewVisible = false
          // 设置视频地址
          this.$set(this.playerOptions.sources, 0, {
            type: mediaType,
            src: attachment.path
          })
          console.log(this.playerOptions.sources)
        } else if (prefix === 'image') {
          this.photoPreviewVisible = true
          this.videoPreviewVisible = false
          this.nonsupportPreviewVisible = false
        } else {
          this.nonsupportPreviewVisible = true
          this.videoPreviewVisible = false
          this.photoPreviewVisible = false
        }
      }
    }
    // handleDownLoadPhoto(attachment) {
    //   var path = attachment.path

    //   var index = path.lastIndexOf('/')
    //   var filename = path.substr(index+1, path.length)
    //   //  chrome/firefox
    //   var aTag = document.createElement('a')
    //   aTag.download = filename
    //   aTag.href = path//URL.createObjectURL(blob)
    //   aTag.target = '_blank'
    //   aTag.click()
    //   URL.revokeObjectURL(aTag.href)
    // }
  }
}
</script>

<style scope>
.attach-detail-img img {
  width: 100%;
}
.video-player-box {
  width: 100%;
}
</style>
