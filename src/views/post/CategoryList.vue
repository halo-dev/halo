<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col
        :xl="10"
        :lg="10"
        :md="10"
        :sm="24"
        :xs="24"
        :style="{ 'padding-bottom': '12px' }"
      >
        <a-card :title="title" :bodyStyle="{ padding: '16px' }">
          <a-form layout="horizontal">
            <a-form-item
              label="名称："
              help="* 页面上所显示的名称"
            >
              <a-input v-model="categoryToCreate.name" />
            </a-form-item>
            <a-form-item
              label="别名："
              help="* 一般为单个分类页面的标识，最好为英文"
            >
              <a-input v-model="categoryToCreate.slugName" />
            </a-form-item>
            <a-form-item label="上级目录：">
              <category-select-tree
                :categories="categories"
                v-model="categoryToCreate.parentId"
              />
            </a-form-item>
            <a-form-item
              label="描述："
              help="* 分类描述，部分主题可显示"
            >
              <a-input
                type="textarea"
                v-model="categoryToCreate.description"
                :autosize="{ minRows: 3 }"
              />
            </a-form-item>
            <a-form-item>
              <a-button
                type="primary"
                @click="handleSaveClick"
                v-if="formType==='create'"
              >保存</a-button>
              <a-button-group v-else>
                <a-button
                  type="primary"
                  @click="handleSaveClick"
                >更新</a-button>
                <a-button
                  type="dashed"
                  @click="handleAddCategory"
                  v-if="formType==='update'"
                >返回添加</a-button>
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
        :style="{ 'padding-bottom': '1rem' }"
      >
        <a-card title="分类列表" :bodyStyle="{ padding: '16px' }">
          <a-table
            :columns="columns"
            :dataSource="categories"
            :rowKey="record => record.id"
            :loading="loading"
          >
            <ellipsis
              :length="30"
              tooltip
              slot="name"
              slot-scope="text"
            >
              {{ text }}
            </ellipsis>
            <span
              slot="action"
              slot-scope="text, record"
            >
              <a
                href="javascript:;"
                @click="handleEditCategory(record)"
              >编辑</a>
              <a-divider type="vertical" />
              <a-dropdown :trigger="['click']">
                <a
                  href="javascript:void(0);"
                  class="ant-dropdown-link"
                >更多</a>
                <a-menu slot="overlay">
                  <a-menu-item key="1">
                    <a-popconfirm
                      :title="'你确定要添加【' + record.name + '】到菜单？'"
                      @confirm="handleCategoryToMenu(record)"
                      okText="确定"
                      cancelText="取消"
                    >
                      <a href="javascript:void(0);">添加到菜单</a>
                    </a-popconfirm>
                  </a-menu-item>
                  <a-menu-item key="2">
                    <a-popconfirm
                      :title="'你确定要删除【' + record.name + '】分类？'"
                      @confirm="handleDeleteCategory(record.id)"
                      okText="确定"
                      cancelText="取消"
                    >
                      <a href="javascript:void(0);">删除</a>
                    </a-popconfirm>
                  </a-menu-item>
                </a-menu>
              </a-dropdown>
            </span>
          </a-table>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import CategorySelectTree from './components/CategorySelectTree'
import categoryApi from '@/api/category'
import menuApi from '@/api/menu'

const columns = [
  {
    title: '名称',
    dataIndex: 'name'
  },
  {
    title: '别名',
    dataIndex: 'slugName'
  },
  {
    title: '描述',
    dataIndex: 'description'
  },
  {
    title: '文章数',
    dataIndex: 'postCount'
  },
  {
    title: '操作',
    key: 'action',
    scopedSlots: { customRender: 'action' }
  }
]
export default {
  components: { CategorySelectTree },
  data() {
    return {
      formType: 'create',
      categories: [],
      categoryToCreate: {},
      menu: {},
      loading: false,
      columns
    }
  },
  computed: {
    title() {
      if (this.categoryToCreate.id) {
        return '修改分类'
      }
      return '添加分类'
    }
  },
  created() {
    this.loadCategories()
  },
  methods: {
    loadCategories() {
      this.loading = true
      categoryApi.listAll(true).then(response => {
        this.categories = response.data.data
        this.loading = false
      })
    },
    handleSaveClick() {
      this.createOrUpdateCategory()
    },
    handleAddCategory() {
      this.formType = 'create'
      this.categoryToCreate = {}
    },
    handleEditCategory(category) {
      this.categoryToCreate = category
      this.formType = 'update'
    },
    handleDeleteCategory(id) {
      categoryApi.delete(id).then(response => {
        this.$message.success('删除成功！')
        this.loadCategories()
        this.handleAddCategory()
      })
    },
    createOrUpdateCategory() {
      if (!this.categoryToCreate.name) {
        this.$notification['error']({
          message: '提示',
          description: '分类名称不能为空！'
        })
        return
      }
      if (this.categoryToCreate.id) {
        categoryApi.update(this.categoryToCreate.id, this.categoryToCreate).then(response => {
          this.$message.success('更新成功！')
          this.loadCategories()
          this.categoryToCreate = {}
        })
      } else {
        categoryApi.create(this.categoryToCreate).then(response => {
          this.$message.success('保存成功！')
          this.loadCategories()
          this.categoryToCreate = {}
        })
      }
      this.handleAddCategory()
    },
    handleCategoryToMenu(category) {
      this.menu['name'] = category.name
      this.menu['url'] = `/categories/${category.slugName}`
      menuApi.create(this.menu).then(response => {
        this.$message.success('添加到菜单成功！')
        this.menu = {}
      })
    }
  }
}
</script>
