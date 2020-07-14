<template>
  <div>
    <a-form-model
      ref="apiOptionsForm"
      :model="options"
      :rules="rules"
      layout="vertical"
      :wrapperCol="wrapperCol"
    >
      <a-form-model-item label="API 服务：">
        <a-switch v-model="options.api_enabled" />
      </a-form-model-item>
      <a-form-model-item label="Access key：">
        <a-input-password
          v-model="options.api_access_key"
          autocomplete="new-password"
        />
      </a-form-model-item>
      <a-form-model-item>
        <a-button
          type="primary"
          @click="handleSaveOptions"
          :loading="saving"
        >保存</a-button>
      </a-form-model-item>
    </a-form-model>
  </div>
</template>
<script>
export default {
  name: 'ApiTab',
  props: {
    options: {
      type: Object,
      required: true
    },
    saving: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      wrapperCol: {
        xl: { span: 8 },
        lg: { span: 8 },
        sm: { span: 12 },
        xs: { span: 24 }
      },
      rules: {}
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
      const _this = this
      _this.$refs.apiOptionsForm.validate(valid => {
        if (valid) {
          this.$emit('onSave')
        }
      })
    }
  }
}
</script>
