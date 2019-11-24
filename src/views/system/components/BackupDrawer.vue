<template>
  <a-drawer
    title="博客备份"
    :width="isMobile()?'100%':'460'"
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
        <a-card
          :bordered="false"
          :bodyStyle="{ padding: '0' }"
        >
          <a-list
            itemLayout="horizontal"
            :dataSource="backupTips"
          >
            <a-list-item
              slot="renderItem"
              slot-scope="backupTip"
            >
              <a-list-item-meta :description="backupTip.description">
                <h4 slot="title">{{ backupTip.title }}</h4>
              </a-list-item-meta>
              <a-alert
                slot="extra"
                v-if="backupTip.alert"
                :message="backupTip.alert.message"
                :type="backupTip.alert.type"
                banner
              />
            </a-list-item>
          </a-list>

          <a-divider>历史备份</a-divider>

          <a-list
            itemLayout="vertical"
            size="small"
            :dataSource="backups"
          >
            <a-list-item
              slot="renderItem"
              slot-scope="backup"
            >
              <a-button
                slot="extra"
                type="link"
                style="color: red"
                icon="delete"
                :loading="deleting"
                @click="handleBackupDeleteClick(backup.filename)"
              >删除</a-button>
              <a-list-item-meta>
                <a
                  slot="title"
                  :href="backup.downloadUrl"
                >
                  <a-icon
                    type="schedule"
                    style="color: #52c41a"
                  />
                  {{ backup.filename }}
                </a>
                <p slot="description">{{ backup.updateTime | timeAgo }}/{{ backup.fileSize | fileSizeFormat }}</p>
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
        </a-card>
      </a-col>
    </a-row>
    <a-divider class="divider-transparent" />
    <div class="bottom-control">
      <a-button
        type="primary"
        icon="download"
        style="marginRight: 8px"
        :loading="backuping"
        @click="handleBackupClick"
      >备份</a-button>
      <a-button
        type="dash"
        icon="reload"
        :loading="loading"
        @click="handleBAckupRefreshClick"
      >刷新</a-button>
    </div>
  </a-drawer>
</template>
<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import backupApi from '@/api/backup'
export default {
  name: 'BackupDrawer',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      backuping: false,
      loading: false,
      deleting: false,
      backups: [],
      backupTips: [
        {
          title: '博客备份',
          description:
            '将会压缩 Halo 的工作目录到临时文件中，并提供下载链接。如果附件太多的话，可能会十分耗时，请耐心等待！',
          alert: {
            type: 'warning',
            message: '注意：备份后生成的压缩文件存储在临时文件中，重启服务器会造成备份文件的丢失，所以请尽快下载！'
          }
        },
        { title: '备份查询', description: '查询近期的备份，按照备份时间递减排序。' },
        { title: '备份删除', description: '删除已经备份的内容。' },
        {
          title: '版本要求',
          alert: {
            type: 'warning',
            message: '注意：要求 Halo server 版本大于 v1.1.3！你可以在 【系统 | 关于】 里面找到系统的版本信息。'
          }
        }
      ]
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
        this.getBackups()
      }
    }
  },
  methods: {
    getBackups() {
      this.loading = true
      backupApi
        .listHaloBackups()
        .then(response => {
          this.backups = response.data.data
        })
        .finally(() => (this.loading = false))
    },
    handleBackupClick() {
      this.backuping = true
      backupApi
        .backupHalo()
        .then(response => {
          this.$notification.success({ message: '备份成功！' })
          this.getBackups()
        })
        .finally(() => {
          this.backuping = false
        })
    },
    handleBackupDeleteClick(filename) {
      this.deleting = true
      backupApi
        .deleteHaloBackup(filename)
        .then(response => {
          this.$notification.success({ message: '删除成功！' })
          this.getBackups()
        })
        .finally(() => (this.deleting = false))
    },
    handleBAckupRefreshClick() {
      this.getBackups()
    },
    onClose() {
      this.$emit('close', false)
    }
  }
}
</script>
