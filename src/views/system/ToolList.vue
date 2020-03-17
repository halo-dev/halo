<template>
  <div>
    <div class="card-content">
      <a-row :gutter="12">
        <a-col
          v-if="options.developer_mode"
          :xl="6"
          :lg="6"
          :md="12"
          :sm="24"
          :xs="24"
          :style="{ marginBottom: '12px' }"
        >
          <a-card
            :bordered="false"
            :bodyStyle="{ padding: '16px' }"
          >
            <div slot="title">
              <a-icon type="experiment" /> 开发者选项
            </div>
            <p style="min-height: 50px;">点击进入开发者选项页面</p>
            <a-button
              type="primary"
              style="float:right"
              @click="handleToDeveloperOptions()"
            >进入</a-button>
          </a-card>
        </a-col>
        <!-- <a-col
          :xl="6"
          :lg="6"
          :md="12"
          :sm="24"
          :xs="24"
          :style="{ marginBottom: '12px' }"
        >
          <a-card
            :bordered="false"
            :bodyStyle="{ padding: '16px' }"
          >
            <div slot="title">
              <a-icon type="html5" /> 静态部署
            </div>
            <p style="min-height: 50px;">生成静态页面并部署到 Github Pages 之类的托管平台</p>
            <a-button
              type="primary"
              style="float:right"
              @click="handleToStaticPagesManage"
            >管理</a-button>
          </a-card>
        </a-col> -->
        <a-col
          :xl="6"
          :lg="6"
          :md="12"
          :sm="24"
          :xs="24"
          :style="{ marginBottom: '12px' }"
        >
          <a-card
            :bordered="false"
            :bodyStyle="{ padding: '16px' }"
          >
            <div slot="title">
              <a-icon type="hdd" /> 博客备份
            </div>
            <p style="min-height: 50px;">支持备份全站数据和数据导出，支持下载到本地</p>

            <a-dropdown style="float:right">
              <a-menu slot="overlay">
                <a-menu-item
                  key="1"
                  @click="backupWorkDirDrawerVisible = true"
                >
                  整站备份
                </a-menu-item>
                <a-menu-item
                  key="2"
                  @click="exportDataDrawerVisible = true"
                >
                  数据导出
                </a-menu-item>
              </a-menu>
              <a-button style="margin-left: 8px"> 备份
                <a-icon type="down" />
              </a-button>
            </a-dropdown>
          </a-card>
        </a-col>
        <a-col
          :xl="6"
          :lg="6"
          :md="12"
          :sm="24"
          :xs="24"
          :style="{ marginBottom: '12px' }"
        >
          <a-card
            :bordered="false"
            :bodyStyle="{ padding: '16px' }"
          >
            <div slot="title">
              <a-icon type="file-markdown" /> Markdown 文章导入
            </div>
            <p style="min-height: 50px;">支持 Hexo/Jekyll 文章导入并解析元数据</p>
            <a-button
              type="primary"
              style="float:right"
              @click="handleImportMarkdown"
            >导入</a-button>
          </a-card>
        </a-col>
      </a-row>
      <a-modal
        title="Markdown 文章导入"
        v-model="markdownUpload"
        :footer="null"
        destroyOnClose
        :afterClose="onUploadClose"
      >
        <FilePondUpload
          ref="upload"
          name="file"
          accept="text/markdown"
          label="拖拽或点击选择 Markdown 文件到此处"
          :uploadHandler="uploadHandler"
        ></FilePondUpload>
      </a-modal>
      <BackupWorkDirDrawer v-model="backupWorkDirDrawerVisible"></BackupWorkDirDrawer>
      <ExportDataDrawer v-model="exportDataDrawerVisible"></ExportDataDrawer>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import backupApi from '@/api/backup'
import BackupWorkDirDrawer from './components/BackupWorkDirDrawer'
import ExportDataDrawer from './components/ExportDataDrawer'

export default {
  components: { BackupWorkDirDrawer, ExportDataDrawer },
  data() {
    return {
      backupWorkDirDrawerVisible: false,
      exportDataDrawerVisible: false,
      markdownUpload: false,
      uploadHandler: backupApi.importMarkdown
    }
  },
  computed: {
    ...mapGetters(['options'])
  },
  methods: {
    handleImportMarkdown() {
      this.markdownUpload = true
    },
    handleChange(info) {
      const status = info.file.status
      if (status !== 'uploading') {
        this.$log.debug(info.file, info.fileList)
      }
      if (status === 'done') {
        this.$message.success(`${info.file.name} 导入成功！`)
      } else if (status === 'error') {
        this.$message.error(`${info.file.name} 导入失败！`)
      }
    },
    handleToDeveloperOptions() {
      this.$router.push({ name: 'DeveloperOptions' })
    },
    // handleToStaticPagesManage() {
    //   this.$router.push({ name: 'StaticPagesManage' })
    // },
    onUploadClose() {
      this.$refs.upload.handleClearFileList()
    }
  }
}
</script>
