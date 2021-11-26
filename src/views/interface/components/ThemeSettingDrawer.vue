<template>
  <a-drawer
    :afterVisibleChange="handleAfterVisibleChanged"
    :title="`${theme.name} 主题设置`"
    :visible="visible"
    closable
    destroyOnClose
    placement="right"
    width="100%"
    @close="onClose"
  >
    <a-row :gutter="12" type="flex">
      <a-col v-if="!viewMode" :lg="12" :md="12" :sm="24" :xl="12" :xs="24">
        <a-card :bordered="false">
          <img slot="cover" :alt="theme.name" :src="theme.screenshots" />
          <a-card-meta :description="theme.description">
            <template slot="title">
              <a :href="author.website" target="_blank">{{ author.name }}</a>
            </template>
            <a-avatar v-if="theme.logo" slot="avatar" :src="theme.logo" size="large" />
            <a-avatar v-else slot="avatar" size="large">{{ author.name }}</a-avatar>
          </a-card-meta>
        </a-card>
      </a-col>
      <a-col :lg="formColValue" :md="formColValue" :sm="24" :xl="formColValue" :xs="24" style="padding-bottom: 50px;">
        <a-spin :spinning="settingLoading">
          <div v-if="themeConfigurations.length > 0" class="card-container">
            <a-tabs defaultActiveKey="0" type="card">
              <a-tab-pane v-for="(group, index) in themeConfigurations" :key="index.toString()" :tab="group.label">
                <a-form :wrapperCol="wrapperCol" layout="vertical">
                  <a-form-item v-for="(item, index1) in group.items" :key="index1" :label="item.label + '：'">
                    <p v-if="item.description && item.description !== ''" slot="help" v-html="item.description"></p>
                    <a-input
                      v-if="item.type === 'TEXT'"
                      v-model="themeSettings[item.name]"
                      :defaultValue="item.defaultValue"
                      :placeholder="item.placeholder"
                    />
                    <a-input
                      v-else-if="item.type === 'TEXTAREA'"
                      v-model="themeSettings[item.name]"
                      :autoSize="{ minRows: 5 }"
                      :placeholder="item.placeholder"
                      type="textarea"
                    />
                    <a-radio-group
                      v-else-if="item.type === 'RADIO'"
                      v-model="themeSettings[item.name]"
                      v-decorator="['radio-group']"
                      :defaultValue="item.defaultValue"
                    >
                      <a-radio v-for="(option, index2) in item.options" :key="index2" :value="option.value"
                        >{{ option.label }}
                      </a-radio>
                    </a-radio-group>
                    <a-select
                      v-else-if="item.type === 'SELECT'"
                      v-model="themeSettings[item.name]"
                      :defaultValue="item.defaultValue"
                    >
                      <a-select-option v-for="option in item.options" :key="option.value" :value="option.value"
                        >{{ option.label }}
                      </a-select-option>
                    </a-select>
                    <verte
                      v-else-if="item.type === 'COLOR'"
                      v-model="themeSettings[item.name]"
                      :defaultValue="item.defaultValue"
                      model="hex"
                      picker="square"
                      style="display: inline-block;height: 24px;"
                    ></verte>
                    <a-input
                      v-else-if="item.type === 'ATTACHMENT'"
                      v-model="themeSettings[item.name]"
                      :defaultValue="item.defaultValue"
                    >
                      <a slot="addonAfter" href="javascript:void(0);" @click="handleShowSelectAttachment(item.name)">
                        <a-icon type="picture" />
                      </a>
                    </a-input>
                    <a-input-number
                      v-else-if="item.type === 'NUMBER'"
                      v-model="themeSettings[item.name]"
                      :defaultValue="item.defaultValue"
                      style="width:100%"
                    />
                    <a-switch
                      v-else-if="item.type === 'SWITCH'"
                      v-model="themeSettings[item.name]"
                      :defaultChecked="item.defaultValue"
                    />
                    <a-input
                      v-else
                      v-model="themeSettings[item.name]"
                      :defaultValue="item.defaultValue"
                      :placeholder="item.placeholder"
                    />
                  </a-form-item>
                </a-form>
              </a-tab-pane>
            </a-tabs>
          </div>
          <a-empty v-if="themeConfigurations.length <= 0 && !settingLoading" description="当前主题暂无设置选项" />
        </a-spin>
      </a-col>

      <a-col v-if="viewMode" :lg="20" :md="20" :sm="24" :xl="20" :xs="24" style="padding-bottom: 50px;">
        <a-card :bodyStyle="{ padding: 0 }" :bordered="true">
          <iframe
            id="themeViewIframe"
            :height="clientHeight - 165"
            :src="options.blog_url"
            border="0"
            frameborder="0"
            scrolling="auto"
            title="主题预览"
            width="100%"
          >
          </iframe>
        </a-card>
      </a-col>
    </a-row>

    <AttachmentSelectDrawer
      v-model="attachmentDrawerVisible"
      title="选择附件"
      @listenToSelect="handleSelectAttachment"
    />

    <footer-tool-bar v-if="themeConfigurations.length > 0" class="w-full">
      <a-space>
        <a-button v-if="!this.isMobile() && theme.activated && viewMode" ghost type="primary" @click="toggleViewMode"
          >普通模式
        </a-button>
        <a-button v-else-if="!this.isMobile() && theme.activated && !viewMode" type="dashed" @click="toggleViewMode"
          >预览模式
        </a-button>
        <ReactiveButton
          :errored="saveErrored"
          :loading="saving"
          erroredText="保存失败"
          loadedText="保存成功"
          text="保存"
          type="primary"
          @callback="saveErrored = false"
          @click="handleSaveSettings"
        ></ReactiveButton>
      </a-space>
    </footer-tool-bar>
  </a-drawer>
</template>
<script>
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import { mapGetters } from 'vuex'
import FooterToolBar from '@/components/FooterToolbar'
import Verte from 'verte'
import 'verte/dist/verte.css'
import apiClient from '@/utils/api-client'

export default {
  name: 'ThemeSetting',
  mixins: [mixin, mixinDevice],
  components: {
    FooterToolBar,
    Verte
  },
  data() {
    return {
      attachmentDrawerVisible: false,
      themeConfigurations: [],
      themeSettings: [],
      settingLoading: true,
      selectedField: '',
      wrapperCol: {
        xl: { span: 12 },
        lg: { span: 12 },
        sm: { span: 24 },
        xs: { span: 24 }
      },
      viewMode: false,
      formColValue: 12,
      clientHeight: document.documentElement.clientHeight,
      saving: false,
      saveErrored: false
    }
  },
  model: {
    prop: 'visible',
    event: 'close'
  },
  props: {
    theme: {
      type: Object,
      required: true,
      default: () => {}
    },
    visible: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  computed: {
    ...mapGetters(['options']),
    author() {
      if (this.theme.author) {
        return this.theme.author
      }
      return {}
    }
  },
  methods: {
    async handleFetchConfiguration() {
      this.settingLoading = true
      await apiClient.theme.listConfigurations(this.theme.id).then(response => {
        this.themeConfigurations = response.data
      })
      this.handleFetchSettings()
    },
    handleFetchSettings() {
      apiClient.theme
        .listSettings(this.theme.id)
        .then(response => {
          this.themeSettings = response.data
        })
        .finally(() => {
          this.settingLoading = false
        })
    },
    handleSaveSettings() {
      this.saving = true
      apiClient.theme
        .saveSettings(this.theme.id, this.themeSettings)
        .then(() => {
          if (this.viewMode) {
            document.getElementById('themeViewIframe').contentWindow.location.reload(true)
          }
        })
        .catch(() => {
          this.saveErrored = true
        })
        .finally(() => {
          setTimeout(() => {
            this.saving = false
          }, 400)
        })
    },
    onClose() {
      this.$emit('close', false)
    },
    handleShowSelectAttachment(field) {
      this.selectedField = field
      this.attachmentDrawerVisible = true
    },
    handleSelectAttachment(data) {
      this.$set(this.themeSettings, this.selectedField, encodeURI(data.path))
      // this.themeSettings[this.selectedField] = encodeURI(data.path)
      this.attachmentDrawerVisible = false
    },
    handleAfterVisibleChanged(visible) {
      if (visible) {
        this.handleFetchConfiguration()
      } else {
        this.themeConfigurations = []
        this.themeSettings = []
        this.settingLoading = true
      }
    },
    toggleViewMode() {
      this.viewMode = !this.viewMode
      if (this.viewMode) {
        this.formColValue = 4
        this.wrapperCol = {
          xl: { span: 24 },
          lg: { span: 24 },
          sm: { span: 24 },
          xs: { span: 24 }
        }
      } else {
        this.formColValue = 12
        this.wrapperCol = {
          xl: { span: 12 },
          lg: { span: 12 },
          sm: { span: 24 },
          xs: { span: 24 }
        }
      }
    }
  }
}
</script>
