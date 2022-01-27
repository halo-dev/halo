<template>
  <div>
    <a-input :defaultValue="defaultValue" :placeholder="placeholder" :value="value" @change="onInputChange">
      <template #addonAfter>
        <a-button class="!p-0 !h-auto" type="link" @click="attachmentModalVisible = true">
          <a-icon type="picture" />
        </a-button>
      </template>
    </a-input>
    <AttachmentSelectModal
      :multiSelect="false"
      :visible.sync="attachmentModalVisible"
      @confirm="handleSelectAttachment"
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
    },
    title: {
      type: String,
      default: '选择附件'
    }
  },
  data() {
    return {
      attachmentModalVisible: false
    }
  },
  methods: {
    onInputChange(e) {
      this.$emit('input', e.target.value)
    },
    handleSelectAttachment({ raw }) {
      if (raw.length) {
        this.$emit('input', encodeURI(raw[0].path))
      }
    }
  }
}
</script>
