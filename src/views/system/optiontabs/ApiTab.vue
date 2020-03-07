<template>
  <div>
    <a-form
      layout="vertical"
      :wrapperCol="wrapperCol"
    >
      <a-form-item label="API 服务：">
        <a-switch v-model="options.api_enabled" />
      </a-form-item>
      <a-form-item label="Access key：">
        <a-input-password
          v-model="options.api_access_key"
          autocomplete="new-password"
        />
      </a-form-item>
      <a-form-item>
        <a-button
          type="primary"
          @click="handleSaveOptions"
        >保存</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script>
export default {
  name: 'ApiTab',
  props: {
    options: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      wrapperCol: {
        xl: { span: 8 },
        lg: { span: 8 },
        sm: { span: 12 },
        xs: { span: 24 }
      }
    }
  },
  watch: {
    options(val) {
      this.$emit('onChange', val)
    }
  },
  methods: {
    handleSaveOptions() {
      // API 配置验证
      if (this.options.api_enabled) {
        if (!this.options.api_access_key) {
          this.$notification['error']({
            message: '提示',
            description: 'Access key 不能为空！'
          })
          return
        }
      }
      this.$emit('onSave')
    }
  }
}
</script>
