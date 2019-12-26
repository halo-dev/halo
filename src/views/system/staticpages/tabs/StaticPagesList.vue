<template>
  <div class="option-tab-wrapper">
    <a-card
      :bordered="false"
      :bodyStyle="{ padding: 0 }"
    >
      <div class="table-operator">
        <a-button
          type="primary"
          icon="reload"
          @click="handleGenerate"
        >生成</a-button>
        <a-button
          icon="cloud-upload"
          @click="handleDeploy"
          :loading="deployLoading"
          :disabled="deployLoading"
        >
          部署
        </a-button>
        <a-button
          icon="sync"
          @click="loadStaticPageList"
          :loading="loading"
          :disabled="loading"
        >
          刷新
        </a-button>
      </div>
      <div style="margin-top:15px">
        <a-table
          :rowKey="record => record.id"
          :columns="columns"
          :dataSource="staticPages"
          :pagination="false"
          size="middle"
          :loading="loading"
        >
          <span
            slot="name"
            slot-scope="name"
          >
            <ellipsis
              :length="64"
              tooltip
            >
              {{ name }}
            </ellipsis>
          </span>
        </a-table>
      </div>
    </a-card>
  </div>
</template>
<script>
import staticPageApi from '@/api/staticPage'
const columns = [
  {
    title: '文件名',
    dataIndex: 'name',
    scopedSlots: { customRender: 'name' }
  }
]
export default {
  name: 'StaticPagesList',
  data() {
    return {
      columns: columns,
      staticPages: [],
      loading: false,
      deployLoading: false
    }
  },
  created() {
    this.loadStaticPageList()
  },
  methods: {
    loadStaticPageList() {
      this.loading = true
      staticPageApi.list().then(response => {
        this.staticPages = response.data.data
        this.loading = false
      })
    },
    handleGenerate() {
      this.loading = true
      const hide = this.$message.loading('生成中...', 0)
      staticPageApi
        .generate()
        .then(response => {
          this.$message.success('生成成功！')
        })
        .finally(response => {
          this.loadStaticPageList()
          hide()
        })
    },
    handleDeploy() {
      this.deployLoading = true
      const hide = this.$message.loading('部署中...', 0)
      staticPageApi
        .deploy()
        .then(response => {
          this.$message.success('部署成功！')
        })
        .finally(response => {
          this.deployLoading = false
          hide()
        })
    }
  }
}
</script>
