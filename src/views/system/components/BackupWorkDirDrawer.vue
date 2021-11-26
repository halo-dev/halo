<template>
  <a-drawer
    :afterVisibleChange="handleAfterVisibleChanged"
    :visible="visible"
    :width="isMobile() ? '100%' : '480'"
    closable
    destroyOnClose
    title="整站备份"
    @close="onClose"
  >
    <a-row align="middle" type="flex">
      <a-col :span="24">
        <a-alert
          banner
          closable
          message="注意：备份后生成的压缩文件存储在临时文件中，重启服务器会造成备份文件的丢失，所以请尽快下载。"
        />
        <a-divider>历史备份</a-divider>
        <a-list :dataSource="backups" :loading="loading" itemLayout="vertical" size="small">
          <a-list-item slot="renderItem" slot-scope="backup">
            <a-button
              slot="extra"
              :loading="backup.deleting"
              icon="delete"
              style="color: red"
              type="link"
              @click="handleBackupDeleteClick(backup)"
              >删除
            </a-button>
            <a-list-item-meta>
              <a slot="title" href="javascript:void(0)" @click="handleDownloadBackupPackage(backup)">
                <a-icon style="color: #52c41a" type="schedule" />
                {{ backup.filename }}
              </a>
              <p slot="description">{{ backup.updateTime | timeAgo }}/{{ backup.fileSize | fileSizeFormat }}</p>
            </a-list-item-meta>
          </a-list-item>
        </a-list>
      </a-col>
    </a-row>
    <a-divider class="divider-transparent" />
    <div class="bottom-control">
      <a-space>
        <a-button icon="download" type="primary" @click="handleBackupClick">备份</a-button>
        <a-button :loading="loading" icon="reload" type="dashed" @click="handleListBackups">刷新</a-button>
      </a-space>
    </div>
    <a-modal v-model="optionsModal.visible" title="备份选项">
      <template slot="footer">
        <a-button @click="() => (optionsModal.visible = false)">取消</a-button>
        <ReactiveButton
          :errored="backupErrored"
          :loading="backuping"
          erroredText="备份失败"
          loadedText="备份成功"
          text="确认"
          type="primary"
          @callback="handleBackupedCallback"
          @click="handleBackupConfirmed"
        ></ReactiveButton>
      </template>
      <a-checkbox-group v-model="optionsModal.selected" style="width: 100%">
        <a-row>
          <a-col v-for="item in optionsModal.options" :key="item" :span="8">
            <a-checkbox :value="item">
              {{ item }}
            </a-checkbox>
          </a-col>
        </a-row>
      </a-checkbox-group>
    </a-modal>
  </a-drawer>
</template>
<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import apiClient from '@/utils/api-client'

export default {
  name: 'BackupWorkDirDrawer',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      backuping: false,
      loading: false,
      backupErrored: false,
      backups: [],
      optionsModal: {
        options: [],
        visible: false,
        selected: []
      }
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
        .listWorkdirBackups()
        .then(response => {
          this.backups = response.data
        })
        .finally(() => {
          this.loading = false
        })
    },
    handleBackupClick() {
      apiClient.backup.getWorkdirBackupOptions().then(response => {
        this.optionsModal = {
          visible: true,
          options: response.data,
          selected: response.data
        }
      })
    },
    handleBackupConfirmed() {
      this.backuping = true
      apiClient.backup
        .backupWorkdir(this.optionsModal.selected)
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
        this.optionsModal.visible = false
        this.handleListBackups()
      }
    },
    handleBackupDeleteClick(backup) {
      backup.deleting = true
      apiClient.backup.deleteWorkdirBackup(backup.filename).finally(() => {
        setTimeout(() => {
          backup.deleting = false
        }, 400)
        this.handleListBackups()
      })
    },
    handleDownloadBackupPackage(item) {
      apiClient.backup
        .getWorkdirBackup(item.filename)
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
        .catch(error => {
          this.$message.error(error.data.message)
        })
    },
    onClose() {
      this.$emit('close', false)
    }
  }
}
</script>
