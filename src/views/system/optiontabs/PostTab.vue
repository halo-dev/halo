<template>
  <div>
    <a-form
      layout="vertical"
      :wrapperCol="wrapperCol"
    >
      <!-- <a-form-item label="默认编辑器：">
        <a-select v-model="options.default_editor">
          <a-select-option value="MARKDOWN">Markdown 编辑器</a-select-option>
          <a-select-option value="RICHTEXT">富文本编辑器</a-select-option>
        </a-select>
      </a-form-item> -->
      <a-form-item label="首页文章排序：">
        <a-select v-model="options.post_index_sort">
          <a-select-option value="createTime">创建时间</a-select-option>
          <a-select-option value="editTime">最后编辑时间</a-select-option>
          <a-select-option value="visits">点击量</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="首页每页条数：">
        <a-input
          type="number"
          v-model="options.post_index_page_size"
        />
      </a-form-item>
      <a-form-item label="归档每页条数：">
        <a-input
          type="number"
          v-model="options.post_archives_page_size"
        />
      </a-form-item>
      <a-form-item label="RSS 内容类型：">
        <a-select v-model="options.rss_content_type">
          <a-select-option value="full">全文</a-select-option>
          <a-select-option value="summary">摘要</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="RSS 内容条数：">
        <a-input
          type="number"
          v-model="options.rss_page_size"
        />
      </a-form-item>
      <a-form-item label="文章摘要字数：">
        <a-input
          type="number"
          v-model="options.post_summary_length"
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
  name: 'PostTab',
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
