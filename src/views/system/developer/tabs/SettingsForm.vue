<template>
  <a-form layout="vertical" :wrapperCol="wrapperCol">
    <a-form-item label="开发者选项：">
      <a-switch v-model="options.developer_mode" />
    </a-form-item>
    <a-form-item>
      <ReactiveButton
        type="primary"
        @click="handleSaveOptions"
        @callback="errored = false"
        :loading="saving"
        :errored="errored"
        text="保存"
        loadedText="保存成功"
        erroredText="保存失败"
      ></ReactiveButton>
    </a-form-item>
  </a-form>
</template>
<script>
import { mapActions } from 'vuex'
import optionApi from '@/api/option'
export default {
  name: 'SettingsForm',
  data() {
    return {
      options: [],
      wrapperCol: {
        xl: { span: 8 },
        lg: { span: 8 },
        sm: { span: 12 },
        xs: { span: 24 }
      },
      saving: false,
      errored: false
    }
  },
  created() {
    this.handleListOptions()
  },
  methods: {
    ...mapActions(['refreshOptionsCache']),
    handleListOptions() {
      optionApi.listAll().then(response => {
        this.options = response.data.data
      })
    },
    handleSaveOptions() {
      this.saving = true
      optionApi
        .save(this.options)
        .catch(() => {
          this.errored = false
        })
        .finally(() => {
          setTimeout(() => {
            this.saving = false
          }, 400)
          this.handleListOptions()
          this.refreshOptionsCache()
          if (!this.options.developer_mode) {
            this.$router.push({ name: 'ToolList' })
          }
        })
    }
  }
}
</script>
