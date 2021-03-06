<template>
  <a-drawer
    :title="`${theme.name} 主题设置`"
    width="100%"
    placement="right"
    closable
    destroyOnClose
    @close="onClose"
    :visible="visible"
    :afterVisibleChange="handleAfterVisibleChanged"
  >
    <a-row :gutter="12" type="flex">
      <a-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24" v-if="!viewMode">
        <a-card :bordered="false">
          <img :alt="theme.name" :src="theme.screenshots" slot="cover" />
          <a-card-meta :description="theme.description">
            <template slot="title">
              <a :href="author.website" target="_blank">{{ author.name }}</a>
            </template>
            <a-avatar v-if="theme.logo" :src="theme.logo" size="large" slot="avatar" />
            <a-avatar v-else size="large" slot="avatar">{{ author.name }}</a-avatar>
          </a-card-meta>
        </a-card>
      </a-col>
      <a-col :xl="formColValue" :lg="formColValue" :md="formColValue" :sm="24" :xs="24" style="padding-bottom: 50px;">
        <a-spin :spinning="settingLoading">
          <div class="card-container" v-if="themeConfigurations.length > 0">
            <a-tabs type="card" defaultActiveKey="0">
              <a-tab-pane v-for="(group, index) in themeConfigurations" :key="index.toString()" :tab="group.label">
                <a-form layout="vertical" :wrapperCol="wrapperCol">
                  <a-form-item v-for="(item, index1) in group.items" :label="item.label + '：'" :key="index1">
                    <p v-if="item.description && item.description != ''" slot="help" v-html="item.description"></p>
                    <a-input
                      v-model="themeSettings[item.name]"
                      :defaultValue="item.defaultValue"
                      :placeholder="item.placeholder"
                      v-if="item.type == 'TEXT'"
                    />
                    <a-input
                      type="textarea"
                      :autoSize="{ minRows: 5 }"
                      v-model="themeSettings[item.name]"
                      :placeholder="item.placeholder"
                      v-else-if="item.type == 'TEXTAREA'"
                    />
                    <a-radio-group
                      v-decorator="['radio-group']"
                      :defaultValue="item.defaultValue"
                      v-model="themeSettings[item.name]"
                      v-else-if="item.type == 'RADIO'"
                    >
                      <a-radio v-for="(option, index2) in item.options" :key="index2" :value="option.value">{{
                        option.label
                      }}</a-radio>
                    </a-radio-group>
                    <a-select
                      v-model="themeSettings[item.name]"
                      :defaultValue="item.defaultValue"
                      v-else-if="item.type == 'SELECT'"
                    >
                      <a-select-option v-for="option in item.options" :key="option.value" :value="option.value">{{
                        option.label
                      }}</a-select-option>
                    </a-select>
                    <verte
                      picker="square"
                      model="hex"
                      v-model="themeSettings[item.name]"
                      :defaultValue="item.defaultValue"
                      v-else-if="item.type == 'COLOR'"
                      style="display: inline-block;height: 24px;"
                    ></verte>
                    <a-input
                      v-model="themeSettings[item.name]"
                      :defaultValue="item.defaultValue"
                      v-else-if="item.type == 'ATTACHMENT'"
                    >
                      <a href="javascript:void(0);" slot="addonAfter" @click="handleShowSelectAttachment(item.name)">
                        <a-icon type="picture" />
                      </a>
                    </a-input>
                    <a-input-number
                      v-model="themeSettings[item.name]"
                      :defaultValue="item.defaultValue"
                      v-else-if="item.type == 'NUMBER'"
                      style="width:100%"
                    />
                    <a-switch
                      v-model="themeSettings[item.name]"
                      :defaultChecked="item.defaultValue"
                      v-else-if="item.type == 'SWITCH'"
                    />
                    <a-input
                      v-model="themeSettings[item.name]"
                      :defaultValue="item.defaultValue"
                      :placeholder="item.placeholder"
                      v-else
                    />
                  </a-form-item>
                </a-form>
              </a-tab-pane>
            </a-tabs>
          </div>
          <a-empty v-if="themeConfigurations.length <= 0 && !settingLoading" description="当前主题暂无设置选项" />
        </a-spin>
      </a-col>

      <a-col :xl="20" :lg="20" :md="20" :sm="24" :xs="24" v-if="viewMode" style="padding-bottom: 50px;">
        <a-card :bordered="true" :bodyStyle="{ padding: 0 }">
          <iframe
            id="themeViewIframe"
            title="主题预览"
            frameborder="0"
            scrolling="auto"
            border="0"
            :src="options.blog_url"
            width="100%"
            :height="clientHeight - 165"
          >
          </iframe>
        </a-card>
      </a-col>
    </a-row>

    <AttachmentSelectDrawer
      v-model="attachmentDrawerVisible"
      @listenToSelect="handleSelectAttachment"
      title="选择附件"
    />

    <footer-tool-bar v-if="themeConfigurations.length > 0" class="w-full">
      <a-space>
        <a-button v-if="!this.isMobile() && theme.activated && viewMode" type="primary" @click="toggleViewMode" ghost
          >普通模式</a-button
        >
        <a-button v-else-if="!this.isMobile() && theme.activated && !viewMode" type="dashed" @click="toggleViewMode"
          >预览模式</a-button
        >
        <ReactiveButton
          type="primary"
          @click="handleSaveSettings"
          @callback="saveErrored = false"
          :loading="saving"
          :errored="saveErrored"
          text="保存"
          loadedText="保存成功"
          erroredText="保存失败"
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
import themeApi from '@/api/theme'
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
      await themeApi.fetchConfiguration(this.theme.id).then(response => {
        this.themeConfigurations = response.data.data
      })
      this.handleFetchSettings()
    },
    handleFetchSettings() {
      themeApi
        .fetchSettings(this.theme.id)
        .then(response => {
          this.themeSettings = response.data.data
        })
        .finally(() => {
          setTimeout(() => {
            this.settingLoading = false
          }, 200)
        })
    },
    handleSaveSettings() {
      this.saving = true
      themeApi
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
