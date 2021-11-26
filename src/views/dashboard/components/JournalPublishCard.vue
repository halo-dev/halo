<template>
  <a-card :bodyStyle="{ padding: '16px' }" :bordered="false">
    <template slot="title">
      速记
      <a-tooltip slot="action" title="内容将保存到页面/所有页面/日志页面">
        <a-icon class="cursor-pointer" type="info-circle-o" />
      </a-tooltip>
    </template>
    <a-form-model ref="journalForm" :model="form.model" :rules="form.rules" layout="vertical">
      <a-form-model-item prop="sourceContent">
        <a-input
          v-model="form.model.sourceContent"
          :autoSize="{ minRows: 8 }"
          placeholder="写点什么吧..."
          type="textarea"
        />
      </a-form-model-item>
      <a-form-model-item>
        <ReactiveButton
          :errored="form.errored"
          :loading="form.saving"
          erroredText="发布失败"
          loadedText="发布成功"
          text="发布"
          @callback="
            () => {
              if (!form.errored) form.model = {}
              form.errored = false
            }
          "
          @click="handleCreateJournalClick"
        ></ReactiveButton>
      </a-form-model-item>
    </a-form-model>
  </a-card>
</template>
<script>
import apiClient from '@/utils/api-client'

export default {
  name: 'JournalPublishCard',
  data() {
    return {
      form: {
        model: {},
        rules: {
          sourceContent: [{ required: true, message: '* 内容不能为空', trigger: ['change'] }]
        },
        saving: false,
        errored: false
      }
    }
  },
  methods: {
    handleCreateJournalClick() {
      const _this = this
      _this.$refs.journalForm.validate(valid => {
        if (valid) {
          _this.form.saving = true
          apiClient.journal
            .create(_this.form.model)
            .catch(() => {
              this.form.errored = true
            })
            .finally(() => {
              setTimeout(() => {
                _this.form.saving = false
              }, 400)
            })
        }
      })
    }
  }
}
</script>
