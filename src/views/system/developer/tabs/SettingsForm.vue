<template>
  <a-form layout="vertical">
    <a-form-item label="开发者选项：">
      <a-switch v-model="options.developer_mode" />
    </a-form-item>
    <a-form-item>
      <a-button
        type="primary"
        @click="handleSaveOptions"
      >保存</a-button>
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
      options: []
    }
  },
  created() {
    this.loadFormOptions()
  },
  methods: {
    ...mapActions(['loadOptions']),
    loadFormOptions() {
      optionApi.listAll().then(response => {
        this.options = response.data.data
      })
    },
    handleSaveOptions() {
      optionApi.save(this.options).then(response => {
        this.loadFormOptions()
        this.loadOptions()
        this.$message.success('保存成功！')
        if (!this.options.developer_mode) {
          this.$router.push({ name: 'ToolList' })
        }
      })
    }
  }
}
</script>
