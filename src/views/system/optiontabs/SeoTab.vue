<template>
  <div>
    <a-form
      layout="vertical"
      :wrapperCol="wrapperCol"
    >
      <a-form-item label="屏蔽搜索引擎：">
        <a-switch v-model="options.seo_spider_disabled" />
      </a-form-item>
      <a-form-item label="关键词：">
        <a-input
          v-model="options.seo_keywords"
          placeholder="多个关键词以英文状态下的逗号隔开"
        />
      </a-form-item>
      <a-form-item label="博客描述：">
        <a-input
          type="textarea"
          :autoSize="{ minRows: 5 }"
          v-model="options.seo_description"
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
  name: 'SeoTab',
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
