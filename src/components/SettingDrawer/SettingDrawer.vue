<template>
  <div ref="settingDrawer" class="setting-drawer">
    <a-drawer :visible="layoutSetting" closable width="300" @close="onClose">
      <div class="setting-drawer-index-content">
        <div class="mb-6">
          <h3 class="setting-drawer-index-title">整体风格设置</h3>
          <div class="setting-drawer-index-blockChecbox">
            <a-tooltip>
              <template slot="title">暗色菜单风格</template>
              <div class="setting-drawer-index-item" @click="handleMenuTheme('dark')">
                <img alt="dark" src="/images/dark.svg" />
                <div v-if="navTheme === 'dark'" class="setting-drawer-index-selectIcon">
                  <a-icon type="check" />
                </div>
              </div>
            </a-tooltip>

            <a-tooltip>
              <template slot="title">亮色菜单风格</template>
              <div class="setting-drawer-index-item" @click="handleMenuTheme('light')">
                <img alt="light" src="/images/dark.svg" />
                <div v-if="navTheme !== 'dark'" class="setting-drawer-index-selectIcon">
                  <a-icon type="check" />
                </div>
              </div>
            </a-tooltip>
          </div>
        </div>
        <a-divider />
        <div class="mb-6">
          <h3 class="setting-drawer-index-title">主题色</h3>
          <div class="h-5">
            <a-tooltip v-for="(item, index) in colorList" :key="index" class="setting-drawer-theme-color-colorBlock">
              <template slot="title">{{ item.key }}</template>
              <a-tag :color="item.color" @click="changeColor(item.color)">
                <a-icon v-if="item.color === primaryColor" type="check"></a-icon>
              </a-tag>
            </a-tooltip>
          </div>
        </div>
        <a-divider />
        <div class="mb-6">
          <h3 class="setting-drawer-index-title">导航模式</h3>

          <div class="setting-drawer-index-blockChecbox">
            <div class="setting-drawer-index-item" @click="handleLayout('sidemenu')">
              <img alt="sidemenu" src="/images/sidemenu.svg" />
              <div v-if="layoutMode === 'sidemenu'" class="setting-drawer-index-selectIcon">
                <a-icon type="check" />
              </div>
            </div>

            <div class="setting-drawer-index-item" @click="handleLayout('topmenu')">
              <img alt="topmenu" src="/images/topmenu.svg" />
              <div v-if="layoutMode !== 'sidemenu'" class="setting-drawer-index-selectIcon">
                <a-icon type="check" />
              </div>
            </div>
          </div>
        </div>
        <a-divider />
        <div class="mt-6">
          <a-list :split="false">
            <a-list-item>
              <a-tooltip slot="actions">
                <template slot="title">
                  该设定仅 [顶部栏导航] 时有效
                </template>
                <a-select
                  :defaultValue="contentWidth"
                  size="small"
                  style="width: 80px;"
                  @change="handleContentWidthChange"
                >
                  <a-select-option value="Fixed">固定</a-select-option>
                  <a-select-option v-if="layoutMode !== 'sidemenu'" value="Fluid">流式</a-select-option>
                </a-select>
              </a-tooltip>
              <a-list-item-meta>
                <div slot="title">内容区域宽度</div>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-switch slot="actions" :defaultChecked="fixedHeader" size="small" @change="handleFixedHeader" />
              <a-list-item-meta>
                <div slot="title">固定 Header</div>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-switch
                slot="actions"
                :defaultChecked="autoHideHeader"
                :disabled="!fixedHeader"
                size="small"
                @change="handleFixedHeaderHidden"
              />
              <a-list-item-meta>
                <a-tooltip slot="title" placement="left">
                  <template slot="title">固定 Header 时可配置</template>
                  <div :style="{ opacity: !fixedHeader ? '0.5' : '1' }">下滑时隐藏 Header</div>
                </a-tooltip>
              </a-list-item-meta>
            </a-list-item>
            <a-list-item>
              <a-switch
                slot="actions"
                :defaultChecked="fixedSidebar"
                :disabled="layoutMode === 'topmenu'"
                size="small"
                @change="handleFixedSidebar"
              />
              <a-list-item-meta>
                <div slot="title" :style="{ opacity: layoutMode === 'topmenu' ? '0.5' : '1' }">固定侧边菜单</div>
              </a-list-item-meta>
            </a-list-item>
          </a-list>
        </div>
        <a-divider />
      </div>
    </a-drawer>
  </div>
</template>

<script>
import config from '@/config/defaultSettings'
import { colorList, updateTheme } from './setting'
import { mixin, mixinDevice } from '@/mixins/mixin'
import { mapActions, mapGetters } from 'vuex'

export default {
  mixins: [mixin, mixinDevice],
  data() {
    return {
      colorList,
      baseConfig: Object.assign({}, config)
    }
  },
  watch: {},
  mounted() {
    // 当主题色不是默认色时，才进行主题编译
    if (this.primaryColor !== config.primaryColor) {
      updateTheme(this.primaryColor)
    }
  },
  computed: {
    ...mapGetters(['layoutSetting'])
  },
  methods: {
    ...mapActions(['setSidebar', 'ToggleLayoutSetting']),
    onClose() {
      this.ToggleLayoutSetting(false)
    },
    handleMenuTheme(theme) {
      this.baseConfig.navTheme = theme
      this.$store.dispatch('ToggleTheme', theme)
    },
    handleLayout(mode) {
      this.baseConfig.layout = mode
      this.$store.dispatch('ToggleLayoutMode', mode)
      this.handleFixedSidebar(false)
      if (mode === 'sidemenu') {
        this.handleContentWidthChange('Fixed')
      }
    },
    handleContentWidthChange(type) {
      this.baseConfig.contentWidth = type
      this.$store.dispatch('ToggleContentWidth', type)
    },
    changeColor(color) {
      this.baseConfig.primaryColor = color
      if (this.primaryColor !== color) {
        this.$store.dispatch('ToggleColor', color)
        updateTheme(color)
      }
    },
    handleFixedHeader(fixed) {
      this.baseConfig.fixedHeader = fixed
      this.$store.dispatch('ToggleFixedHeader', fixed)
    },
    handleFixedHeaderHidden(autoHidden) {
      this.baseConfig.autoHideHeader = autoHidden
      this.$store.dispatch('ToggleFixedHeaderHidden', autoHidden)
    },
    handleFixedSidebar(fixed) {
      if (this.layoutMode === 'topmenu') {
        this.baseConfig.fixedSidebar = false
        this.$store.dispatch('ToggleFixedSidebar', false)
        return
      }
      this.baseConfig.fixedSidebar = fixed
      this.$store.dispatch('ToggleFixedSidebar', fixed)
    }
  }
}
</script>

<style lang="less" scoped>
.setting-drawer-index-content {
  .setting-drawer-index-blockChecbox {
    display: flex;

    .setting-drawer-index-item {
      margin-right: 16px;
      position: relative;
      border-radius: 4px;
      cursor: pointer;

      img {
        width: 48px;
      }

      .setting-drawer-index-selectIcon {
        position: absolute;
        top: 0;
        right: 0;
        width: 100%;
        padding-top: 15px;
        padding-left: 24px;
        height: 100%;
        color: #1890ff;
        font-size: 14px;
        font-weight: 700;
      }
    }
  }

  .setting-drawer-theme-color-colorBlock {
    width: 20px;
    height: 20px;
    border-radius: 2px;
    float: left;
    cursor: pointer;
    margin-right: 8px;
    padding-left: 0;
    padding-right: 0;
    text-align: center;
    color: #fff;
    font-weight: 700;

    i {
      font-size: 14px;
    }
  }
}

.setting-drawer-index-handle {
  position: absolute;
  top: 240px;
  background: #1890ff;
  width: 48px;
  height: 48px;
  right: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  pointer-events: auto;
  z-index: 1001;
  text-align: center;
  font-size: 16px;
  border-radius: 4px 0 0 4px;

  i {
    color: rgb(255, 255, 255);
    font-size: 20px;
  }
}
</style>
