<template>
  <div>
    <a-form-model
      ref="postOptionsForm"
      :model="options"
      :rules="rules"
      layout="vertical"
      :wrapperCol="wrapperCol"
    >
      <!-- <a-form-model-item label="默认编辑器：">
        <a-select v-model="options.default_editor">
          <a-select-option value="MARKDOWN">Markdown 编辑器</a-select-option>
          <a-select-option value="RICHTEXT">富文本编辑器</a-select-option>
        </a-select>
      </a-form-model-item> -->
      <a-form-model-item label="首页文章排序：">
        <a-select v-model="options.post_index_sort">
          <a-select-option value="createTime">创建时间</a-select-option>
          <a-select-option value="editTime">最后编辑时间</a-select-option>
          <a-select-option value="visits">点击量</a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="首页每页条数：">
        <a-input-number
          v-model="options.post_index_page_size"
          :min="1"
          style="width:100%"
        />
      </a-form-model-item>
      <a-form-model-item label="归档每页条数：">
        <a-input-number
          v-model="options.post_archives_page_size"
          :min="1"
          style="width:100%"
        />
      </a-form-model-item>
      <a-form-model-item label="RSS 内容类型：">
        <a-select v-model="options.rss_content_type">
          <a-select-option value="full">全文</a-select-option>
          <a-select-option value="summary">摘要</a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="RSS 内容条数：">
        <a-input-number
          v-model="options.rss_page_size"
          :min="1"
          style="width:100%"
        />
      </a-form-model-item>
      <a-form-model-item label="文章摘要字数：">
        <a-input-number
          v-model="options.post_summary_length"
          style="width:100%"
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
  name: 'PostTab',
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
      _this.$refs.postOptionsForm.validate(valid => {
        if (valid) {
          this.$emit('onSave')
        }
      })
    }
  }
}
</script>
