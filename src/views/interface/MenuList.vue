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
        <a-card :title="title">
          <a-form layout="horizontal">
            <a-form-item
              label="名称："
              help="* 页面上所显示的名称"
            >
              <a-input v-model="menuToCreate.name" />
            </a-form-item>
            <a-form-item
              label="路径："
              help="* 菜单的路径"
            >
              <a-input v-model="menuToCreate.url" />
            </a-form-item>
            <a-form-item label="上级菜单：">
              <menu-select-tree
                :menus="menus"
                v-model="menuToCreate.parentId"
              />
            </a-form-item>
            <a-form-item label="排序编号：">
              <a-input
                type="number"
                v-model="menuToCreate.sort"
              />
            </a-form-item>
            <a-form-item
              label="图标："
              help="* 请根据主题的支持选填"
              :style="{ display: fieldExpand ? 'block' : 'none' }"
            >
              <a-input v-model="menuToCreate.icon" />
            </a-form-item>
            <a-form-item
              label="打开方式："
              :style="{ display: fieldExpand ? 'block' : 'none' }"
            >
              <a-select
                defaultValue="_self"
                v-model="menuToCreate.target"
              >
                <a-select-option value="_self">当前窗口</a-select-option>
                <a-select-option value="_blank">新窗口</a-select-option>
              </a-select>
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
                  @click="handleAddMenu"
                  v-if="formType==='update'"
                >返回添加</a-button>
              </a-button-group>
              <a
                :style="{ marginLeft: '8px'}"
                @click="toggleExpand"
              >
                更多选项
                <a-icon :type="fieldExpand ? 'up' : 'down'" />
              </a>
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
        :style="{ 'padding-bottom': '12px' }"
      >
        <a-card title="所有菜单">
          <a-table
            :columns="columns"
            :dataSource="menus"
            :loading="loading"
            :rowKey="menu => menu.id"
          >
            <ellipsis
              :length="30"
              tooltip
              slot="name"
              slot-scope="text"
            >{{ text }}</ellipsis>
            <span
              slot="action"
              slot-scope="text, record"
            >
              <a
                href="javascript:;"
                @click="handleEditMenu(record.id)"
              >编辑</a>
              <a-divider type="vertical" />
              <a-popconfirm
                :title="'你确定要删除【' + record.name + '】菜单？'"
                @confirm="handleDeleteMenu(record.id)"
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
import MenuSelectTree from './components/MenuSelectTree'
import menuApi from '@/api/menu'
const columns = [
  {
    title: '名称',
    dataIndex: 'name',
    scopedSlots: { customRender: 'name' }
  },
  {
    title: '路径',
    dataIndex: 'url'
  },
  {
    title: '排序',
    dataIndex: 'sort'
  },
  {
    title: '图标',
    dataIndex: 'icon'
  },
  {
    title: '操作',
    key: 'action',
    scopedSlots: { customRender: 'action' }
  }
]
export default {
  components: { MenuSelectTree },
  data() {
    return {
      title: '添加菜单',
      data: [],
      formType: 'create',
      loading: false,
      columns,
      menus: [],
      menuToCreate: {},
      fieldExpand: false
    }
  },
  created() {
    this.loadMenus()
  },
  methods: {
    loadMenus() {
      this.loading = true
      menuApi.listAll().then(response => {
        this.menus = response.data.data
        this.loading = false
      })
    },
    handleSaveClick() {
      this.createOrUpdateMenu()
    },
    handleAddMenu() {
      this.title = '添加菜单'
      this.formType = 'create'
      this.menuToCreate = {}
    },
    handleEditMenu(id) {
      menuApi.get(id).then(response => {
        this.menuToCreate = response.data.data
        this.title = '编辑菜单'
        this.formType = 'update'
      })
    },
    handleDeleteMenu(id) {
      menuApi.delete(id).then(response => {
        this.$message.success('删除成功！')
        this.loadMenus()
      })
    },
    createOrUpdateMenu() {
      if (this.menuToCreate.id) {
        menuApi.update(this.menuToCreate.id, this.menuToCreate).then(response => {
          this.$message.success('更新成功！')
          this.loadMenus()
        })
      } else {
        menuApi.create(this.menuToCreate).then(response => {
          this.$message.success('保存成功！')
          this.loadMenus()
        })
      }
      this.handleAddMenu()
    },
    toggleExpand() {
      this.fieldExpand = !this.fieldExpand
    }
  }
}
</script>

<style scoped>
</style>
