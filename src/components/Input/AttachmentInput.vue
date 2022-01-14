<template>
  <div>
    <a-input :defaultValue="defaultValue" :placeholder="placeholder" :value="value" @change="onInputChange">
      <a slot="addonAfter" href="javascript:void(0);" @click="attachmentDrawerVisible = true">
        <a-icon type="picture" />
      </a>
    </a-input>
    <AttachmentSelectDrawer
      v-model="attachmentDrawerVisible"
      title="选择附件"
      @listenToSelect="handleSelectAttachment"
    />
  </div>
</template>
<script>
export default {
  name: 'AttachmentInput',
  props: {
    value: {
      type: String,
      default: ''
    },
    defaultValue: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      attachmentDrawerVisible: false
    }
  },
  methods: {
    onInputChange(e) {
      this.$emit('input', e.target.value)
    },
    handleSelectAttachment(data) {
      this.$emit('input', encodeURI(data.path))
      this.attachmentDrawerVisible = false
    }
  }
}
</script>
