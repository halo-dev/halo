<template>
  <div>
    <a-form
      layout="vertical"
      :wrapperCol="wrapperCol"
    >
      <a-form-item label="博客标题：">
        <a-input v-model="options.blog_title" />
      </a-form-item>
      <a-form-item label="博客地址：">
        <a-input
          v-model="options.blog_url"
          placeholder="如：https://halo.run"
        />
      </a-form-item>
      <a-form-item label="Logo：">
        <a-input v-model="options.blog_logo">
          <a
            href="javascript:void(0);"
            slot="addonAfter"
            @click="logoDrawerVisible = true"
          >
            <a-icon type="picture" />
          </a>
        </a-input>
      </a-form-item>
      <a-form-item label="Favicon：">
        <a-input v-model="options.blog_favicon">
          <a
            href="javascript:void(0);"
            slot="addonAfter"
            @click="faviconDrawerVisible = true"
          >
            <a-icon type="picture" />
          </a>
        </a-input>
      </a-form-item>
      <a-form-item label="页脚信息：">
        <a-input
          type="textarea"
          :autoSize="{ minRows: 5 }"
          v-model="options.blog_footer_info"
          placeholder="支持 HTML 格式的文本"
        />
      </a-form-item>
      <a-form-item>
        <a-button
          type="primary"
          @click="handleSaveOptions"
        >保存</a-button>
      </a-form-item>
    </a-form>

    <AttachmentSelectDrawer
      v-model="logoDrawerVisible"
      @listenToSelect="handleSelectLogo"
      title="选择 Logo"
    />
    <AttachmentSelectDrawer
      v-model="faviconDrawerVisible"
      @listenToSelect="handleSelectFavicon"
      title="选择 Favicon"
    />
  </div>
</template>

<script>
import AttachmentSelectDrawer from '../../attachment/components/AttachmentSelectDrawer'
export default {
  name: 'GeneralTab',
  components: {
    AttachmentSelectDrawer
  },
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
      },
      logoDrawerVisible: false,
      faviconDrawerVisible: false
    }
  },
  destroyed: function() {
    if (this.faviconDrawerVisible) {
      this.faviconDrawerVisible = false
    }
    if (this.logoDrawerVisible) {
      this.logoDrawerVisible = false
    }
  },
  beforeRouteLeave(to, from, next) {
    if (this.faviconDrawerVisible) {
      this.faviconDrawerVisible = false
    }
    if (this.logoDrawerVisible) {
      this.logoDrawerVisible = false
    }
    next()
  },
  watch: {
    options(val) {
      this.$emit('onChange', val)
    }
  },
  methods: {
    handleSaveOptions() {
      if (!this.options.blog_title) {
        this.$notification['error']({
          message: '提示',
          description: '博客标题不能为空！'
        })
        return
      }

      if (!this.options.blog_url) {
        this.$notification['error']({
          message: '提示',
          description: '博客地址不能为空！'
        })
        return
      }
      this.$emit('onSave')
    },
    handleSelectLogo(data) {
      this.$set(this.options, 'blog_logo', encodeURI(data.path))
      this.logoDrawerVisible = false
    },
    handleSelectFavicon(data) {
      this.$set(this.options, 'blog_favicon', encodeURI(data.path))
      this.faviconDrawerVisible = false
    }
  }
}
</script>
