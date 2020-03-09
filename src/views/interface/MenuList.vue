<template>
  <div>
    <a-row :gutter="12">
      <a-col
        :xl="10"
        :lg="10"
        :md="10"
        :sm="24"
        :xs="24"
        :style="{ 'padding-bottom': '12px' }"
      >
        <a-card
          :title="title"
          :bodyStyle="{ padding: '16px' }"
        >
          <a-form layout="horizontal">
            <a-form-item
              label="名称："
              help="* 页面上所显示的名称"
            >
              <a-input v-model="menuToCreate.name" />
            </a-form-item>
            <a-form-item
              label="地址："
              help="* 菜单的地址"
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
                v-model="menuToCreate.priority"
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
              label="分组："
              :style="{ display: fieldExpand ? 'block' : 'none' }"
            >
              <a-auto-complete
                :dataSource="teams"
                v-model="menuToCreate.team"
                allowClear
              />
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
        <a-card
          title="所有菜单"
          :bodyStyle="{ padding: '16px' }"
        >
          <!-- Mobile -->
          <a-list
            v-if="isMobile()"
            itemLayout="vertical"
            size="large"
            :pagination="false"
            :dataSource="menus"
            :loading="loading"
          >
            <a-list-item
              slot="renderItem"
              slot-scope="item, index"
              :key="index"
            >
              <template slot="actions">
                <a-dropdown
                  placement="topLeft"
                  :trigger="['click']"
                >
                  <span>
                    <a-icon type="bars" />
                  </span>
                  <a-menu slot="overlay">
                    <a-menu-item>
                      <a
                        href="javascript:;"
                        @click="handleEditMenu(item)"
                      >编辑</a>
                    </a-menu-item>
                    <a-menu-item>
                      <a-popconfirm
                        :title="'你确定要删除【' + item.name + '】菜单？'"
                        @confirm="handleDeleteMenu(item.id)"
                        okText="确定"
                        cancelText="取消"
                      >
                        <a href="javascript:;">删除</a>
                      </a-popconfirm>
                    </a-menu-item>

                  </a-menu>
                </a-dropdown>
              </template>
              <template slot="extra">
                <span>
                  {{ item.team }}
                </span>
              </template>
              <a-list-item-meta>
                <template slot="description">
                  {{ item.url }}
                </template>
                <span
                  slot="title"
                  style="max-width: 300px;display: block;white-space: nowrap;overflow: hidden;text-overflow: ellipsis;"
                >
                  {{ item.name }}
                </span>
              </a-list-item-meta>
            </a-list-item>
          </a-list>
          <!-- Desktop -->
          <a-table
            v-else
            :columns="columns"
            :dataSource="menus"
            :loading="loading"
            :rowKey="menu => menu.id"
            :scrollToFirstRowOnChange="true"
          >
            <span
              slot="action"
              slot-scope="text, record"
            >
              <a
                href="javascript:;"
                @click="handleEditMenu(record)"
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
import { mixin, mixinDevice } from '@/utils/mixin.js'
import MenuSelectTree from './components/MenuSelectTree'
import menuApi from '@/api/menu'
const columns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true,
    scopedSlots: { customRender: 'name' }
  },
  {
    title: '地址',
    ellipsis: true,
    dataIndex: 'url'
  },
  {
    title: '分组',
    ellipsis: true,
    dataIndex: 'team'
  },
  {
    title: '排序',
    dataIndex: 'priority'
  },
  {
    title: '操作',
    key: 'action',
    scopedSlots: { customRender: 'action' }
  }
]
export default {
  components: { MenuSelectTree },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      formType: 'create',
      loading: false,
      columns,
      menus: [],
      menuToCreate: {
        target: '_self'
      },
      fieldExpand: false,
      teams: []
    }
  },
  computed: {
    title() {
      if (this.menuToCreate.id) {
        return '修改菜单'
      }
      return '添加菜单'
    }
  },
  created() {
    this.loadMenus()
    this.loadTeams()
  },
  methods: {
    loadMenus() {
      this.loading = true
      menuApi.listTree().then(response => {
        this.menus = response.data.data
        this.loading = false
      })
    },
    loadTeams() {
      menuApi.listTeams().then(response => {
        this.teams = response.data.data
      })
    },
    handleSaveClick() {
      this.createOrUpdateMenu()
    },
    handleAddMenu() {
      this.formType = 'create'
      this.menuToCreate = {}
    },
    handleEditMenu(menu) {
      this.menuToCreate = menu
      this.formType = 'update'
    },
    handleDeleteMenu(id) {
      menuApi.delete(id).then(response => {
        this.$message.success('删除成功！')
        this.loadMenus()
        this.loadTeams()
      })
    },
    createOrUpdateMenu() {
      if (!this.menuToCreate.name) {
        this.$notification['error']({
          message: '提示',
          description: '菜单名称不能为空！'
        })
        return
      }
      if (!this.menuToCreate.url) {
        this.$notification['error']({
          message: '提示',
          description: '菜单地址不能为空！'
        })
        return
      }
      if (this.menuToCreate.id) {
        menuApi.update(this.menuToCreate.id, this.menuToCreate).then(response => {
          this.$message.success('更新成功！')
          this.loadMenus()
          this.loadTeams()
        })
      } else {
        menuApi.create(this.menuToCreate).then(response => {
          this.$message.success('保存成功！')
          this.loadMenus()
          this.loadTeams()
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
