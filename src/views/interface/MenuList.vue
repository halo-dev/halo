<template>
  <page-view>
    <a-row :gutter="12">
      <a-col :lg="6" :md="6" :sm="24" :xl="6" :xs="24" class="mb-3">
        <a-card :bodyStyle="{ padding: '16px' }" title="分组">
          <template slot="extra">
            <ReactiveButton
              :errored="teams.default.errored"
              :loading="teams.default.saving"
              erroredText="设置失败"
              loadedText="设置成功"
              text="设为默认"
              type="default"
              @callback="handleSetDefaultTeamCallback"
              @click="handleSetDefaultTeam"
            ></ReactiveButton>
          </template>
          <div class="menu-teams">
            <a-spin :spinning="teams.loading">
              <a-empty v-if="teams.data.length === 0 && !teams.loading" />
              <a-menu
                v-if="teams.data.length > 0"
                v-model="selectedTeam"
                class="w-full"
                mode="inline"
                @select="handleSelectedTeam"
              >
                <a-menu-item v-for="team in teams.data" :key="team">
                  {{ team === '' ? '未分组' : team }}{{ defaultMenuTeam === team ? '（默认）' : '' }}
                </a-menu-item>
              </a-menu>
            </a-spin>
          </div>
          <a-popover
            v-model="teams.form.visible"
            destroyTooltipOnHide
            placement="bottom"
            title="新增分组"
            trigger="click"
            @visibleChange="handleTeamFormVisibleChange"
          >
            <template slot="content">
              <a-form-model
                ref="teamForm"
                :model="teams.form.model"
                :rules="teams.form.rules"
                @keyup.enter.native="handleCreateTeam"
              >
                <a-form-model-item prop="team">
                  <a-input v-model="teams.form.model.team" autoFocus />
                </a-form-model-item>
                <a-form-model-item style="margin-bottom: 0">
                  <a-button type="primary" @click="handleCreateTeam"> 新增 </a-button>
                </a-form-model-item>
              </a-form-model>
            </template>
            <a-button block class="mt-3" type="primary"> 新增分组 </a-button>
          </a-popover>
        </a-card>
      </a-col>
      <a-col :lg="18" :md="18" :sm="24" :xl="18" :xs="24" class="pb-3">
        <a-card :bodyStyle="{ padding: '16px' }">
          <template slot="title">
            <span>
              {{ menuListTitle }}
            </span>
            <a-tooltip
              v-if="list.data.length <= 0 && !list.loading"
              slot="action"
              title="分组下的菜单为空时，该分组也不会保存"
            >
              <a-icon class="cursor-pointer" type="info-circle-o" />
            </a-tooltip>
          </template>
          <template slot="extra">
            <a-space>
              <ReactiveButton
                :disabled="list.data.length <= 0"
                :errored="formBatch.errored"
                :loading="formBatch.saving"
                erroredText="保存失败"
                loadedText="保存成功"
                text="保存"
                @callback="formBatch.errored = false"
                @click="handleUpdateBatch"
              ></ReactiveButton>
              <a-button v-if="!form.visible" ghost type="primary" @click="handleOpenCreateMenuForm()"> 新增 </a-button>
              <a-button v-else type="default" @click="handleCloseCreateMenuForm()"> 取消新增 </a-button>
              <a-dropdown :trigger="['click']">
                <a-menu slot="overlay">
                  <a-menu-item @click="menuInternalLinkSelector.visible = true"> 从系统预设链接添加 </a-menu-item>
                  <a-menu-item @click="handleDeleteBatch"> 删除当前组 </a-menu-item>
                </a-menu>
                <a-button>
                  其他
                  <a-icon type="down" />
                </a-button>
              </a-dropdown>
            </a-space>
          </template>
          <a-spin :spinning="list.loading">
            <MenuForm
              v-if="form.visible"
              :menu="form.model"
              @cancel="handleCloseCreateMenuForm()"
              @succeed="handleCreateMenuSucceed()"
            />
            <a-empty v-if="list.data.length === 0 && !list.loading && !form.visible" />
            <MenuTreeNode v-model="list.data" :excludedTeams="excludedTeams" @reload="handleListMenus" />
          </a-spin>
        </a-card>
      </a-col>
    </a-row>
    <MenuInternalLinkSelector
      v-model="menuInternalLinkSelector.visible"
      :team="teams.selected"
      @reload="handleListMenus"
    />
  </page-view>
</template>

<script>
// components
import { PageView } from '@/layouts'
import MenuTreeNode from './components/MenuTreeNode'
import MenuForm from './components/MenuForm'
import MenuInternalLinkSelector from './components/MenuInternalLinkSelector'

import { deepClone } from '@/utils/util'
import { mapActions, mapGetters } from 'vuex'

// apis
import apiClient from '@/utils/api-client'

export default {
  components: { PageView, MenuTreeNode, MenuForm, MenuInternalLinkSelector },
  data() {
    return {
      list: {
        data: [],
        loading: false
      },
      form: {
        visible: false,
        model: {}
      },
      formBatch: {
        saving: false,
        errored: false
      },
      teams: {
        data: [],
        loading: false,
        selected: null,
        form: {
          visible: false,
          model: {
            team: null
          },
          rules: {
            team: [{ required: true, message: '分组名称不能为空', trigger: ['change'] }]
          }
        },
        default: {
          saving: false,
          errored: false
        }
      },
      menuInternalLinkSelector: {
        visible: false
      }
    }
  },
  computed: {
    ...mapGetters(['options']),
    computedMenusMoved() {
      const menus = deepClone(this.list.data)
      return this.handleMenuMoved(0, menus)
    },
    computedMenusWithoutLevel() {
      return this.handleGetMenusWithoutLevel(this.computedMenusMoved, [])
    },
    computedMenuIds() {
      return this.computedMenusWithoutLevel.map(menu => {
        return menu.id
      })
    },
    selectedTeam: {
      get() {
        return [this.teams.selected]
      },
      set(value) {
        this.teams.selected = value[0]
      }
    },
    menuListTitle() {
      return this.teams.selected === '' ? '未分组' : this.teams.selected
    },
    excludedTeams() {
      return this.teams.data.filter(item => {
        return item !== this.teams.selected
      })
    },
    defaultMenuTeam() {
      return this.options.default_menu_team ? this.options.default_menu_team : ''
    }
  },
  created() {
    this.handleListTeams()
  },
  methods: {
    ...mapActions(['refreshOptionsCache']),
    handleListTeams(autoSelectTeam = false) {
      this.teams.loading = true
      apiClient.menu
        .listTeams()
        .then(response => {
          this.teams.data = response.data
          if (!this.teams.selected || autoSelectTeam) {
            this.teams.selected = this.teams.data[0]
          }
          this.handleListMenus()
        })
        .finally(() => {
          this.teams.loading = false
        })
    },
    handleListMenus() {
      this.list.data = []
      this.list.loading = true
      apiClient.menu
        .listTreeViewByTeam(this.teams.selected)
        .then(response => {
          this.list.data = response.data
        })
        .finally(() => {
          this.list.loading = false
        })
    },
    handleMenuMoved(pid, menus) {
      for (let i = 0; i < menus.length; i++) {
        menus[i].priority = i
        menus[i].parentId = pid
        menus[i].team = this.teams.selected
        if (menus[i].children && menus[i].children.length > 0) {
          this.handleMenuMoved(menus[i].id, menus[i].children)
        }
      }
      return menus
    },
    handleGetMenusWithoutLevel(menus, result) {
      for (let i = 0; i < menus.length; i++) {
        result.push(menus[i])
        const children = menus[i].children
        if (children.length > 0) {
          this.handleGetMenusWithoutLevel(children, result)
        }
      }
      return result
    },
    handleSelectedTeam({ key }) {
      this.teams.selected = key
      this.handleCloseCreateMenuForm()
      this.handleListMenus()
    },
    handleUpdateBatch() {
      this.formBatch.saving = true
      apiClient.menu
        .updateInBatch(this.computedMenusWithoutLevel)
        .catch(() => {
          this.formBatch.errored = true
        })
        .finally(() => {
          setTimeout(() => {
            this.formBatch.saving = false
            this.handleListMenus()
          }, 400)
        })
    },
    handleDeleteBatch() {
      const _this = this
      _this.$confirm({
        title: '提示',
        content: '确定要删除当前分组以及所有菜单？',
        onOk() {
          apiClient.menu.deleteInBatch(_this.computedMenuIds).finally(() => {
            _this.handleListTeams(true)
          })
        }
      })
    },
    handleTeamFormVisibleChange(visible) {
      if (visible) {
        this.teams.form.model.team = null
      }
    },
    handleCreateTeam() {
      const _this = this
      _this.$refs.teamForm.validate(valid => {
        if (valid) {
          if (!_this.teams.data.includes(_this.teams.form.model.team)) {
            _this.teams.data.push(_this.teams.form.model.team)
          }
          _this.teams.selected = _this.teams.form.model.team
          _this.teams.form.visible = false
          _this.handleListMenus()
        }
      })
    },
    handleOpenCreateMenuForm() {
      this.form.visible = true
      this.form.model = {
        team: this.teams.selected,
        target: '_self'
      }
    },
    handleCloseCreateMenuForm() {
      this.form.visible = false
      this.form.model = {}
    },
    handleCreateMenuSucceed() {
      this.handleCloseCreateMenuForm()
      this.handleListMenus()
    },
    handleSetDefaultTeam() {
      this.teams.default.saving = true
      apiClient.option
        .save([
          {
            default_menu_team: this.teams.selected
          }
        ])
        .catch(() => {
          this.teams.default.errored = true
        })
        .finally(() => {
          setTimeout(() => {
            this.teams.default.saving = false
          }, 400)
        })
    },
    handleSetDefaultTeamCallback() {
      if (this.teams.default.errored) {
        this.teams.default.errored = false
      } else {
        this.refreshOptionsCache()
      }
    }
  }
}
</script>
