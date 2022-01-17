<template>
  <div>
    <a-form-model ref="generalOptionsForm" :model="options" :rules="rules" :wrapperCol="wrapperCol" layout="vertical">
      <a-form-model-item label="博客标题：" prop="blog_title">
        <a-input v-model="options.blog_title" />
      </a-form-model-item>
      <a-form-model-item label="博客地址：" prop="blog_url">
        <a-input v-model="options.blog_url" placeholder="如：https://halo.run" />
      </a-form-model-item>
      <a-form-model-item label="Logo：" prop="blog_logo">
        <AttachmentInput v-model="options.blog_logo" title="选择 Logo" />
      </a-form-model-item>
      <a-form-model-item label="Favicon：" prop="blog_favicon">
        <AttachmentInput v-model="options.blog_favicon" title="选择 Favicon" />
      </a-form-model-item>
      <a-form-model-item label="页脚信息：" prop="blog_footer_info">
        <a-input
          v-model="options.blog_footer_info"
          :autoSize="{ minRows: 5 }"
          placeholder="支持 HTML 格式的文本"
          type="textarea"
        />
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
  name: 'GeneralTab',
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
        blog_title: [
          { required: true, message: '* 博客标题不能为空', trigger: ['change'] },
          { max: 1023, message: '* 字符数不能超过 1023', trigger: ['change'] }
        ],
        blog_url: [
          { required: true, message: '* 博客地址不能为空', trigger: ['change'] },
          { max: 1023, message: '* 字符数不能超过 1023', trigger: ['change'] }
        ],
        blog_logo: [{ max: 1023, message: '* 字符数不能超过 1023', trigger: ['change'] }],
        blog_favicon: [{ max: 1023, message: '* 字符数不能超过 1023', trigger: ['change'] }],
        blog_footer_info: [{ max: 1023, message: '* 字符数不能超过 1023', trigger: ['change'] }]
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
      _this.$refs.generalOptionsForm.validate(valid => {
        if (valid) {
          this.$emit('onSave')
        }
      })
    }
  }
}
</script>
