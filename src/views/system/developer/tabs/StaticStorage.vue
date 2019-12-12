<template>
  <div class="option-tab-wrapper">
    <a-card
      :bordered="false"
      :bodyStyle="{ padding: 0 }"
    >
      <div class="table-operator">
        <a-button
          type="primary"
          icon="cloud-upload"
        >上传</a-button>
      </div>
      <div style="margin-top:15px">
        <a-table
          :rowKey="record => record.name"
          :columns="columns"
          :dataSource="statics"
          :pagination="false"
          size="middle"
          :loading="loading"
        >
          <span
            slot="createTime"
            slot-scope="createTime"
          >
            {{ createTime | moment }}
          </span>
          <span
            slot="action"
            slot-scope="text, record"
          >
            <a
              href="javascript:;"
              v-if="!record.isFile"
            >上传</a>
            <a
              :href="options.blog_url+record.relativePath"
              target="_blank"
              v-else
            >访问</a>
            <a-divider type="vertical" />
            <a-popconfirm
              :title="'你确定要删除该文件？'"
              okText="确定"
              cancelText="取消"
            >
              <a href="javascript:;">删除</a>
            </a-popconfirm>
          </span>
        </a-table>
      </div>
    </a-card>
  </div>
</template>
<script>
import { mapGetters } from 'vuex'
import staticApi from '@/api/static'
const columns = [
  {
    title: '文件名',
    dataIndex: 'name',
    scopedSlots: { customRender: 'key' }
  },
  {
    title: '文件类型',
    dataIndex: 'mediaType',
    scopedSlots: { customRender: 'mediaType' }
  },
  {
    title: '上传时间',
    dataIndex: 'createTime',
    width: '200px',
    scopedSlots: { customRender: 'createTime' }
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '120px',
    scopedSlots: { customRender: 'action' }
  }
]
export default {
  name: 'StaticStorage',
  data() {
    return {
      columns: columns,
      statics: [],
      loading: false
    }
  },
  created() {
    this.loadStaticList()
  },
  computed: {
    ...mapGetters(['options'])
  },
  methods: {
    loadStaticList() {
      this.loading = true
      staticApi.list().then(response => {
        this.statics = response.data.data
        this.loading = false
      })
    }
  }
}
</script>
