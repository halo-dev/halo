<template>
  <a-modal v-model="modalVisible" destroyOnClose title="评论回复" @close="onClose">
    <template #footer>
      <ReactiveButton
        :errored="submitErrored"
        :loading="submitting"
        erroredText="回复失败"
        loadedText="回复成功"
        text="回复"
        type="primary"
        @callback="handleSubmitCallback"
        @click="handleSubmit"
      ></ReactiveButton>
    </template>
    <a-form-model ref="replyCommentForm" :model="model" :rules="rules" layout="vertical">
      <a-form-model-item prop="content">
        <a-input ref="contentInput" v-model="model.content" :autoSize="{ minRows: 8 }" type="textarea" />
      </a-form-model-item>
    </a-form-model>
  </a-modal>
</template>
<script>
import apiClient from '@/utils/api-client'

export default {
  name: 'CommentReplyModal',
  props: {
    visible: {
      type: Boolean,
      default: true
    },
    comment: {
      type: Object,
      default: null
    },
    targetId: {
      type: Number,
      default: 0
    },
    target: {
      type: String,
      required: true,
      validator: value => {
        return ['post', 'sheet', 'journal'].indexOf(value) !== -1
      }
    }
  },
  data() {
    return {
      model: {},
      submitting: false,
      submitErrored: false,
      rules: {
        content: [{ required: true, message: '* 内容不能为空', trigger: ['change'] }]
      }
    }
  },
  computed: {
    modalVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    }
  },
  watch: {
    modalVisible(value) {
      if (value) {
        this.$nextTick(() => {
          this.$refs.contentInput.focus()
        })
      }
    }
  },
  methods: {
    handleSubmit() {
      const _this = this
      _this.$refs.replyCommentForm.validate(async valid => {
        if (valid) {
          try {
            _this.submitting = true

            _this.model.postId = _this.targetId

            if (_this.comment) {
              _this.model.parentId = _this.comment.id
            }

            await apiClient.comment.create(`${_this.target}s`, _this.model)
          } catch (e) {
            _this.submitErrored = true
          } finally {
            setTimeout(() => {
              _this.submitting = false
            }, 400)
          }
        }
      })
    },

    handleSubmitCallback() {
      if (this.submitErrored) {
        this.submitErrored = false
      } else {
        this.model = {}
        this.modalVisible = false
        this.$emit('succeed')
      }
    },

    onClose() {
      this.model = {}
      this.modalVisible = false
    }
  }
}
</script>
