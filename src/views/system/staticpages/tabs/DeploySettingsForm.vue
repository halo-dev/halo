<template>
  <a-form
    layout="vertical"
    :wrapperCol="wrapperCol"
  >
    <a-form-item label="部署平台：">
      <a-select v-model="options.static_deploy_type">
        <a-select-option
          v-for="item in Object.keys(deployType)"
          :key="item"
          :value="item"
        >{{ deployType[item].text }}</a-select-option>
      </a-select>
    </a-form-item>
    <div
      id="gitForm"
      v-show="options.static_deploy_type === 'GIT'"
    >
      <a-form-item label="域名：">
        <a-input v-model="options.git_static_deploy_domain" />
      </a-form-item>
      <a-form-item label="仓库：">
        <a-input v-model="options.git_static_deploy_repository" />
      </a-form-item>
      <a-form-item label="分支：">
        <a-input v-model="options.git_static_deploy_branch" />
      </a-form-item>
      <a-form-item label="仓库用户名：">
        <a-input v-model="options.git_static_deploy_username" />
      </a-form-item>
      <a-form-item label="邮箱：">
        <a-input v-model="options.git_static_deploy_email" />
      </a-form-item>
      <a-form-item label="Token：">
        <a-input-password
          v-model="options.git_static_deploy_token"
          autocomplete="new-password"
        />
      </a-form-item>
      <a-form-item label="CNAME：">
        <a-input v-model="options.git_static_deploy_cname" />
      </a-form-item>
    </div>
    <div
      id="netlifyForm"
      v-show="options.static_deploy_type === 'NETLIFY'"
    >
      <a-form-item label="域名：">
        <a-input v-model="options.netlify_static_deploy_domain" />
      </a-form-item>
      <a-form-item label="Site ID：">
        <a-input v-model="options.netlify_static_deploy_site_id" />
      </a-form-item>
      <a-form-item label="Token：">
        <a-input-password
          v-model="options.netlify_static_deploy_token"
          autocomplete="new-password"
        />
      </a-form-item>
    </div>
    <a-form-item>
      <a-button
        type="primary"
        @click="handleSaveOptions"
      >保存</a-button>
    </a-form-item>
  </a-form>
</template>
<script>
import staticPageApi from '@/api/staticPage'
import optionApi from '@/api/option'
import { mapActions } from 'vuex'
export default {
  name: 'DeploySettingsForm',
  data() {
    return {
      deployType: staticPageApi.deployType,
      wrapperCol: {
        xl: { span: 8 },
        lg: { span: 8 },
        sm: { span: 12 },
        xs: { span: 24 }
      },
      options: []
    }
  },
  mounted() {
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
      })
    }
  }
}
</script>
