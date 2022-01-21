<template>
  <page-view :title="activatedTheme ? activatedTheme.name : '无'" affix subTitle="当前启用">
    <template slot="extra">
      <a-button :loading="list.loading" icon="reload" @click="handleRefreshThemesCache"> 刷新</a-button>
      <a-button icon="plus" type="primary" @click="installModal.visible = true"> 安装</a-button>
    </template>
    <a-row :gutter="12" align="middle" type="flex">
      <a-col :span="24">
        <a-list
          :dataSource="sortedThemes"
          :grid="{ gutter: 12, xs: 1, sm: 1, md: 2, lg: 4, xl: 4, xxl: 4 }"
          :loading="list.loading"
        >
          <a-list-item :key="index" slot="renderItem" slot-scope="item, index">
            <a-card :bodyStyle="{ padding: 0 }" :title="item.name" hoverable>
              <div class="theme-screenshot">
                <img :alt="item.name" :src="item.screenshots || '/images/placeholder.jpg'" loading="lazy" />
              </div>
              <template slot="actions">
                <div v-if="item.activated">
                  <a-icon style="margin-right: 3px" theme="twoTone" type="unlock" />
                  已启用
                </div>
                <div v-else @click="handleActiveTheme(item)">
                  <a-icon style="margin-right: 3px" type="lock" />
                  启用
                </div>
                <div @click="handleRouteToThemeSetting(item)">
                  <a-icon style="margin-right: 3px" type="setting" />
                  设置
                </div>
                <a-dropdown :trigger="['click']" placement="topCenter">
                  <a class="ant-dropdown-link" href="#">
                    <a-icon style="margin-right: 3px" type="ellipsis" />
                    更多
                  </a>
                  <a-menu slot="overlay">
                    <a-menu-item :key="1" :disabled="item.activated" @click="handleOpenThemeDeleteModal(item)">
                      <a-icon style="margin-right: 3px" type="delete" />
                      删除
                    </a-menu-item>
                    <a-menu-item v-if="item.repo" :key="2" @click="handleConfirmRemoteUpdate(item)">
                      <a-icon style="margin-right: 3px" type="cloud" />
                      在线更新
                    </a-menu-item>
                    <a-menu-item :key="3" @click="handleOpenLocalUpdateModal(item)">
                      <a-icon style="margin-right: 3px" type="file" />
                      本地更新
                    </a-menu-item>
                  </a-menu>
                </a-dropdown>
              </template>
            </a-card>
          </a-list-item>
        </a-list>
      </a-col>
    </a-row>

    <ThemeSettingDrawer
      v-model="themeSettingDrawer.visible"
      :theme="themeSettingDrawer.selected"
      @close="onThemeSettingsDrawerClose"
    />

    <ThemeDeleteConfirmModal
      :theme="themeDeleteModal.selected"
      :visible.sync="themeDeleteModal.visible"
      @onAfterClose="themeDeleteModal.selected = {}"
      @success="handleListThemes"
    />

    <ThemeLocalUpgradeModal
      :theme="localUpgradeModel.selected"
      :visible.sync="localUpgradeModel.visible"
      @onAfterClose="localUpgradeModel.selected = {}"
      @success="handleListThemes"
    />

    <ThemeInstallModal :visible.sync="installModal.visible" @onAfterClose="handleListThemes" />
  </page-view>
</template>

<script>
import ThemeSettingDrawer from './components/ThemeSettingDrawer'
import ThemeDeleteConfirmModal from './components/ThemeDeleteConfirmModal'
import ThemeLocalUpgradeModal from './components/ThemeLocalUpgradeModal'
import ThemeInstallModal from './components/ThemeInstallModal.vue'
import { PageView } from '@/layouts'
import apiClient from '@/utils/api-client'

export default {
  components: {
    PageView,
    ThemeSettingDrawer,
    ThemeDeleteConfirmModal,
    ThemeLocalUpgradeModal,
    ThemeInstallModal
  },
  data() {
    return {
      list: {
        loading: false,
        data: []
      },

      installModal: {
        visible: false
      },

      localUpgradeModel: {
        visible: false,
        selected: {}
      },

      themeDeleteModal: {
        visible: false,
        selected: {}
      },

      themeSettingDrawer: {
        visible: false,
        selected: {}
      }
    }
  },
  computed: {
    sortedThemes() {
      const data = this.list.data.slice(0)
      return data.sort((a, b) => {
        return b.activated - a.activated
      })
    },
    activatedTheme() {
      if (this.sortedThemes.length > 0) {
        return this.sortedThemes[0]
      }
      return null
    }
  },
  beforeMount() {
    this.handleListThemes()
  },
  methods: {
    handleListThemes() {
      this.list.loading = true
      apiClient.theme
        .list()
        .then(response => {
          this.list.data = response.data
        })
        .finally(() => {
          this.list.loading = false
        })
    },
    handleRefreshThemesCache() {
      apiClient.theme.reload().finally(() => {
        this.handleListThemes()
      })
    },
    handleActiveTheme(theme) {
      apiClient.theme.active(theme.id).finally(() => {
        this.handleListThemes()
      })
    },
    handleOpenLocalUpdateModal(item) {
      this.localUpgradeModel.selected = item
      this.localUpgradeModel.visible = true
    },
    handleRouteToThemeSetting(theme) {
      this.$router.push({ name: 'ThemeSetting', query: { themeId: theme.id } })
    },
    handleOpenThemeDeleteModal(item) {
      this.themeDeleteModal.visible = true
      this.themeDeleteModal.selected = item
    },
    handleConfirmRemoteUpdate(item) {
      const _this = this
      _this.$confirm({
        title: '提示',
        maskClosable: true,
        content: '确定更新【' + item.name + '】主题？',
        async onOk() {
          const hideLoading = _this.$message.loading('更新中...', 0)
          try {
            await apiClient.theme.updateThemeByFetching(item.id)
            _this.$message.success('更新成功！')
          } catch (e) {
            _this.$log.error('Failed to update theme: ', e)
          } finally {
            hideLoading()
            _this.handleListThemes()
          }
        }
      })
    },
    onThemeSettingsDrawerClose() {
      this.themeSettingDrawer.visible = false
      this.themeSettingDrawer.selected = {}
    }
  }
}
</script>
