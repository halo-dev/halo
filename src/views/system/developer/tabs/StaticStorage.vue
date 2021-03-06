<template>
  <div class="option-tab-wrapper">
    <a-card :bordered="false" :bodyStyle="{ padding: 0 }">
      <div class="table-operator">
        <a-button type="primary" icon="cloud-upload" @click="uploadModal.visible = true">上传</a-button>
        <a-button icon="plus" @click="handleOpenCreateDirectoryModal({})">
          新建文件夹
        </a-button>
        <a-button icon="sync" @click="handleListStatics" :loading="list.loading">
          刷新
        </a-button>
      </div>
      <div class="mt-4">
        <a-table
          :rowKey="record => record.id"
          :columns="list.columns"
          :dataSource="sortedStatics"
          :pagination="false"
          size="middle"
          :loading="list.loading"
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
            <a href="javascript:void(0);" v-if="!record.isFile" @click="handleUpload(record)">上传</a>
            <a :href="options.blog_url + record.relativePath" target="_blank" v-else>访问</a>
            <a-divider type="vertical" />
            <a-dropdown :trigger="['click']">
              <a href="javascript:void(0);" class="ant-dropdown-link">更多</a>
              <a-menu slot="overlay">
                <a-menu-item key="1" v-if="!record.isFile">
                  <a href="javascript:void(0);" @click="handleOpenCreateDirectoryModal(record)">创建文件夹</a>
                </a-menu-item>
                <a-menu-item key="2">
                  <a-popconfirm
                    :title="record.isFile ? '你确定要删除该文件？' : '你确定要删除该文件夹？'"
                    okText="确定"
                    cancelText="取消"
                    @confirm="handleDelete(record.relativePath)"
                  >
                    <a href="javascript:void(0);">删除</a>
                  </a-popconfirm>
                </a-menu-item>
                <a-menu-item key="3">
                  <a href="javascript:void(0);" @click="handleOpenRenameModal(record)">重命名</a>
                </a-menu-item>
                <a-menu-item key="4" v-if="record.isFile">
                  <a href="javascript:void(0);" @click="handleOpenEditContentModal(record)">编辑</a>
                </a-menu-item>
              </a-menu>
            </a-dropdown>
          </span>
        </a-table>
      </div>
    </a-card>
    <a-modal
      title="上传文件"
      v-model="uploadModal.visible"
      :footer="null"
      :afterClose="onUploadModalClose"
      destroyOnClose
    >
      <FilePondUpload
        ref="upload"
        name="file"
        :uploadHandler="uploadModal.uploadHandler"
        :filed="list.selected.relativePath"
      ></FilePondUpload>
    </a-modal>
    <a-modal v-model="directoryForm.visible" :afterClose="onDirectoryFormModalClose" title="创建文件夹">
      <template slot="footer">
        <ReactiveButton
          @click="handleCreateDirectory"
          @callback="handleCreateDirectoryCallback"
          :loading="directoryForm.saving"
          :errored="directoryForm.saveErrored"
          text="创建"
          loadedText="创建成功"
          erroredText="创建失败"
        ></ReactiveButton>
      </template>
      <a-form-model ref="directoryForm" :model="directoryForm.model" :rules="directoryForm.rules" layout="vertical">
        <a-form-model-item prop="name" label="文件夹名：">
          <a-input ref="createDirectoryInput" v-model="directoryForm.model.name" @keyup.enter="handleCreateDirectory" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
    <a-modal v-model="renameForm.visible" :afterClose="onRenameModalClose" title="重命名">
      <template slot="footer">
        <ReactiveButton
          @click="handleRenameDirectoryOrFile"
          @callback="handleRenameDirectoryOrFileCallback"
          :loading="renameForm.saving"
          :errored="renameForm.saveErrored"
          text="重命名"
          loadedText="重命名成功"
          erroredText="重命名失败"
        ></ReactiveButton>
      </template>
      <a-form-model ref="renameForm" :model="renameForm.model" :rules="renameForm.rules" layout="vertical">
        <a-form-model-item prop="name" :label="list.selected.isFile ? '文件名：' : '文件夹名：'">
          <a-input ref="renameModalInput" v-model="renameForm.model.name" @keyup.enter="handleRenameDirectoryOrFile" />
        </a-form-model-item>
      </a-form-model>
    </a-modal>
    <a-modal
      v-model="editContentForm.visible"
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
          @confirm="handleEditContentModalClose"
        >
          <a-button>取消</a-button>
        </a-popconfirm>
        <ReactiveButton
          @click="handleContentEdit"
          @callback="handleContentEditCallback"
          :loading="editContentForm.saving"
          :errored="editContentForm.saveErrored"
          text="保存"
          loadedText="保存成功"
          erroredText="保存失败"
        ></ReactiveButton>
      </template>
      <a-form layout="vertical">
        <a-form-item>
          <codemirror
            ref="editor"
            :value="editContentForm.model.content"
            :options="editContentForm.codeMirror.options"
          ></codemirror>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script>
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
      list: {
        columns: columns,
        data: [],
        loading: false,
        selected: {}
      },

      uploadModal: {
        visible: false,
        uploadHandler: staticApi.upload
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
        saveErrored: false,
        codeMirror: {
          instance: null,
          options: {
            tabSize: 4,
            lineNumbers: true,
            line: true
          }
        }
      }
    }
  },
  beforeMount() {
    this.handleListStatics()
    this.editContentForm.codeMirror.instance = require('codemirror')
    this.editContentForm.codeMirror.instance.modeURL = 'codemirror/mode/%N/%N.js'
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
      staticApi
        .list()
        .then(response => {
          this.list.data = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.list.loading = false
          }, 200)
        })
    },
    handleDelete(path) {
      staticApi
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
      const _this = this
      _this.list.selected = file
      _this.directoryForm.visible = true
      _this.$nextTick(() => {
        _this.$refs.createDirectoryInput.focus()
      })
    },
    handleCreateDirectory() {
      const _this = this
      _this.$refs.directoryForm.validate(valid => {
        if (valid) {
          this.directoryForm.saving = true
          staticApi
            .createFolder(_this.list.selected.relativePath, _this.directoryForm.model.name)
            .catch(() => {
              _this.directoryForm.saveErrored = true
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
      const _this = this
      _this.list.selected = file
      _this.$set(_this.renameForm.model, 'name', file.name)
      _this.renameForm.visible = true
      _this.$nextTick(() => {
        const inputRef = _this.$refs.renameModalInput
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
      const _this = this
      _this.$refs.renameForm.validate(valid => {
        if (valid) {
          this.renameForm.saving = true
          staticApi
            .rename(_this.list.selected.relativePath, _this.renameForm.model.name)
            .catch(() => {
              _this.renameForm.saveErrored = true
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
      const _this = this
      _this.list.selected = file
      const arr = file.name.split('.')
      const postfix = arr[arr.length - 1]
      staticApi.getContent(_this.options.blog_url + file.relativePath).then(response => {
        _this.editContentForm.model.content = response.data
        const info = _this.editContentForm.codeMirror.instance.findModeByExtension(postfix)
        if (info === undefined) {
          _this.$message.error(`不支持编辑 "${postfix}" 类型的文件`)
        } else {
          _this.editContentForm.visible = true
          _this.$nextTick(() => {
            const editor = _this.$refs.editor.editor
            editor.setOption('mode', info.mime)
            _this.editContentForm.codeMirror.instance.autoLoadMode(editor, info.mode)
          })
        }
      })
    },
    handleContentEdit() {
      this.editContentForm.saving = true
      staticApi
        .save(this.list.selected.relativePath, this.$refs.editor.editor.getValue())
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
