<template>
  <a-drawer
    :title="selectedTheme.name + ' 主题设置'"
    width="100%"
    placement="right"
    closable
    destroyOnClose
    @close="onClose"
    :visible="visible"
  >
    <a-row
      :gutter="12"
      type="flex"
    >
      <a-col
        :xl="12"
        :lg="12"
        :md="12"
        :sm="24"
        :xs="24"
        v-if="!viewMode"
      >
        <a-skeleton
          active
          :loading="settingLoading"
          :paragraph="{rows: 10}"
        >
          <a-card :bordered="false">
            <img
              :alt="selectedTheme.name"
              :src="selectedTheme.screenshots"
              slot="cover"
            >
            <a-card-meta :description="selectedTheme.description">
              <template slot="title">
                <a
                  :href="selectedTheme.author.website"
                  target="_blank"
                >{{ selectedTheme.author.name }}</a>
              </template>
              <a-avatar
                v-if="selectedTheme.logo"
                :src="selectedTheme.logo"
                size="large"
                slot="avatar"
              />
              <a-avatar
                v-else
                size="large"
                slot="avatar"
              >{{ selectedTheme.author.name }}</a-avatar>
            </a-card-meta>
          </a-card>
        </a-skeleton>
      </a-col>
      <a-col
        :xl="formColValue"
        :lg="formColValue"
        :md="formColValue"
        :sm="24"
        :xs="24"
        style="padding-bottom: 50px;"
      >
        <a-skeleton
          active
          :loading="settingLoading"
          :paragraph="{rows: 20}"
        >
          <div class="card-container">
            <a-tabs
              type="card"
              defaultActiveKey="0"
              v-if="themeConfiguration.length>0"
            >
              <a-tab-pane
                v-for="(group, index) in themeConfiguration"
                :key="index.toString()"
                :tab="group.label"
              >
                <a-form
                  layout="vertical"
                  :wrapperCol="wrapperCol"
                >
                  <a-form-item
                    v-for="(item, index1) in group.items"
                    :label="item.label + '：'"
                    :key="index1"
                  >
                    <p
                      v-if="item.description && item.description!=''"
                      slot="help"
                      v-html="item.description"
                    ></p>
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
                      <a-radio
                        v-for="(option, index2) in item.options"
                        :key="index2"
                        :value="option.value"
                      >{{ option.label }}</a-radio>
                    </a-radio-group>
                    <a-select
                      v-model="themeSettings[item.name]"
                      :defaultValue="item.defaultValue"
                      v-else-if="item.type == 'SELECT'"
                    >
                      <a-select-option
                        v-for="option in item.options"
                        :key="option.value"
                        :value="option.value"
                      >{{ option.label }}</a-select-option>
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
                      <a
                        href="javascript:void(0);"
                        slot="addonAfter"
                        @click="handleShowSelectAttachment(item.name)"
                      >
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
            <a-alert
              message="当前主题暂无设置选项"
              banner
              v-else
            />
          </div>
        </a-skeleton>
      </a-col>

      <a-col
        :xl="20"
        :lg="20"
        :md="20"
        :sm="24"
        :xs="24"
        v-if="viewMode"
        style="padding-bottom: 50px;"
      >
        <a-card
          :bordered="true"
          :bodyStyle="{ padding: 0}"
        >
          <iframe
            id="themeViewIframe"
            title="主题预览"
            frameborder="0"
            scrolling="auto"
            border="0"
            :src="options.blog_url"
            width="100%"
            :height="clientHeight-165"
          > </iframe>
        </a-card>
      </a-col>
    </a-row>

    <AttachmentSelectDrawer
      v-model="attachmentDrawerVisible"
      @listenToSelect="handleSelectAttachment"
      title="选择附件"
    />

    <footer-tool-bar
      v-if="themeConfiguration.length>0"
      :style="{ width : '100%'}"
    >
      <a-button
        v-if="!this.isMobile() && theme.activated && viewMode"
        type="primary"
        @click="toggleViewMode"
        style="marginRight: 8px"
        ghost
      >普通模式</a-button>
      <a-button
        v-else-if="!this.isMobile() && theme.activated && !viewMode"
        type="dashed"
        @click="toggleViewMode"
        style="marginRight: 8px"
      >预览模式</a-button>
      <a-button
        type="primary"
        @click="handleSaveSettings"
      >保存</a-button>
    </footer-tool-bar>
  </a-drawer>
</template>
<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import { mapGetters } from 'vuex'
import AttachmentSelectDrawer from '../../attachment/components/AttachmentSelectDrawer'
import FooterToolBar from '@/components/FooterToolbar'
import Verte from 'verte'
import 'verte/dist/verte.css'
import themeApi from '@/api/theme'
export default {
  name: 'ThemeSetting',
  mixins: [mixin, mixinDevice],
  components: {
    AttachmentSelectDrawer,
    FooterToolBar,
    Verte
  },
  data() {
    return {
      attachmentDrawerVisible: false,
      selectedTheme: this.theme,
      themeConfiguration: [],
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
      clientHeight: document.documentElement.clientHeight
    }
  },
  model: {
    prop: 'visible',
    event: 'close'
  },
  props: {
    theme: {
      type: Object,
      required: true
    },
    visible: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  created() {
    this.loadSkeleton()
    this.initData()
  },
  watch: {
    visible: function(newValue, oldValue) {
      if (newValue) {
        this.loadSkeleton()
      }
    }
  },
  computed: {
    ...mapGetters(['options'])
  },
  methods: {
    loadSkeleton() {
      this.settingLoading = true
      setTimeout(() => {
        this.settingLoading = false
      }, 500)
    },
    initData() {
      this.settingLoading = true

      themeApi.fetchConfiguration(this.selectedTheme.id).then(response => {
        this.themeConfiguration = response.data.data
        themeApi.fetchSettings(this.selectedTheme.id).then(response => {
          this.themeSettings = response.data.data
          setTimeout(() => {
            this.settingLoading = false
          }, 300)
        })
      })
    },
    handleSaveSettings() {
      themeApi.saveSettings(this.selectedTheme.id, this.themeSettings).then(response => {
        this.$message.success('保存成功！')
        if (this.viewMode) {
          document.getElementById('themeViewIframe').contentWindow.location.reload(true)
        }
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
