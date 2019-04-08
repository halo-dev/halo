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
        <a-card title="添加分类目录">
          <a-form layout="horizontal">
            <a-form-item label="名称：" help="*页面上所显示的名称">
              <a-input v-model="categoryToCreate.name" />
            </a-form-item>
            <a-form-item label="路径名称：" help="*这是文章路径上显示的名称，最好为英文">
              <a-input v-model="categoryToCreate.slugNames" />
            </a-form-item>
            <a-form-item label="上级目录：">
              <a-select>
                <a-select-option value="1">上级目录</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="描述：" help="*分类描述，部分主题可显示">
              <a-input type="textarea" v-model="categoryToCreate.description"  :autosize="{ minRows: 3 }" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="createCategory">保存</a-button>
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
        <a-card title="所有分类">
          <a-table :columns="columns" :dataSource="categories" :rowKey="record => record.id" :loading="loading">
            <ellipsis :length="30" tooltip slot="name" slot-scope="text">
              {{ text }}
            </ellipsis>
            <span slot="action" slot-scope="text, record">
              <a href="javascript:;" @click="editCategory(record.id)">编辑</a>
              <a-divider type="vertical" />
              <a-popconfirm
                :title="'你确定要删除【' + record.name + '】菜单？'"
                @confirm="deleteCategory(record.id)"
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
import categoryApi from '@/api/category'

const columns = [
  {
    title: '名称',
    dataIndex: 'name'
  },
  {
    title: '路径',
    dataIndex: 'slugName'
  },
  {
    title: '描述',
    dataIndex: 'description'
  },
  {
    title: '文章数',
    dataIndex: 'posts.count'
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
      categories: [],
      categoryToCreate: {},
      loading: false,
      columns
    }
  },
  created() {
    this.loadCategories()
  },
  methods: {
    loadCategories() {
      categoryApi.listAll().then(response => {
        this.categories = response.data.data
      })
    },
    createCategory() {
      categoryApi.create(this.categoryToCreate).then(response => {
        this.loadCategories()
      })
    },
    editCategory(id) {
      this.$message.success('编辑' + id)
    },
    deleteCategory(id) {
      categoryApi.delete(id).then(response => {
        this.$message.success('删除成功！')
        this.loadCategories()
      })
    }
  }
}
</script>

<style scoped>
</style>
