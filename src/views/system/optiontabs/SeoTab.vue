<template>
  <div>
    <a-form-model ref="seoOptionsForm" :model="options" :rules="rules" layout="vertical" :wrapperCol="wrapperCol">
      <a-form-model-item label="屏蔽搜索引擎：" prop="seo_spider_disabled">
        <a-switch v-model="options.seo_spider_disabled" />
      </a-form-model-item>
      <a-form-model-item label="关键词：" prop="seo_keywords">
        <a-input v-model="options.seo_keywords" placeholder="多个关键词以英文状态下的逗号隔开" />
      </a-form-model-item>
      <a-form-model-item label="博客描述：" prop="seo_description">
        <a-input type="textarea" :autoSize="{ minRows: 5 }" v-model="options.seo_description" />
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
  name: 'SeoTab',
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
      rules: {
        seo_keywords: [{ max: 1023, message: '* 字符数不能超过 1023', trigger: ['change'] }],
        seo_description: [{ max: 1023, message: '* 字符数不能超过 1023', trigger: ['change'] }]
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
      const _this = this
      _this.$refs.seoOptionsForm.validate(valid => {
        if (valid) {
          this.$emit('onSave')
        }
      })
    }
  }
}
</script>
