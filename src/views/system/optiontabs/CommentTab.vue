<template>
  <div>
    <a-form-model ref="commentOptionsForm" :model="options" :rules="rules" layout="vertical" :wrapperCol="wrapperCol">
      <a-form-model-item label="评论者头像：">
        <a-select v-model="options.comment_gravatar_default">
          <a-select-option value="mm">默认</a-select-option>
          <a-select-option value="identicon">抽象几何图形</a-select-option>
          <a-select-option value="monsterid">小怪物</a-select-option>
          <a-select-option value="wavatar">Wavatar</a-select-option>
          <a-select-option value="retro">复古</a-select-option>
          <a-select-option value="robohash">机器人</a-select-option>
          <a-select-option value="blank">不显示头像</a-select-option>
        </a-select>
      </a-form-model-item>
      <a-form-model-item label="评论审核后才显示：">
        <a-switch v-model="options.comment_new_need_check" />
      </a-form-model-item>
      <a-form-model-item label="新评论通知：">
        <a-switch v-model="options.comment_new_notice" />
      </a-form-model-item>
      <a-form-model-item label="评论回复通知对方：">
        <a-switch v-model="options.comment_reply_notice" />
      </a-form-model-item>
      <a-form-model-item label="API 评论开关：" help="* 关闭之后将无法进行评论">
        <a-switch v-model="options.comment_api_enabled" />
      </a-form-model-item>
      <a-form-model-item label="评论模块 JS：" help="* 该设置需要主题支持">
        <a-input type="textarea" :autoSize="{ minRows: 2 }" v-model="options.comment_internal_plugin_js" />
      </a-form-model-item>
      <a-form-model-item label="Gravatar 镜像源：" help="* 例如：//gravatar.com/avatar/">
        <a-input v-model="options.gravatar_source" />
      </a-form-model-item>
      <a-form-model-item label="每页显示条数： ">
        <a-input-number v-model="options.comment_page_size" :min="1" style="width:100%" />
      </a-form-model-item>
      <a-form-model-item label="占位提示：">
        <a-input v-model="options.comment_content_placeholder" />
      </a-form-model-item>
      <!-- <a-form-model-item
                  label="自定义样式："

                >
                  <a-input
                    type="textarea"
                    :autoSize="{ minRows: 5 }"
                    v-model="options.comment_custom_style"
                  />
                </a-form-model-item> -->
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
  name: 'CommentTab',
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
      // 新评论通知和回复通知验证
      if (this.options.comment_new_notice || this.options.comment_reply_notice) {
        if (!this.options.email_enabled) {
          this.$notification['error']({
            message: '提示',
            description: '新评论通知或回复通知需要打开和配置 SMTP 服务！'
          })
          return
        }
      }
      const _this = this
      _this.$refs.commentOptionsForm.validate(valid => {
        if (valid) {
          this.$emit('onSave')
        }
      })
    }
  }
}
</script>
