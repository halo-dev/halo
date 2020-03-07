<template>
  <div>
    <a-form
      layout="vertical"
      :wrapperCol="wrapperCol"
    >
      <a-form-item label="文章固定链接类型：">
        <template slot="help">
          <span v-if="options.post_permalink_type === 'DEFAULT'">{{ options.blog_url }}/{{ options.archives_prefix }}/${slug}{{ options.path_suffix }}</span>
          <span v-else-if="options.post_permalink_type === 'DATE'">{{ options.blog_url }}{{ new Date() | moment_post_date }}${slug}{{ options.path_suffix }}</span>
          <span v-else-if="options.post_permalink_type === 'DAY'">{{ options.blog_url }}{{ new Date() | moment_post_day }}${slug}{{ options.path_suffix }}</span>
          <span v-else-if="options.post_permalink_type === 'ID'">{{ options.blog_url }}/?p=${id}</span>
        </template>
        <a-select v-model="options.post_permalink_type">
          <a-select-option
            v-for="item in Object.keys(permalinkType)"
            :key="item"
            :value="item"
          >{{ permalinkType[item].text }}</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="自定义页面前缀：">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.sheet_prefix }}/${slug}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.sheet_prefix" />
      </a-form-item>
      <a-form-item label="友情链接页面前缀：">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.links_prefix }}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.links_prefix" />
      </a-form-item>
      <a-form-item label="图库页面前缀：">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.photos_prefix }}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.photos_prefix" />
      </a-form-item>
      <a-form-item label="日志页面前缀：">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.journals_prefix }}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.journals_prefix" />
      </a-form-item>
      <a-form-item label="归档前缀：">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.archives_prefix }}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.archives_prefix" />
      </a-form-item>
      <a-form-item label="分类前缀：">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.categories_prefix }}/${slug}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.categories_prefix" />
      </a-form-item>
      <a-form-item label="标签前缀：">
        <template slot="help">
          <span>{{ options.blog_url }}/{{ options.tags_prefix }}/${slug}{{ options.path_suffix }}</span>
        </template>
        <a-input v-model="options.tags_prefix" />
      </a-form-item>
      <a-form-item label="路径后缀：">
        <template slot="help">
          <span>* 格式为：<code>.${suffix}</code>，仅对内建路径有效</span>
        </template>
        <a-input v-model="options.path_suffix" />
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
import postApi from '@/api/post'
export default {
  name: 'PermalinkTab',
  props: {
    options: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      permalinkType: postApi.permalinkType,
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
