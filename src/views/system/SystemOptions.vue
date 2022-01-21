<template>
  <page-view :title="title">
    <template slot="extra">
      <a-button style="padding: 0" type="link" @click="advancedOptions = !advancedOptions">
        切换到{{ advancedOptions ? '基础选项' : '高级选项' }}
      </a-button>
    </template>
    <a-row>
      <a-col :span="24">
        <div class="card-container">
          <a-tabs v-if="!advancedOptions" class="general" type="card">
            <a-tab-pane key="general">
              <span slot="tab"> <a-icon type="tool" />常规设置 </span>
              <GeneralTab
                :errored="errored"
                :options="options"
                :saving="saving"
                @callback="errored = false"
                @onChange="onOptionsChange"
                @onSave="onSaveOptions"
              />
            </a-tab-pane>
            <a-tab-pane key="seo">
              <span slot="tab"> <a-icon type="global" />SEO 设置 </span>
              <SeoTab
                :errored="errored"
                :options="options"
                :saving="saving"
                @callback="errored = false"
                @onChange="onOptionsChange"
                @onSave="onSaveOptions"
              />
            </a-tab-pane>
            <a-tab-pane key="post">
              <span slot="tab"> <a-icon type="form" />文章设置 </span>
              <PostTab
                :errored="errored"
                :options="options"
                :saving="saving"
                @callback="errored = false"
                @onChange="onOptionsChange"
                @onSave="onSaveOptions"
              />
            </a-tab-pane>
            <a-tab-pane key="comment">
              <span slot="tab"> <a-icon type="message" />评论设置 </span>
              <CommentTab
                :errored="errored"
                :options="options"
                :saving="saving"
                @callback="errored = false"
                @onChange="onOptionsChange"
                @onSave="onSaveOptions"
              />
            </a-tab-pane>
            <a-tab-pane key="attachment">
              <span slot="tab"> <a-icon type="picture" />附件设置 </span>
              <AttachmentTab
                :errored="errored"
                :options="options"
                :saving="saving"
                @callback="errored = false"
                @onChange="onOptionsChange"
                @onSave="onSaveOptions"
              />
            </a-tab-pane>
            <a-tab-pane key="smtp">
              <span slot="tab"> <a-icon type="mail" />SMTP 服务 </span>
              <SmtpTab
                :errored="errored"
                :options="options"
                :saving="saving"
                @callback="errored = false"
                @onChange="onOptionsChange"
                @onSave="onSaveOptions"
              />
            </a-tab-pane>
            <a-tab-pane key="other">
              <span slot="tab"> <a-icon type="align-left" />其他设置 </span>
              <OtherTab
                :errored="errored"
                :options="options"
                :saving="saving"
                @callback="errored = false"
                @onChange="onOptionsChange"
                @onSave="onSaveOptions"
              />
            </a-tab-pane>
          </a-tabs>

          <a-tabs v-else class="advanced" type="card">
            <a-tab-pane key="permalink">
              <span slot="tab"> <a-icon type="link" />固定链接 </span>
              <PermalinkTab
                :errored="errored"
                :options="options"
                :saving="saving"
                @callback="errored = false"
                @onChange="onOptionsChange"
                @onSave="onSaveOptions"
              />
            </a-tab-pane>
            <a-tab-pane key="api">
              <span slot="tab"> <a-icon type="api" />API 设置 </span>
              <ApiTab
                :errored="errored"
                :options="options"
                :saving="saving"
                @callback="errored = false"
                @onChange="onOptionsChange"
                @onSave="onSaveOptions"
              />
            </a-tab-pane>
            <a-tab-pane key="advanced-other">
              <span slot="tab"> <a-icon type="align-left" />其他设置 </span>
              <AdvancedOtherTab
                :errored="errored"
                :options="options"
                :saving="saving"
                @callback="errored = false"
                @onChange="onOptionsChange"
                @onSave="onSaveOptions"
              />
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-col>
    </a-row>
  </page-view>
</template>
<script>
import { PageView } from '@/layouts'
import GeneralTab from './optiontabs/GeneralTab'
import SeoTab from './optiontabs/SeoTab'
import PostTab from './optiontabs/PostTab'
import CommentTab from './optiontabs/CommentTab'
import AttachmentTab from './optiontabs/AttachmentTab'
import SmtpTab from './optiontabs/SmtpTab'
import OtherTab from './optiontabs/OtherTab'
import PermalinkTab from './optiontabs/PermalinkTab'
import ApiTab from './optiontabs/ApiTab'
import AdvancedOtherTab from './optiontabs/AdvancedOtherTab'
import apiClient from '@/utils/api-client'

import { mapActions } from 'vuex'

export default {
  components: {
    PageView,
    GeneralTab,
    SeoTab,
    PostTab,
    CommentTab,
    AttachmentTab,
    SmtpTab,
    OtherTab,
    PermalinkTab,
    ApiTab,
    AdvancedOtherTab
  },
  data() {
    return {
      options: {},
      advancedOptions: false,
      saving: false,
      errored: false
    }
  },
  computed: {
    title() {
      return this.advancedOptions ? '高级选项' : '基础选项'
    }
  },
  created() {
    this.handleListOptions()
  },
  methods: {
    ...mapActions(['refreshUserCache', 'refreshOptionsCache']),

    /**
     * Get options list
     *
     * @returns {Promise<void>}
     */
    async handleListOptions() {
      try {
        const response = await apiClient.option.listAsMapView()
        this.options = response.data
      } catch (e) {
        this.$log.error(e)
      }
    },

    /**
     * Handle options change event
     *
     * @param val new options
     */
    onOptionsChange(val) {
      this.options = val
    },

    /**
     * Save options
     *
     * @returns {Promise<void>}
     */
    async onSaveOptions() {
      try {
        this.saving = true
        await apiClient.option.saveMapView(this.options)
      } catch (e) {
        this.errored = true
        this.$log.error(e)
      } finally {
        setTimeout(() => {
          this.saving = false
        }, 400)
        await this.handleListOptions()
        await this.refreshOptionsCache()
        await this.refreshUserCache()
      }
    }
  }
}
</script>
