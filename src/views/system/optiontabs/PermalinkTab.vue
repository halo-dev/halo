<template>
  <div>
    <a-form-model ref="permalinkOptionsForm" :model="options" :rules="rules" layout="vertical" :wrapperCol="wrapperCol">
      <a-form-model-item label="文章固定链接类型：">
        <template slot="help">
          <span v-if="options.post_permalink_type === 'DEFAULT'"
            >{{ options.blog_url }}/{{ options.archives_prefix }}/{slug}{{ options.path_suffix }}</span
          >
          <span v-else-if="options.post_permalink_type === 'YEAR'"
            >{{ options.blog_url }}{{ new Date() | moment_post_year }}{slug}{{ options.path_suffix }}</span
          >
          <span v-else-if="options.post_permalink_type === 'DATE'"
            >{{ options.blog_url }}{{ new Date() | moment_post_date }}{slug}{{ options.path_suffix }}</span
          >
          <span v-else-if="options.post_permalink_type === 'DAY'"
            >{{ options.blog_url }}{{ new Date() | moment_post_day }}{slug}{{ options.path_suffix }}</span
          >
          <span v-else-if="options.post_permalink_type === 'ID'">{{ options.blog_url }}/?p={id}</span>
          <span v-else-if="options.post_permalink_type === 'ID_SLUG'"
            >{{ options.blog_url }}/{{ options.archives_prefix }}/{id}{{ options.path_suffix }}</span
          >
        </template>
        <a-select v-model="options.post_permalink_type">
          <a-select-option v-for="item in Object.keys(postPermalinkType)" :key="item" :value="item">{{
            postPermalinkType[item].text
          }}</a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="归档前缀：">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.archives_prefix }}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.archives_prefix" />
      </a-form-model-item>
      <a-form-model-item label="分类前缀：">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.categories_prefix }}/{slug}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.categories_prefix" />
      </a-form-model-item>
      <a-form-model-item label="标签前缀：">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.tags_prefix }}/{slug}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.tags_prefix" />
      </a-form-model-item>
      <a-form-model-item label="自定义页面固定链接类型：">
        <template slot="help">
          <span v-if="options.sheet_permalink_type === 'SECONDARY'"
            >{{ options.blog_url }}/{{ options.sheet_prefix }}/{slug}{{ options.path_suffix }}</span
          >
          <span v-else-if="options.sheet_permalink_type === 'ROOT'"
            >{{ options.blog_url }}/{slug}{{ options.path_suffix }}</span
          >
        </template>
        <a-select v-model="options.sheet_permalink_type">
          <a-select-option v-for="item in Object.keys(sheetPermalinkType)" :key="item" :value="item">{{
            sheetPermalinkType[item].text
          }}</a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="自定义页面前缀：" v-show="options.sheet_permalink_type === 'SECONDARY'">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.sheet_prefix }}/{slug}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.sheet_prefix" />
      </a-form-model-item>
      <a-form-model-item label="友情链接页面前缀：">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.links_prefix }}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.links_prefix" />
      </a-form-model-item>
      <a-form-model-item label="图库页面前缀：">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.photos_prefix }}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.photos_prefix" />
      </a-form-model-item>
      <a-form-model-item label="日志页面前缀：">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.journals_prefix }}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.journals_prefix" />
      </a-form-model-item>
      <a-form-model-item label="路径后缀：">
        <template slot="help">
          <span>* 格式为：<code>.{suffix}</code>，仅对内建路径有效</span>
        </template>
        <a-input v-model="options.path_suffix" />
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
import postApi from '@/api/post'
import sheetApi from '@/api/sheet'
export default {
  name: 'PermalinkTab',
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
      postPermalinkType: postApi.permalinkType,
      sheetPermalinkType: sheetApi.permalinkType,
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
      _this.$refs.permalinkOptionsForm.validate(valid => {
        if (valid) {
          this.$emit('onSave')
        }
      })
    }
  }
}
</script>
