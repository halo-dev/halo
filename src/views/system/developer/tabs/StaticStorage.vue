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
          @click="handleShowCreateFolderModal({})"
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
          :rowKey="record => record.id"
          :columns="columns"
          :dataSource="sortedStatics"
          :pagination="false"
          size="middle"
          :loading="loading"
        >
          <span
            slot="name"
            slot-scope="name"
          >
            <ellipsis
              :length="64"
              tooltip
            >
              {{ name }}
            </ellipsis>
          </span>
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
                <a-menu-item
                  key="3"
                >
                  <a
                    href="javascript:;"
                    @click="handleShowRenameModal(record)"
                  >重命名</a>
                </a-menu-item>
                <a-menu-item
                  key="4"
                  v-if="record.isFile"
                >
                  <a
                    href="javascript:;"
                    @click="handleShowEditModal(record)"
                  >编辑</a>
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
            ref="createFoldeInput"
            v-model="createFolderName"
            @keyup.enter="handleCreateFolder"
          />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal
      v-model="renameModal"
      :afterClose="onRenameClose"
      title="重命名"
    >
      <template slot="footer">
        <a-button
          key="submit"
          type="primary"
          @click="handleRename()"
        >重命名</a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item :label="renameFile?'文件名：':'文件夹名：'">
          <a-input
            ref="renameModalInput"
            v-model="renameName"
            @keyup.enter="handleRename"
          />
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal
      v-model="editModal"
      title="编辑文件"
      width="80%"
      style="max-width: 1000px"
      :maskClosable="false"
      :keyboard="false"
      :closable="false"
    >
      <template slot="footer">
        <a-popconfirm
          title="未保存的内容将会丢失，确定要退出吗？"
          okText="确定"
          cancelText="取消"
          @confirm="handleEditClose"
        >
          <a-button>取消</a-button>
        </a-popconfirm>
        <a-button
          key="submit"
          type="primary"
          @click="handleEditSave()"
        >保存</a-button>
      </template>
      <a-form layout="vertical">
        <a-form-item>
          <codemirror
            ref="editor"
            :value="editContent"
            :options="codemirrorOptions"
          ></codemirror>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script>
import Vue from 'vue'
import { mapGetters } from 'vuex'
import staticApi from '@/api/static'
import { codemirror } from 'vue-codemirror-lite'
const context = require.context('codemirror/mode', true, /\.js$/)
context.keys().map(context)
const columns = [
  {
    title: '文件名',
    dataIndex: 'name',
    scopedSlots: { customRender: 'name' }
  },
  {
    title: '文件类型',
    dataIndex: 'mimeType',
    scopedSlots: { customRender: 'mimeType' }
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
  components: {
    codemirror
  },
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
      createFolderName: '',
      renameModal: false,
      renameName: '',
      renameFile: false,
      codemirrorOptions: {
        tabSize: 4,
        lineNumbers: true,
        line: true
      },
      editModal: false,
      editContent: '',
      CodeMirror: null
    }
  },
  created() {
    this.loadStaticList()
    this.CodeMirror = require('codemirror')
    this.CodeMirror.modeURL = 'codemirror/mode/%N/%N.js'
  },
  computed: {
    ...mapGetters(['options']),
    sortedStatics() {
      const data = this.statics.slice(0)
      return data.sort(function(a, b) {
        return b.createTime - a.createTime
      })
    }
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
      const that = this
      Vue.nextTick()
        .then(() => {
          that.$refs.createFoldeInput.focus()
        })
    },
    handleShowRenameModal(file) {
      this.selectedFile = file
      this.renameName = file.name
      this.renameFile = file.isFile
      this.renameModal = true
      const that = this
      Vue.nextTick()
        .then(() => {
          const inputRef = that.$refs.renameModalInput
          const tmp = inputRef.value.split('.')
          inputRef.focus()
          if (tmp.length <= 1) {
            inputRef.$el.setSelectionRange(0, inputRef.value.length)
          } else {
            inputRef.$el.setSelectionRange(0, inputRef.value.length - tmp.pop().length - 1)
          }
        })
    },
    handleShowEditModal(file) {
      this.selectedFile = file
      const arr = file.name.split('.')
      const postfix = arr[arr.length - 1]
      staticApi.getContent(this.options.blog_url + file.relativePath).then(response => {
        this.editContent = response.data
        const info = this.CodeMirror.findModeByExtension(postfix)
        if (info === undefined) {
          this.$message.error(`不支持编辑 "${postfix}" 类型的文件`)
        } else {
          this.editModal = true
          Vue.nextTick()
            .then(() => {
              const editor = this.$refs.editor.editor
              editor.setOption('mode', info.mime)
              this.CodeMirror.autoLoadMode(editor, info.mode)
            })
        }
      })
    },
    handleCreateFolder() {
      staticApi.createFolder(this.selectedFile.relativePath, this.createFolderName).then(response => {
        this.$message.success(`创建文件夹成功！`)
        this.createFolderModal = false
        this.loadStaticList()
      })
    },
    handleRename() {
      staticApi.rename(this.selectedFile.relativePath, this.renameName).then(response => {
        this.$message.success(`重命名成功！`)
        this.renameModal = false
        this.loadStaticList()
      })
    },
    handleEditSave() {
      staticApi.save(this.selectedFile.relativePath, this.$refs.editor.editor.getValue()).then(response => {
        this.$message.success(`文件保存成功！`)
        this.editModal = false
      })
    },
    onCreateFolderClose() {
      this.selectedFile = {}
      this.createFolderName = ''
    },
    onRenameClose() {
      this.selectedFile = {}
      this.renameName = ''
    },
    onUploadClose() {
      this.$refs.upload.handleClearFileList()
      this.selectedFile = {}
      this.loadStaticList()
    },
    handleEditClose() {
      this.editModal = false
      this.selectedFile = {}
      this.editContent = ''
    }
  }
}
</script>
