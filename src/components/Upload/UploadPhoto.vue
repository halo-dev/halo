<template>
  <div class="clearfix">
    <a-upload
      :name="name"
	  :customRequest="handleUpload"
      listType="picture-card"
      :fileList="fileList"
      @preview="handlePreview"
      @change="handleChange"
    >
      <div v-if="fileList.length < 9" id="plus-photo-uploadbox">
        <a-icon type="plus" />
        <div class="ant-upload-text">Upload</div>
      </div>
    </a-upload>
    <a-modal :visible="previewVisible" :footer="null" @cancel="handleCancel">
      <img alt="example" style="width: 100%" :src="previewImage" />
    </a-modal>
  </div>
</template>
<script>
import axios from 'axios'
import attachmentApi from '@/api/attachment'
export default {
  data () {
    return {
	  name: 'file',
      previewVisible: false,
      previewImage: '',
      fileList: [],
	  uploadHandler: attachmentApi.upload
    }
  },
  methods: {
    handleCancel () {
      this.previewVisible = false
    },
    handlePreview (file) {
      this.previewImage = file.url || file.thumbUrl
      this.previewVisible = true
    },
    handleChange ({ fileList }) {
      this.fileList = fileList
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
  },
}
</script>
<style>
  /* you can make up upload button and sample style by using stylesheets */
  .ant-upload-select-picture-card i {
    font-size: 32px;
    color: #999;
  }

  .ant-upload-select-picture-card .ant-upload-text {
    margin-top: 8px;
    color: #666;
  }
	.ant-upload-list-picture-card {
		/* 将浮动恢复为默认值,避免出现纵向换行情况 */
    float: initial;
	}
</style>