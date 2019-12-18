<template>
  <div class="option-tab-wrapper">
    <a-card
      :bordered="false"
      :bodyStyle="{ padding: 0 }"
    >
      <div class="table-operator">
        <a-button
          type="primary"
          icon="cloud-upload"
          @click="() => (uploadVisible = true)"
        >上传</a-button>
        <a-button
          icon="plus"
          @click="() => (createFolderModal = true)"
        >
          新建文件夹
        </a-button>
        <a-button
          icon="sync"
          @click="loadStaticList"
          :loading="loading"
          :disabled="loading"
        >
          刷新
        </a-button>
      </div>
      <div style="margin-top:15px">
        <a-table
          :rowKey="record => record.name"
          :columns="columns"
          :dataSource="statics"
          :pagination="false"
          size="middle"
          :loading="loading"
        >
          <span
            slot="createTime"
            slot-scope="createTime"
          >
            {{ createTime | moment }}
          </span>
          <span
            slot="action"
            slot-scope="text, record"
          >
            <a
              href="javascript:;"
              v-if="!record.isFile"
              @click="handleUpload(record)"
            >上传</a>
            <a
              :href="options.blog_url+record.relativePath"
              target="_blank"
              v-else
            >访问</a>
            <a-divider type="vertical" />
            <a-dropdown :trigger="['click']">
              <a
                href="javascript:void(0);"
                class="ant-dropdown-link"
              >更多</a>
              <a-menu slot="overlay">
                <a-menu-item
                  key="1"
                  v-if="!record.isFile"
                >
                  <a
                    href="javascript:;"
                    @click="handleShowCreateFolderModal(record)"
                  >创建文件夹</a>
                </a-menu-item>
                <a-menu-item key="2">
                  <a-popconfirm
                    :title="record.isFile?'你确定要删除该文件？':'你确定要删除该文件夹？'"
                    okText="确定"
                    cancelText="取消"
                    @confirm="handleDelete(record.relativePath)"
                  >
                    <a href="javascript:;">删除</a>
                  </a-popconfirm>
                </a-menu-item>
              </a-menu>
            </a-dropdown>
          </span>
        </a-table>
      </div>
    </a-card>
    <a-modal
      title="上传文件"
      v-model="uploadVisible"
      :footer="null"
      :afterClose="onUploadClose"
      destroyOnClose
    >
      <FilePondUpload
        ref="upload"
        name="file"
        :uploadHandler="uploadHandler"
        :filed="selectedFile.relativePath"
      ></FilePondUpload>
    </a-modal>
    <a-modal
      v-model="createFolderModal"
      :afterClose="onCreateFolderClose"
      title="创建文件夹"
    >
      <template slot="footer">
        <a-button
          key="submit"
          type="primary"
          @click="handleCreateFolder()"
        >创建</a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item label="文件夹名：">
          <a-input
            v-model="createFolderName"
            @keyup.enter="handleCreateFolder"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script>
import { mapGetters } from 'vuex'
import staticApi from '@/api/static'
const columns = [
  {
    title: '文件名',
    dataIndex: 'name',
    scopedSlots: { customRender: 'key' }
  },
  {
    title: '文件类型',
    dataIndex: 'mediaType',
    scopedSlots: { customRender: 'mediaType' }
  },
  {
    title: '上传时间',
    dataIndex: 'createTime',
    width: '200px',
    scopedSlots: { customRender: 'createTime' }
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '120px',
    scopedSlots: { customRender: 'action' }
  }
]
export default {
  name: 'StaticStorage',
  data() {
    return {
      columns: columns,
      statics: [],
      loading: false,
      uploadHandler: staticApi.upload,
      uploadVisible: false,
      selectedFile: {},
      createFolderModal: false,
      createFolderName: ''
    }
  },
  created() {
    this.loadStaticList()
  },
  computed: {
    ...mapGetters(['options'])
  },
  methods: {
    loadStaticList() {
      this.loading = true
      staticApi.list().then(response => {
        this.statics = response.data.data
        this.loading = false
      })
    },
    handleDelete(path) {
      staticApi.delete(path).then(response => {
        this.$message.success(`删除成功！`)
        this.loadStaticList()
      })
    },
    handleUpload(file) {
      this.selectedFile = file
      this.uploadVisible = true
    },
    handleShowCreateFolderModal(file) {
      this.selectedFile = file
      this.createFolderModal = true
    },
    handleCreateFolder() {
      staticApi.createFolder(this.selectedFile.relativePath, this.createFolderName).then(response => {
        this.$message.success(`创建文件夹成功！`)
        this.createFolderModal = false
        this.loadStaticList()
      })
    },
    onCreateFolderClose() {
      this.selectedFile = {}
      this.createFolderName = ''
    },
    onUploadClose() {
      this.$refs.upload.handleClearFileList()
      this.selectedFile = {}
      this.loadStaticList()
    }
  }
}
</script>
