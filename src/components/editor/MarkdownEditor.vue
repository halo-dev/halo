<template>
  <div>
    <halo-editor
      ref="md"
      v-model="originalContentData"
      :boxShadow="false"
      :toolbars="toolbars"
      :ishljs="true"
      :autofocus="false"
      @imgAdd="handleAttachmentUpload"
      @save="handleSaveDraft"
    />
  </div>
</template>
<script>
import { toolbars } from '@/core/const'
import { haloEditor } from 'halo-editor'
import 'halo-editor/dist/css/index.css'
import attachmentApi from '@/api/attachment'
export default {
  name: 'MarkdownEditor',
  components: {
    haloEditor
  },
  props: {
    originalContent: {
      type: String,
      required: false,
      default: ''
    }
  },
  data() {
    return {
      toolbars,
      originalContentData: ''
    }
  },
  watch: {
    originalContent(val) {
      this.originalContentData = val
    },
    originalContentData(val) {
      this.$emit('onContentChange', val)
    }
  },
  methods: {
    handleAttachmentUpload(pos, $file) {
      var formdata = new FormData()
      formdata.append('file', $file)
      attachmentApi.upload(formdata).then(response => {
        var responseObject = response.data

        if (responseObject.status === 200) {
          var HaloEditor = this.$refs.md
          HaloEditor.$img2Url(pos, encodeURI(responseObject.data.path))
          this.$message.success('图片上传成功！')
        } else {
          this.$message.error('图片上传失败：' + responseObject.message)
        }
      })
    },
    handleSaveDraft() {
      this.$emit('onSaveDraft')
    }
  }
}
</script>
