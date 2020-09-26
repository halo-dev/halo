<template>
  <page-view>
    <a-row :gutter="12">
      <a-col
        :xl="10"
        :lg="10"
        :md="10"
        :sm="24"
        :xs="24"
        class="mb-3"
      >
        <a-card
          :title="title"
          :bodyStyle="{ padding: '16px' }"
        >
          <a-form-model
            ref="menuForm"
            :model="form.model"
            :rules="form.rules"
            layout="horizontal"
          >
            <a-form-model-item
              label="名称："
              help="* 页面上所显示的名称"
              prop="name"
            >
              <a-input v-model="form.model.name" />
            </a-form-model-item>
            <a-form-model-item
              label="地址："
              help="* 菜单的地址"
              prop="url"
            >
              <a-input v-model="form.model.url" />
            </a-form-model-item>
            <a-form-model-item
              label="上级菜单："
              prop="parentId"
            >
              <menu-select-tree
                :menus="table.data"
                v-model="form.model.parentId"
              />
            </a-form-model-item>
            <a-form-model-item
              label="排序编号："
              prop="priority"
            >
              <a-input-number
                v-model="form.model.priority"
                :min="0"
                style="width:100%"
              />
            </a-form-model-item>
            <a-form-model-item
              v-show="form.moreField"
              label="图标："
              help="* 请根据主题的支持选填"
              prop="icon"
            >
              <a-input v-model="form.model.icon" />
            </a-form-model-item>
            <a-form-model-item
              v-show="form.moreField"
              label="分组："
              prop="team"
            >
              <a-auto-complete
                :dataSource="computedTeams"
                v-model="form.model.team"
                allowClear
              />
            </a-form-model-item>
            <a-form-model-item
              v-show="form.moreField"
              label="打开方式："
              prop="target"
            >
              <a-select
                defaultValue="_self"
                v-model="form.model.target"
              >
                <a-select-option value="_self">当前窗口</a-select-option>
                <a-select-option value="_blank">新窗口</a-select-option>
              </a-select>
            </a-form-model-item>
            <a-form-model-item>
              <ReactiveButton
                v-if="!isUpdateMode"
                type="primary"
                @click="handleCreateOrUpdateMenu"
                @callback="handleSavedCallback"
                :loading="form.saving"
                :errored="form.errored"
                text="保存"
                loadedText="保存成功"
                erroredText="保存失败"
              ></ReactiveButton>
              <a-button-group v-else>
                <ReactiveButton
                  type="primary"
                  @click="handleCreateOrUpdateMenu"
                  @callback="handleSavedCallback"
                  :loading="form.saving"
                  :errored="form.errored"
                  text="更新"
                  loadedText="更新成功"
                  erroredText="更新失败"
                ></ReactiveButton>
                <a-button
                  type="dashed"
                  @click="form.model = {}"
                  v-if="isUpdateMode"
                >返回添加</a-button>
              </a-button-group>
              <a
                class="ml-2"
                @click="form.moreField = !form.moreField"
              >
                更多选项
                <a-icon :type="form.moreField ? 'up' : 'down'" />
              </a>
            </a-form-model-item>
          </a-form-model>
        </a-card>
      </a-col>
      <a-col
        :xl="14"
        :lg="14"
        :md="14"
        :sm="24"
        :xs="24"
        class="pb-3"
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
            :dataSource="table.data"
            :loading="table.loading"
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
                        @click="form.model = item"
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
            :columns="table.columns"
            :dataSource="table.data"
            :loading="table.loading"
            :rowKey="menu => menu.id"
            :scrollToFirstRowOnChange="true"
          >
            <span
              slot="action"
              slot-scope="text, record"
            >
              <a
                href="javascript:;"
                @click="form.model = record"
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
  </page-view>
</template>

<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import MenuSelectTree from './components/MenuSelectTree'
import { PageView } from '@/layouts'
import menuApi from '@/api/menu'
const columns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true,
    scopedSlots: { customRender: 'name' },
  },
  {
    title: '地址',
    ellipsis: true,
    dataIndex: 'url',
  },
  {
    title: '分组',
    ellipsis: true,
    dataIndex: 'team',
  },
  {
    title: '排序',
    dataIndex: 'priority',
  },
  {
    title: '操作',
    key: 'action',
    scopedSlots: { customRender: 'action' },
  },
]
export default {
  components: { MenuSelectTree, PageView },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      table: {
        columns,
        data: [],
        loading: false,
      },
      form: {
        model: {
          target: '_self',
        },
        saving: false,
        errored: false,
        rules: {
          name: [
            { required: true, message: '* 菜单名称不能为空', trigger: ['change'] },
            { max: 50, message: '* 菜单名称的字符长度不能超过 50', trigger: ['change'] },
          ],
          url: [
            { required: true, message: '* 菜单地址不能为空', trigger: ['change'] },
            { max: 1023, message: '* 菜单地址的字符长度不能超过 1023', trigger: ['change'] },
          ],
          icon: [{ max: 50, message: '* 菜单图标的字符长度不能超过 50', trigger: ['change'] }],
          team: [{ max: 255, message: '* 菜单分组的字符长度不能超过 255', trigger: ['change'] }],
        },
        moreField: false,
      },
      teams: {
        data: [],
      },
    }
  },
  computed: {
    title() {
      if (this.isUpdateMode) {
        return '修改菜单'
      }
      return '添加菜单'
    },
    isUpdateMode() {
      return !!this.form.model.id
    },
    computedTeams() {
      return this.teams.data.filter((item) => {
        return item !== ''
      })
    },
  },
  created() {
    this.handleListMenus()
    this.handleListTeams()
  },
  methods: {
    handleListMenus() {
      this.table.loading = true
      menuApi
        .listTree()
        .then((response) => {
          this.table.data = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.table.loading = false
          }, 200)
        })
    },
    handleListTeams() {
      menuApi.listTeams().then((response) => {
        this.teams.data = response.data.data
      })
    },
    handleDeleteMenu(id) {
      menuApi
        .delete(id)
        .then((response) => {
          this.$message.success('删除成功！')
        })
        .finally(() => {
          this.handleListMenus()
          this.handleListTeams()
        })
    },
    handleCreateOrUpdateMenu() {
      const _this = this
      _this.$refs.menuForm.validate((valid) => {
        if (valid) {
          _this.form.saving = true
          if (_this.isUpdateMode) {
            menuApi
              .update(_this.form.model.id, _this.form.model)
              .catch(() => {
                _this.form.errored = true
              })
              .finally(() => {
                setTimeout(() => {
                  _this.form.saving = false
                }, 400)
              })
          } else {
            menuApi
              .create(_this.form.model)
              .catch(() => {
                _this.form.errored = true
              })
              .finally(() => {
                setTimeout(() => {
                  _this.form.saving = false
                }, 400)
              })
          }
        }
      })
    },
    handleSavedCallback() {
      const _this = this
      if (_this.form.errored) {
        _this.form.errored = false
      } else {
        _this.form.model = { target: '_self' }
        _this.handleListMenus()
        _this.handleListTeams()
      }
    },
  },
}
</script>
