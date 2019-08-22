<template>
  <div>
    <a-upload-dragger
      v-if="draggable"
      :name="name"
      :accept="accept"
      :customRequest="handleUpload"
      :remove="handleRemove"
      @change="handleChange"
    >
      <slot
        role="button"
        class="ant-upload ant-upload-btn"
      />
    </a-upload-dragger>
    <a-upload
      v-else
      :name="name"
      :accept="accept"
      :customRequest="handleUpload"
      :remove="handleRemove"
      @change="handleChange"
    >
      <slot />
    </a-upload>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'UpdateTheme',
  props: {
    name: {
      type: String,
      required: false,
      default: 'file'
    },
    themeId: {
      type: String,
      required: true
    },
    draggable: {
      type: Boolean,
      required: false,
      default: true
    },
    accept: {
      type: String,
      required: false,
      default: ''
    },
    uploadHandler: {
      type: Function,
      required: true
    }
  },
  methods: {
    handleChange(info) {
      this.$emit('change', info)
    },
    handleRemove(file) {
      this.$log.debug('Removed file', file)
      this.$emit('remove', file)
    },
    handleUpload(option) {
      this.$log.debug('Uploading option', option)
      const CancelToken = axios.CancelToken
      const source = CancelToken.source()

      const data = new FormData()
      data.append(this.name, option.file)

      this.uploadHandler(
        data,
        progressEvent => {
          if (progressEvent.total > 0) {
            progressEvent.percent = (progressEvent.loaded / progressEvent.total) * 100
          }
          this.$log.debug('Uploading percent: ', progressEvent.percent)
          option.onProgress(progressEvent)
        },
        source.token,
        this.themeId,
        option.file
      )
        .then(response => {
          this.$log.debug('Uploaded successfully', response)
          option.onSuccess(response, option.file)
          this.$emit('success', response, option.file)
        })
        .catch(error => {
          this.$log.debug('Failed to upload file', error)
          option.onError(error, error.response)
          this.$emit('failure', error, option.file)
        })
      return {
        abort: () => {
          this.$log.debug('Upload operation aborted by the user')
          source.cancel('Upload operation canceled by the user.')
        }
      }
    }
  }
}
</script>

<style>
</style>
