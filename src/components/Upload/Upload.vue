<template>
  <div>
    <a-upload-dragger
      v-if="draggable"
      :name="name"
      :multiple="multiple"
      :accept="accept"
      :customRequest="handleUpload"
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
      :multiple="multiple"
      :accept="accept"
      :customRequest="handleUpload"
      @change="handleChange"
    >
      <slot />
    </a-upload>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Upload',
  props: {
    name: {
      type: String,
      required: true
    },
    multiple: {
      type: Boolean,
      required: false,
      default: false
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
    handleUpload(option) {
      this.$log.debug('Uploading option', option)
      const CancelToken = axios.CancelToken
      const source = CancelToken.source()

      const data = new FormData()
      data.append('file', option.file)

      this.uploadHandler(
        data,
        progressEvent => {
          if (progressEvent.total > 0) {
            progressEvent.percent = (progressEvent.loaded / progressEvent.total) * 100
          }
          this.$log.debug('Uploading percent: ', progressEvent.percent)
          option.onProgress(progressEvent)
        },
        source.token
      )
        .then(response => {
          option.onSuccess(response, option.file)
          this.$emit('success', response, option.file)
        })
        .catch(error => {
          option.onError(error, error.response)
          this.$emit('failure', error, option.file)
        })
      return {
        abort: () => {
          source.cancel('Upload operation canceled by the user.')
        }
      }
    }
  }
}
</script>

<style>
</style>
