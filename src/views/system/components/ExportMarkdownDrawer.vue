<template>
  <a-drawer
    :afterVisibleChange="handleAfterVisibleChanged"
    :visible="visible"
    :width="isMobile() ? '100%' : '480'"
    closable
    destroyOnClose
    title="导出文章为 Markdown 文档"
    @close="onClose"
  >
    <a-row align="middle" type="flex">
      <a-col :span="24">
        <a-alert
          banner
          closable
          message="注意：导出后的数据文件存储在临时文件中，重启服务器会造成备份文件的丢失，所以请尽快下载。"
        />
        <a-divider>历史文件</a-divider>
        <a-list :dataSource="files" :loading="loading" itemLayout="vertical" size="small">
          <a-list-item slot="renderItem" slot-scope="file">
            <a-button
              slot="extra"
              :loading="file.deleting"
              icon="delete"
              style="color: red"
              type="link"
              @click="handleFileDeleteClick(file)"
              >删除
            </a-button>
            <a-list-item-meta>
              <a slot="title" href="javascript:void(0)" @click="handleDownloadMarkdownPackage(file)">
                <a-icon style="color: #52c41a" type="schedule" />
                {{ file.filename }}
              </a>
              <p slot="description">{{ file.updateTime | timeAgo }}/{{ file.fileSize | fileSizeFormat }}</p>
            </a-list-item-meta>
          </a-list-item>
        </a-list>
      </a-col>
    </a-row>
    <a-divider class="divider-transparent" />
    <div class="bottom-control">
      <a-space>
        <a-popconfirm
          cancelText="否"
          okText="是"
          title="是否同时为 Markdown 文档生成 Front Matter？"
          @cancel="handleExportClick(false)"
          @confirm="handleExportClick(true)"
        >
          <ReactiveButton
            :errored="backupErrored"
            :loading="backuping"
            erroredText="备份失败"
            icon="download"
            loadedText="备份成功"
            text="备份"
            type="primary"
            @callback="handleBackupedCallback"
          ></ReactiveButton>
        </a-popconfirm>
        <a-button :loading="loading" icon="reload" type="dashed" @click="handleListBackups">刷新</a-button>
      </a-space>
    </div>
  </a-drawer>
</template>
<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import apiClient from '@/utils/api-client'

export default {
  name: 'ExportDataDrawer',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      backuping: false,
      loading: false,
      backupErrored: false,
      files: []
    }
  },
  model: {
    prop: 'visible',
    event: 'close'
  },
  props: {
    visible: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  methods: {
    handleAfterVisibleChanged(visible) {
      if (visible) {
        this.handleListBackups()
      }
    },
    handleListBackups() {
      this.loading = true
      apiClient.backup
        .listMarkdownBackups()
        .then(response => {
          this.files = response.data
        })
        .finally(() => {
          this.loading = false
        })
    },
    handleExportClick(needFrontMatter = false) {
      this.backuping = true
      apiClient.backup
        .backupMarkdown({ needFrontMatter })
        .catch(() => {
          this.backupErrored = true
        })
        .finally(() => {
          setTimeout(() => {
            this.backuping = false
          }, 400)
        })
    },
    handleBackupedCallback() {
      if (this.backupErrored) {
        this.backupErrored = false
      } else {
        this.handleListBackups()
      }
    },
    handleFileDeleteClick(file) {
      file.deleting = true
      apiClient.backup.deleteMarkdownBackup(file.filename).finally(() => {
        setTimeout(() => {
          file.deleting = false
        }, 400)
        this.handleListBackups()
      })
    },
    handleDownloadMarkdownPackage(item) {
      apiClient.backup
        .getMarkdownBackup(item.filename)
        .then(response => {
          const downloadElement = document.createElement('a')
          const href = new window.URL(response.data.downloadLink)
          downloadElement.href = href
          downloadElement.download = response.data.filename
          document.body.appendChild(downloadElement)
          downloadElement.click()
          document.body.removeChild(downloadElement)
          window.URL.revokeObjectURL(href)
        })
        .catch(() => {
          this.$message.error('下载失败！')
        })
    },
    onClose() {
      this.$emit('close', false)
    }
  }
}
</script>
