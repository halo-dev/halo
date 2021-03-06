<template>
  <div>
    <a-input type="textarea" v-model="originalContent" :rows="16" />
  </div>
</template>
<script>
export default {
  name: 'RichTextEditor',
  props: {
    originalContent: {
      type: String,
      required: false,
      default: ''
    }
  },
  data() {
    return {
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
  }
}
</script>
