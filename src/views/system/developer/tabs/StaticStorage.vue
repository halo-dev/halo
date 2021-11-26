<template>
  <div class="option-tab-wrapper">
    <a-card :bodyStyle="{ padding: 0 }" :bordered="false">
      <div class="table-operator">
        <a-button icon="cloud-upload" type="primary" @click="uploadModal.visible = true">上传</a-button>
        <a-button icon="plus" @click="handleOpenCreateDirectoryModal({})">
          新建文件夹
        </a-button>
        <a-button :loading="list.loading" icon="sync" @click="handleListStatics">
          刷新
        </a-button>
      </div>
      <div class="mt-4">
        <a-table
          :columns="list.columns"
          :dataSource="sortedStatics"
          :loading="list.loading"
          :pagination="false"
          :rowKey="record => record.id"
          size="middle"
        >
          <span slot="name" slot-scope="name">
            <ellipsis :length="64" tooltip>
              {{ name }}
            </ellipsis>
          </span>
          <span slot="createTime" slot-scope="createTime">
            {{ createTime | moment }}
          </span>
          <span slot="action" slot-scope="text, record">
            <a v-if="!record.isFile" href="javascript:void(0);" @click="handleUpload(record)">上传</a>
            <a v-else :href="options.blog_url + record.relativePath" target="_blank">访问</a>
            <a-divider type="vertical" />
            <a-dropdown :trigger="['click']">
              <a class="ant-dropdown-link" href="javascript:void(0);">更多</a>
              <a-menu slot="overlay">
                <a-menu-item v-if="!record.isFile" key="1">
                  <a href="javascript:void(0);" @click="handleOpenCreateDirectoryModal(record)">创建文件夹</a>
                </a-menu-item>
                <a-menu-item key="2">
                  <a-popconfirm
                    :title="record.isFile ? '你确定要删除该文件？' : '你确定要删除该文件夹？'"
                    cancelText="取消"
                    okText="确定"
                    @confirm="handleDelete(record.relativePath)"
                  >
                    <a href="javascript:void(0);">删除</a>
                  </a-popconfirm>
                </a-menu-item>
                <a-menu-item key="3">
                  <a href="javascript:void(0);" @click="handleOpenRenameModal(record)">重命名</a>
                </a-menu-item>
                <a-menu-item v-if="record.isFile" key="4">
                  <a href="javascript:void(0);" @click="handleOpenEditContentModal(record)">编辑</a>
                </a-menu-item>
              </a-menu>
            </a-dropdown>
          </span>
        </a-table>
      </div>
    </a-card>
    <a-modal
      v-model="uploadModal.visible"
      :afterClose="onUploadModalClose"
      :footer="null"
      destroyOnClose
      title="上传文件"
    >
      <FilePondUpload
        ref="upload"
        :field="list.selected.relativePath"
        :uploadHandler="uploadModal.uploadHandler"
        name="file"
      ></FilePondUpload>
    </a-modal>
    <a-modal v-model="directoryForm.visible" :afterClose="onDirectoryFormModalClose" title="创建文件夹">
      <template slot="footer">
        <ReactiveButton
          :errored="directoryForm.saveErrored"
          :loading="directoryForm.saving"
          erroredText="创建失败"
          loadedText="创建成功"
          text="创建"
          @callback="handleCreateDirectoryCallback"
          @click="handleCreateDirectory"
        ></ReactiveButton>
      </template>
      <a-form-model ref="directoryForm" :model="directoryForm.model" :rules="directoryForm.rules" layout="vertical">
        <a-form-model-item label="文件夹名：" prop="name">
          <a-input ref="createDirectoryInput" v-model="directoryForm.model.name" @keyup.enter="handleCreateDirectory" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
    <a-modal v-model="renameForm.visible" :afterClose="onRenameModalClose" title="重命名">
      <template slot="footer">
        <ReactiveButton
          :errored="renameForm.saveErrored"
          :loading="renameForm.saving"
          erroredText="重命名失败"
          loadedText="重命名成功"
          text="重命名"
          @callback="handleRenameDirectoryOrFileCallback"
          @click="handleRenameDirectoryOrFile"
        ></ReactiveButton>
      </template>
      <a-form-model ref="renameForm" :model="renameForm.model" :rules="renameForm.rules" layout="vertical">
        <a-form-model-item :label="list.selected.isFile ? '文件名：' : '文件夹名：'" prop="name">
          <a-input ref="renameModalInput" v-model="renameForm.model.name" @keyup.enter="handleRenameDirectoryOrFile" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
    <a-modal
      v-model="editContentForm.visible"
      :closable="false"
      :keyboard="false"
      :maskClosable="false"
      style="max-width: 1000px"
      title="编辑文件"
      width="80%"
    >
      <template slot="footer">
        <a-popconfirm
          cancelText="取消"
          okText="确定"
          title="未保存的内容将会丢失，确定要退出吗？"
          @confirm="handleEditContentModalClose"
        >
          <a-button>取消</a-button>
        </a-popconfirm>
        <ReactiveButton
          :errored="editContentForm.saveErrored"
          :loading="editContentForm.saving"
          erroredText="保存失败"
          loadedText="保存成功"
          text="保存"
          @callback="handleContentEditCallback"
          @click="handleContentEdit"
        ></ReactiveButton>
      </template>
      <a-form layout="vertical">
        <a-form-item>
          <Codemirror ref="editor" v-model="editContentForm.model.content" height="600px"></Codemirror>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script>
import { mapGetters } from 'vuex'
import apiClient from '@/utils/api-client'
import Codemirror from '@/components/Codemirror/Codemirror'
import { Axios } from '@halo-dev/admin-api'

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
    Codemirror
  },
  name: 'StaticStorage',
  data() {
    return {
      list: {
        columns: columns,
        data: [],
        loading: false,
        selected: {}
      },

      uploadModal: {
        visible: false,
        uploadHandler: (file, options, field) => apiClient.staticStorage.upload(file, options, field)
      },

      directoryForm: {
        model: {
          name: null
        },
        visible: false,
        saving: false,
        saveErrored: false,
        rules: {
          name: [{ required: true, message: '* 文件夹名不能为空', trigger: ['change'] }]
        }
      },

      renameForm: {
        model: {
          name: null
        },
        visible: false,
        saving: false,
        saveErrored: false,
        rules: {
          name: [{ required: true, message: '* 文件夹名不能为空', trigger: ['change'] }]
        }
      },

      editContentForm: {
        model: {
          content: null
        },
        visible: false,
        saving: false,
        saveErrored: false
      }
    }
  },
  beforeMount() {
    this.handleListStatics()
  },
  computed: {
    ...mapGetters(['options']),
    sortedStatics() {
      const data = this.list.data.slice(0)
      return data.sort(function(a, b) {
        return a.isFile - b.isFile
      })
    }
  },
  methods: {
    handleListStatics() {
      this.list.loading = true
      apiClient.staticStorage
        .list()
        .then(response => {
          this.list.data = response.data
        })
        .finally(() => {
          this.list.loading = false
        })
    },
    handleDelete(path) {
      apiClient.staticStorage
        .delete(path)
        .then(() => {
          this.$message.success(`删除成功！`)
        })
        .finally(() => {
          this.handleListStatics()
        })
    },
    handleUpload(file) {
      this.list.selected = file
      this.uploadModal.visible = true
    },
    handleOpenCreateDirectoryModal(file) {
      this.list.selected = file
      this.directoryForm.visible = true
      this.$nextTick(() => {
        this.$refs.createDirectoryInput.focus()
      })
    },
    handleCreateDirectory() {
      this.$refs.directoryForm.validate(valid => {
        if (valid) {
          this.directoryForm.saving = true
          const basePath = this.list.selected.relativePath || '/'
          apiClient.staticStorage
            .createFolder(basePath, this.directoryForm.model.name)
            .catch(() => {
              this.directoryForm.saveErrored = true
            })
            .finally(() => {
              setTimeout(() => {
                this.directoryForm.saving = false
              }, 400)
            })
        }
      })
    },
    handleCreateDirectoryCallback() {
      if (this.directoryForm.saveErrored) {
        this.directoryForm.saveErrored = false
      } else {
        this.directoryForm.model = {}
        this.directoryForm.visible = false
        this.handleListStatics()
      }
    },
    handleOpenRenameModal(file) {
      this.list.selected = file
      this.$set(this.renameForm.model, 'name', file.name)
      this.renameForm.visible = true
      this.$nextTick(() => {
        const inputRef = this.$refs.renameModalInput
        const tmp = inputRef.value.split('.')
        inputRef.focus()
        if (tmp.length <= 1) {
          inputRef.$el.setSelectionRange(0, inputRef.value.length)
        } else {
          inputRef.$el.setSelectionRange(0, inputRef.value.length - tmp.pop().length - 1)
        }
      })
    },
    handleRenameDirectoryOrFile() {
      this.$refs.renameForm.validate(valid => {
        if (valid) {
          this.renameForm.saving = true
          apiClient.staticStorage
            .rename(this.list.selected.relativePath, this.renameForm.model.name)
            .catch(() => {
              this.renameForm.saveErrored = true
            })
            .finally(() => {
              setTimeout(() => {
                this.renameForm.saving = false
              }, 400)
            })
        }
      })
    },
    handleRenameDirectoryOrFileCallback() {
      if (this.renameForm.saveErrored) {
        this.renameForm.saveErrored = false
      } else {
        this.renameForm.model = {}
        this.renameForm.visible = false
        this.handleListStatics()
      }
    },
    handleOpenEditContentModal(file) {
      this.list.selected = file
      Axios.get(this.options.blog_url + file.relativePath).then(response => {
        this.editContentForm.model.content = response.data + ''
        this.editContentForm.visible = true
        this.$nextTick(() => {
          this.$refs.editor.handleInitCodemirror()
        })
      })
    },
    handleContentEdit() {
      this.editContentForm.saving = true
      apiClient.staticStorage
        .saveContent({
          path: this.list.selected.relativePath,
          content: this.editContentForm.model.content
        })
        .catch(() => {
          this.editContentForm.saveErrored = true
        })
        .finally(() => {
          setTimeout(() => {
            this.editContentForm.saving = false
          }, 400)
        })
    },
    handleContentEditCallback() {
      if (this.editContentForm.saveErrored) {
        this.editContentForm.saveErrored = false
      } else {
        this.editContentForm.model = {}
        this.editContentForm.visible = false
        this.handleListStatics()
      }
    },
    onDirectoryFormModalClose() {
      this.list.selected = {}
      this.$set(this.directoryForm.model, 'name', null)
    },
    onRenameModalClose() {
      this.list.selected = {}
      this.$set(this.renameForm.model, 'name', null)
    },
    onUploadModalClose() {
      this.$refs.upload.handleClearFileList()
      this.list.selected = {}
      this.handleListStatics()
    },
    handleEditContentModalClose() {
      this.editContentForm.visible = false
      this.list.selected = {}
      this.editContentForm.model.content = ''
    }
  }
}
</script>
