<template>
  <a-modal
    v-model="modalVisible"
    :afterClose="onModalClose"
    :bodyStyle="{ padding: '0 24px 24px' }"
    :footer="null"
    destroyOnClose
    title="安装主题"
  >
    <div class="custom-tab-wrapper">
      <a-tabs :animated="{ inkBar: true, tabPane: false }">
        <a-tab-pane key="1" tab="本地上传">
          <FilePondUpload
            ref="upload"
            :accepts="['application/x-zip', 'application/x-zip-compressed', 'application/zip']"
            :uploadHandler="local.uploadHandler"
            label="点击选择主题包或将主题包拖拽到此处<br>仅支持 ZIP 格式的文件"
            name="file"
            @success="onUploadSucceed"
          ></FilePondUpload>
          <div class="mt-5">
            <a-alert closable type="info">
              <template slot="message">
                更多主题请访问：
                <a href="https://halo.run/themes.html" target="_blank">https://halo.run/themes</a>
              </template>
            </a-alert>
          </div>
        </a-tab-pane>
        <a-tab-pane key="2" tab="远程下载">
          <a-form-model ref="remoteInstallForm" :model="remote" :rules="remote.rules" layout="vertical">
            <a-form-model-item help="* 支持 Git 仓库地址，ZIP 链接。" label="远程地址：" prop="url">
              <a-input v-model="remote.url" />
            </a-form-model-item>
            <a-form-model-item>
              <ReactiveButton
                :errored="remote.fetchErrored"
                :loading="remote.fetching"
                erroredText="下载失败"
                loadedText="下载成功"
                text="下载"
                type="primary"
                @callback="handleRemoteFetchCallback"
                @click="handleRemoteFetching"
              ></ReactiveButton>
            </a-form-model-item>
          </a-form-model>
          <div class="mt-5">
            <a-alert closable type="info">
              <template slot="message">
                目前仅支持远程 Git 仓库和 ZIP 下载链接。更多主题请访问：
                <a href="https://halo.run/themes.html" target="_blank">https://halo.run/themes</a>
              </template>
            </a-alert>
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>
  </a-modal>
</template>
<script>
import apiClient from '@/utils/api-client'
export default {
  name: 'ThemeInstallModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      local: {
        uploadHandler: (file, options) => apiClient.theme.upload(file, options)
      },

      remote: {
        url: null,

        fetching: false,
        fetchErrored: false,

        rules: {
          url: [{ required: true, message: '* 远程地址不能为空', trigger: ['change'] }]
        }
      }
    }
  },
  computed: {
    modalVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    }
  },
  methods: {
    onModalClose() {
      this.$refs.upload.handleClearFileList()
      this.remote.url = null
      this.$emit('onAfterClose')
    },
    onUploadSucceed() {
      this.modalVisible = false
      this.$emit('upload-succeed')
    },
    handleRemoteFetching() {
      this.$refs.remoteInstallForm.validate(async valid => {
        if (valid) {
          try {
            this.remote.fetching = true
            await apiClient.theme.fetchTheme(this.remote.url)
          } catch (e) {
            this.remote.fetchErrored = true
            this.$log.error('Fetch remote theme failed: ', e)
          } finally {
            setTimeout(() => {
              this.remote.fetching = false
            }, 400)
          }
        }
      })
    },
    handleRemoteFetchCallback() {
      if (this.remote.fetchErrored) {
        this.remote.fetchErrored = false
      } else {
        this.modalVisible = false
      }
    }
  }
}
</script>
