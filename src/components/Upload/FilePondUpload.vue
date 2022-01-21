<template>
  <div>
    <file-pond
      ref="pond"
      :accepted-file-types="accepts"
      :allow-multiple="multiple"
      :allowImagePreview="allowImagePreview"
      :allowRevert="false"
      :files="fileList"
      :label-idle="label"
      :maxFiles="maxFiles"
      :maxParallelUploads="maxParallelUploads"
      :name="name"
      :server="server"
      fileValidateTypeLabelExpectedTypes="请选择 {lastType} 格式的文件"
      labelFileProcessing="上传中"
      labelFileProcessingAborted="取消上传"
      labelFileProcessingComplete="上传完成"
      labelFileProcessingError="上传错误"
      labelFileTypeNotAllowed="不支持当前文件格式"
      labelTapToCancel="点击取消"
      labelTapToRetry="点击重试"
      @init="handleFilePondInit"
    >
    </file-pond>
  </div>
</template>
<script>
import { mapGetters } from 'vuex'
import { Axios } from '@halo-dev/admin-api'

import vueFilePond from 'vue-filepond'
import 'filepond/dist/filepond.min.css'

// Plugins
import FilePondPluginImagePreview from 'filepond-plugin-image-preview'
import 'filepond-plugin-image-preview/dist/filepond-plugin-image-preview.min.css'
import FilePondPluginFileValidateType from 'filepond-plugin-file-validate-type'

// Create component and register plugins
const FilePond = vueFilePond(FilePondPluginImagePreview, FilePondPluginFileValidateType)
export default {
  name: 'FilePondUpload',
  components: {
    FilePond
  },
  props: {
    name: {
      type: String,
      required: false,
      default: 'file'
    },
    field: {
      type: String,
      required: false,
      default: ''
    },
    multiple: {
      type: Boolean,
      required: false,
      default: true
    },
    accepts: {
      type: Array,
      required: false,
      default: () => {
        return null
      }
    },
    label: {
      type: String,
      required: false,
      default: '点击选择文件或将文件拖拽到此处'
    },
    uploadHandler: {
      type: Function,
      required: true
    }
  },
  computed: {
    ...mapGetters(['options']),
    maxParallelUploads() {
      if (this.options) {
        return this.options.attachment_upload_max_parallel_uploads
      }
      return 1
    },
    allowImagePreview() {
      if (this.options) {
        return this.options.attachment_upload_image_preview_enable
      }
      return false
    },
    maxFiles() {
      if (this.options) {
        return this.options.attachment_upload_max_files
      }
      return 1
    }
  },
  data: function () {
    return {
      server: {
        process: (fieldName, file, metadata, load, error, progress, abort) => {
          const CancelToken = Axios.CancelToken
          const source = CancelToken.source()
          this.uploadHandler(
            file,
            {
              onUploadProgress: progressEvent => {
                if (progressEvent.total > 0) {
                  progress(progressEvent.lengthComputable, progressEvent.loaded, progressEvent.total)
                }
              },
              cancelToken: source.token
            },
            this.field
          )
            .then(response => {
              load(response)
              this.$log.debug('Uploaded successfully', response)
              this.$emit('success', response, file)
            })
            .catch(failure => {
              this.$log.debug('Failed to upload file', failure)
              this.$emit('failure', failure, file)
              error()
            })
          return {
            abort: () => {
              abort()
              this.$log.debug('Upload operation aborted by the user')
              source.cancel('Upload operation canceled by the user.')
            }
          }
        }
      },
      fileList: []
    }
  },
  methods: {
    handleFilePondInit() {
      this.$log.debug('FilePond has initialized')
    },
    handleClearFileList() {
      this.$refs.pond.removeFiles()
    }
  }
}
</script>
