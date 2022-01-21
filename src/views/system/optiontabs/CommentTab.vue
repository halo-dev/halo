<template>
  <div>
    <a-form-model ref="commentOptionsForm" :model="options" :rules="rules" :wrapperCol="wrapperCol" layout="vertical">
      <a-form-model-item label="评论者头像：">
        <a-select v-model="options.comment_gravatar_default">
          <a-select-option v-for="(avatarType, index) in avatarTypes" :key="index" :value="avatarType.value">
            <a-avatar
              :size="18"
              :src="options.gravatar_source + '?s=256&d=' + avatarType.value"
              class="comment_select_gravatar"
            >
            </a-avatar>
            {{ avatarType.text }}
          </a-select-option>
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
      <a-form-model-item help="* 关闭之后将无法进行评论" label="API 评论开关：">
        <a-switch v-model="options.comment_api_enabled" />
      </a-form-model-item>
      <a-form-model-item help="* 该设置需要主题支持" label="评论模块 JS：">
        <a-input v-model="options.comment_internal_plugin_js" :autoSize="{ minRows: 2 }" type="textarea" />
      </a-form-model-item>
      <a-form-model-item help="* 例如：//gravatar.com/avatar/" label="Gravatar 镜像源：">
        <a-input v-model="options.gravatar_source" />
      </a-form-model-item>
      <a-form-model-item label="每页显示条数： ">
        <a-input-number v-model="options.comment_page_size" :min="1" style="width: 100%" />
      </a-form-model-item>
      <a-form-model-item label="占位提示：">
        <a-input v-model="options.comment_content_placeholder" />
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
const avatarTypes = [
  {
    text: '默认',
    value: ''
  },
  {
    text: '匿名者',
    value: 'mm'
  },
  {
    text: '抽象几何图形',
    value: 'identicon'
  },
  {
    text: '小怪物',
    value: 'monsterid'
  },
  {
    text: 'Wavatar',
    value: 'wavatar'
  },
  {
    text: '复古',
    value: 'retro'
  },
  {
    text: '机器人',
    value: 'robohash'
  },
  {
    text: '不显示头像',
    value: 'blank'
  }
]

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
      rules: {},
      avatarTypes
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

<style scoped>
.comment_select_gravatar {
  border: 1px #c8c8ca solid;
  margin: 0 5px 3px 0;
}
</style>
