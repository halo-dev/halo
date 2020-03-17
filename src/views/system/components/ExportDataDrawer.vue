<template>
  <a-drawer
    title="数据导出"
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
        <a-alert
          message="注意：导出后的数据文件存储在临时文件中，重启服务器会造成备份文件的丢失，所以请尽快下载！"
          banner
          closable
        />
        <a-divider>历史文件</a-divider>
        <a-list
          itemLayout="vertical"
          size="small"
          :dataSource="files"
        >
          <a-list-item
            slot="renderItem"
            slot-scope="file"
          >
            <a-button
              slot="extra"
              type="link"
              style="color: red"
              icon="delete"
              :loading="deleting"
              @click="handleFileDeleteClick(file.filename)"
            >删除</a-button>
            <a-list-item-meta>
              <a
                slot="title"
                :href="file.downloadLink"
              >
                <a-icon
                  type="schedule"
                  style="color: #52c41a"
                />
                {{ file.filename }}
              </a>
              <p slot="description">{{ file.updateTime | timeAgo }}/{{ file.fileSize | fileSizeFormat }}</p>
            </a-list-item-meta>
          </a-list-item>
          <div
            v-if="loading"
            class="loading-container"
            style="position: absolute;bottom: 40px; width: 100%;text-align: center;"
          >
            <a-spin />
          </div>
        </a-list>
      </a-col>
    </a-row>
    <a-divider class="divider-transparent" />
    <div class="bottom-control">
      <a-button
        type="primary"
        icon="download"
        style="marginRight: 8px"
        :loading="backuping"
        @click="handleExportClick"
      >备份</a-button>
      <a-button
        type="dashed"
        icon="reload"
        :loading="loading"
        @click="handleFilesRefreshClick"
      >刷新</a-button>
    </div>
  </a-drawer>
</template>
<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import backupApi from '@/api/backup'
export default {
  name: 'ExportDataDrawer',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      backuping: false,
      loading: false,
      deleting: false,
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
  watch: {
    visible: function(newValue, oldValue) {
      if (newValue) {
        this.listFiles()
      }
    }
  },
  methods: {
    listFiles() {
      this.loading = true
      backupApi
        .listExportedData()
        .then(response => {
          this.files = response.data.data
        })
        .finally(() => (this.loading = false))
    },
    handleExportClick() {
      this.backuping = true
      backupApi
        .exportData()
        .then(response => {
          this.$message.success('导出成功！')
          this.listFiles()
        })
        .finally(() => {
          this.backuping = false
        })
    },
    handleFileDeleteClick(filename) {
      this.deleting = true
      backupApi
        .deleteExportedData(filename)
        .then(response => {
          this.$message.success('删除成功！')
          this.listFiles()
        })
        .finally(() => (this.deleting = false))
    },
    handleFilesRefreshClick() {
      this.listFiles()
    },
    onClose() {
      this.$emit('close', false)
    }
  }
}
</script>
