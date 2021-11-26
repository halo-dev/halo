<template>
  <div>
    <a-form-model ref="apiOptionsForm" :model="options" :rules="rules" :wrapperCol="wrapperCol" layout="vertical">
      <a-form-model-item label="API 服务：">
        <a-switch v-model="options.api_enabled" />
      </a-form-model-item>
      <a-form-model-item label="Access key：" prop="api_access_key">
        <a-input-password v-model="options.api_access_key" autocomplete="new-password" />
      </a-form-model-item>
      <a-form-model-item>
        <ReactiveButton
          :errored="errored"
          :loading="saving"
          erroredText="保存失败"
          loadedText="保存成功"
          text="保存"
          type="primary"
          @callback="$emit('callback')"
          @click="handleSaveOptions"
        ></ReactiveButton>
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
    },
    errored: {
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
      }
    }
  },
  watch: {
    options(val) {
      this.$emit('onChange', val)
    }
  },
  computed: {
    rules() {
      const required = this.options.api_enabled
      return {
        api_access_key: [{ required: required, message: '* Access key 不能为空', trigger: ['change'] }]
      }
    }
  },
  methods: {
    handleSaveOptions() {
      const _this = this
      _this.$refs.apiOptionsForm.validate(valid => {
        if (valid) {
          _this.$emit('onSave')
        }
      })
    }
  }
}
</script>
