<template>
  <div>
    <a-form-model ref="otherOptionsForm" :model="options" :rules="rules" layout="vertical" :wrapperCol="wrapperCol">
      <a-form-model-item label="自定义全局 head：">
        <a-input
          type="textarea"
          :autoSize="{ minRows: 5 }"
          v-model="options.blog_custom_head"
          placeholder="放置于每个页面的 <head></head> 标签中"
        />
      </a-form-model-item>
      <a-form-model-item label="自定义内容页 head：">
        <a-input
          type="textarea"
          :autoSize="{ minRows: 5 }"
          v-model="options.blog_custom_content_head"
          placeholder="仅放置于内容页面的 <head></head> 标签中"
        />
      </a-form-model-item>
      <a-form-model-item label="统计代码：">
        <a-input
          type="textarea"
          :autoSize="{ minRows: 5 }"
          v-model="options.blog_statistics_code"
          placeholder="第三方网站统计的代码，如：Google Analytics、百度统计、CNZZ 等"
        />
      </a-form-model-item>
      <a-form-model-item>
        <ReactiveButton
          type="primary"
          @click="handleSaveOptions"
          @callback="$emit('callback')"
          :loading="saving"
          :errored="errored"
          text="保存"
          loadedText="保存成功"
          erroredText="保存失败"
        ></ReactiveButton>
      </a-form-model-item>
    </a-form-model>
  </div>
</template>
<script>
export default {
  name: 'OtherTab',
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
      _this.$refs.otherOptionsForm.validate(valid => {
        if (valid) {
          this.$emit('onSave')
        }
      })
    }
  }
}
</script>
