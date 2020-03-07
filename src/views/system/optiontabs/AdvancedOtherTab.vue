<template>
  <div>
    <a-form
      layout="vertical"
      :wrapperCol="wrapperCol"
    >
      <a-form-item
        label="全局绝对路径："
        help="* 对网站上面的所有页面路径、本地附件路径、以及主题中的静态资源路径有效。"
      >
        <a-switch v-model="options.global_absolute_path_enabled" />
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
  name: 'AdvancedOtherTab',
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
      this.$emit('onSave')
    }
  }
}
</script>
