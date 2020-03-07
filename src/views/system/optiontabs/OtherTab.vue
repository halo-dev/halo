<template>
  <div>
    <a-form
      layout="vertical"
      :wrapperCol="wrapperCol"
    >
      <a-form-item label="自定义全局 head：">
        <a-input
          type="textarea"
          :autoSize="{ minRows: 5 }"
          v-model="options.blog_custom_head"
          placeholder="放置于每个页面的 <head></head> 标签中"
        />
      </a-form-item>
      <a-form-item label="自定义内容页 head：">
        <a-input
          type="textarea"
          :autoSize="{ minRows: 5 }"
          v-model="options.blog_custom_content_head"
          placeholder="仅放置于内容页面的 <head></head> 标签中"
        />
      </a-form-item>
      <a-form-item label="统计代码：">
        <a-input
          type="textarea"
          :autoSize="{ minRows: 5 }"
          v-model="options.blog_statistics_code"
          placeholder="第三方网站统计的代码，如：Google Analytics、百度统计、CNZZ 等"
        />
      </a-form-item>
      <!-- <a-form-item
                  label="黑名单 IP："

                >
                  <a-input
                    type="textarea"
                    :autoSize="{ minRows: 5 }"
                    v-model="options.blog_ip_blacklist"
                    placeholder="多个 IP 地址换行隔开"
                  />
                </a-form-item> -->
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
  name: 'OtherTab',
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
