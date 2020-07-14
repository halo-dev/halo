<template>
  <div>
    <a-form-model
      ref="advancedOptionsForm"
      :model="options"
      :rules="rules"
      layout="vertical"
      :wrapperCol="wrapperCol"
    >
      <a-form-model-item
        label="全局绝对路径："
        help="* 对网站上面的所有页面路径、本地附件路径、以及主题中的静态资源路径有效。"
      >
        <a-switch v-model="options.global_absolute_path_enabled" />
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
  name: 'AdvancedOtherTab',
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
      const _this = this
      _this.$refs.advancedOptionsForm.validate(valid => {
        if (valid) {
          this.$emit('onSave')
        }
      })
    }
  }
}
</script>
