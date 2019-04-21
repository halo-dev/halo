<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col
        :xl="10"
        :lg="10"
        :md="10"
        :sm="24"
        :xs="24"
        :style="{ 'padding-bottom': '12px' }">
        <a-card :title="title">
          <a-form layout="horizontal">
            <a-form-item label="网站名称：">
              <a-input v-model="link.name"/>
            </a-form-item>
            <a-form-item label="网站地址：" help="* 需要加上 http://">
              <a-input v-model="link.url"/>
            </a-form-item>
            <a-form-item label="Logo：">
              <a-input v-model="link.logo"/>
            </a-form-item>
            <a-form-item label="分组：" help="* 非必填">
              <a-input v-model="link.team"/>
            </a-form-item>
            <a-form-item label="描述：">
              <a-input type="textarea" :autosize="{ minRows: 5 }" v-model="link.description"/>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleSaveClick" v-if="formType==='create'">保存</a-button>
              <a-button-group v-else>
                <a-button type="primary" @click="handleSaveClick">更新</a-button>
                <a-button type="dashed" @click="addLink" v-if="formType==='update'">返回添加</a-button>
              </a-button-group>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
      <a-col
        :xl="14"
        :lg="14"
        :md="14"
        :sm="24"
        :xs="24"
        :style="{ 'padding-bottom': '12px' }">
        <a-card title="所有友情链接">
          <a-table
            :columns="columns"
            :dataSource="links"
            :loading="loading"
            :rowKey="link => link.id"
          >
            <template slot="url" slot-scope="text">
              <a target="_blank" :href="text">{{ text }}</a>
            </template>
            <ellipsis :length="15" tooltip slot="name" slot-scope="text">{{ text }}</ellipsis>
            <span slot="action" slot-scope="text, record">
              <a href="javascript:;" @click="editLink(record.id)">编辑</a>
              <a-divider type="vertical"/>
              <a-popconfirm
                :title="'你确定要删除【' + record.name + '】链接？'"
                @confirm="deleteLink(record.id)"
                okText="确定"
                cancelText="取消"
              >
                <a href="javascript:;">删除</a>
              </a-popconfirm>
            </span>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import linkApi from '@/api/link'
const columns = [
  {
    title: '名称',
    dataIndex: 'name',
    scopedSlots: { customRender: 'name' }
  },
  {
    title: '网址',
    dataIndex: 'url',
    scopedSlots: { customRender: 'url' }
  },
  {
    title: '分组',
    dataIndex: 'team'
  },
  {
    title: '操作',
    key: 'action',
    scopedSlots: { customRender: 'action' }
  }
]
export default {
  data() {
    return {
      title: '添加友情链接',
      formType: 'create',
      data: [],
      loading: true,
      columns,
      links: [],
      link: {}
    }
  },
  created() {
    this.loadLinks()
  },
  methods: {
    loadLinks() {
      linkApi.listAll().then(response => {
        this.links = response.data.data
        this.loading = false
      })
    },
    handleSaveClick() {
      this.createOrUpdateLink()
    },
    updateLink() {
      this.$message.success('编辑')
    },
    addLink() {
      this.title = '添加友情链接'
      this.formType = 'create'
    },
    editLink(id) {
      linkApi.get(id).then(response => {
        this.link = response.data.data
        this.title = '编辑友情链接'
        this.formType = 'update'
      })
    },
    deleteLink(id) {
      linkApi.delete(id).then(response => {
        this.$message.success('删除成功！')
        this.loadLinks()
      })
    },
    createOrUpdateLink() {
      if (this.link.id) {
        linkApi.update(this.link.id, this.link).then(response => {
          this.$message.success('更新成功！')
        })
      } else {
        linkApi.create(this.link).then(response => {
          this.$message.success('保存成功！')
        })
      }
      this.addLink()
      this.loadLinks()
      this.link = {}
    }
  }
}
</script>

<style scoped>
</style>
