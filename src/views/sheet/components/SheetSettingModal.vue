<template>
  <a-modal
    v-model="modalVisible"
    :afterClose="onClosed"
    :bodyStyle="{ padding: 0 }"
    :maskClosable="false"
    :width="680"
    destroyOnClose
  >
    <template #title>
      {{ modalTitle }}
      <a-icon v-if="loading" type="loading" />
    </template>

    <div class="card-container">
      <a-tabs type="card">
        <a-tab-pane key="normal" tab="常规">
          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }" labelAlign="left">
            <a-form-item label="页面标题">
              <a-input v-model="form.model.title" />
            </a-form-item>
            <a-form-item :help="fullPath" label="页面别名">
              <a-input v-model="form.model.slug">
                <template #addonAfter>
                  <a-popconfirm
                    cancel-text="取消"
                    ok-text="确定"
                    placement="left"
                    title="是否确定根据标题重新生成别名？"
                    @confirm="handleGenerateSlug"
                  >
                    <a-icon class="cursor-pointer" type="sync" />
                  </a-popconfirm>
                </template>
              </a-input>
            </a-form-item>
            <a-form-item label="摘要">
              <a-input
                v-model="form.model.summary"
                :autoSize="{ minRows: 5 }"
                placeholder="如不填写，会从页面中自动截取"
                type="textarea"
              />
            </a-form-item>
          </a-form>
        </a-tab-pane>
        <a-tab-pane key="advanced" tab="高级">
          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }" labelAlign="left">
            <a-form-item label="禁止评论">
              <a-switch v-model="form.model.disallowComment" />
            </a-form-item>
            <a-form-item label="发表时间：">
              <a-date-picker
                :defaultValue="createTimeDefaultValue"
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择页面发表时间"
                showTime
                @change="onCreateTimeSelect"
                @ok="onCreateTimeSelect"
              />
            </a-form-item>
            <a-form-item label="自定义模板：">
              <a-select v-model="form.model.template">
                <a-select-option key="" value="">无</a-select-option>
                <a-select-option v-for="template in templates" :key="template" :value="template">
                  {{ template }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="封面图：">
              <div class="post-thumb">
                <a-space direction="vertical">
                  <img
                    :src="form.model.thumbnail || '/images/placeholder.jpg'"
                    alt="Post cover thumbnail"
                    class="img"
                    @click="attachmentSelectVisible = true"
                  />
                  <a-input
                    v-model="form.model.thumbnail"
                    allow-clear
                    placeholder="点击封面图选择图片，或者输入外部链接"
                  ></a-input>
                </a-space>
              </div>
            </a-form-item>
          </a-form>
        </a-tab-pane>
        <a-tab-pane key="seo" tab="SEO">
          <a-form :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }" labelAlign="left">
            <a-form-item label="自定义关键词">
              <a-input
                v-model="form.model.metaKeywords"
                :autoSize="{ minRows: 5 }"
                placeholder="多个关键词以英文逗号隔开"
                type="textarea"
              />
            </a-form-item>
            <a-form-item label="自定义描述">
              <a-input
                v-model="form.model.metaDescription"
                :autoSize="{ minRows: 5 }"
                placeholder="如不填写，会从页面中自动截取"
                type="textarea"
              />
            </a-form-item>
          </a-form>
        </a-tab-pane>
        <a-tab-pane key="meta" tab="元数据">
          <MetaEditor :metas.sync="form.model.metas" :targetId="form.model.id" target="sheet" />
        </a-tab-pane>
      </a-tabs>
    </div>
    <template slot="footer">
      <slot name="extraFooter" />
      <a-button :disabled="loading" @click="modalVisible = false"> 关闭</a-button>
      <ReactiveButton
        v-if="!form.model.id"
        :errored="form.draftSaveErrored"
        :loading="form.draftSaving"
        erroredText="保存失败"
        loadedText="保存成功"
        text="保存草稿"
        type="danger"
        @callback="handleSavedCallback"
        @click="handleCreateOrUpdate('DRAFT')"
      ></ReactiveButton>
      <ReactiveButton
        :errored="form.saveErrored"
        :erroredText="`${form.model.id ? '保存' : '发布'}失败`"
        :loadedText="`${form.model.id ? '保存' : '发布'}成功`"
        :loading="form.saving"
        :text="`${form.model.id ? '保存' : '发布'}`"
        @callback="handleSavedCallback"
        @click="handleCreateOrUpdate()"
      ></ReactiveButton>
    </template>
    <AttachmentSelectModal
      :multiSelect="false"
      :visible.sync="attachmentSelectVisible"
      @confirm="handleSelectSheetThumbnail"
    />
  </a-modal>
</template>
<script>
// components
import MetaEditor from '@/components/Post/MetaEditor'
// libs
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import { datetimeFormat } from '@/utils/datetime'
import pinyin from 'tiny-pinyin'
import { mapGetters } from 'vuex'
// apis
import apiClient from '@/utils/api-client'

export default {
  name: 'SheetSettingModal',
  mixins: [mixin, mixinDevice],
  components: {
    MetaEditor
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    },
    post: {
      type: Object,
      default: () => ({})
    },
    savedCallback: {
      type: Function,
      default: null
    }
  },
  data() {
    return {
      form: {
        model: {},
        saving: false,
        saveErrored: false,
        draftSaving: false,
        draftSaveErrored: false
      },
      templates: [],
      attachmentSelectVisible: false
    }
  },
  computed: {
    ...mapGetters(['options']),
    modalVisible: {
      get() {
        return this.visible
      },
      set(value) {
        this.$emit('update:visible', value)
      }
    },
    modalTitle() {
      return this.form.model.id ? '页面设置' : '页面发布'
    },
    createTimeDefaultValue() {
      if (this.form.model.createTime) {
        const date = new Date(this.form.model.createTime)
        return datetimeFormat(date, 'YYYY-MM-DD HH:mm:ss')
      }
      return datetimeFormat(new Date(), 'YYYY-MM-DD HH:mm:ss')
    },
    fullPath() {
      const permalinkType = this.options.sheet_permalink_type
      const blogUrl = this.options.blog_url
      const sheetPrefix = this.options.sheet_prefix
      const pathSuffix = this.options.path_suffix ? this.options.path_suffix : ''
      const slug = this.form.model.slug || '{slug}'
      switch (permalinkType) {
        case 'SECONDARY':
          return `${blogUrl}/${sheetPrefix}/${slug}${pathSuffix}`
        case 'ROOT':
          return `${blogUrl}/${slug}${pathSuffix}`
        default:
          return ''
      }
    }
  },
  watch: {
    modalVisible(value) {
      if (value) {
        this.form.model = Object.assign({}, this.post)
        if (!this.form.model.slug && !this.form.model.id) {
          this.handleGenerateSlug()
        }
      }
    },
    post: {
      deep: true,
      handler(value) {
        this.form.model = Object.assign({}, value)
      }
    }
  },
  created() {
    this.handleListCustomTemplates()
  },
  methods: {
    /**
     * Creates or updates a post
     */
    async handleCreateOrUpdate(preStatus = 'PUBLISHED') {
      if (!this.form.model.title) {
        this.$notification['error']({
          message: '提示',
          description: '页面标题不能为空！'
        })
        return
      }
      this.form.model.status = preStatus
      const { id, status } = this.form.model
      try {
        this.form[status === 'PUBLISHED' ? 'saving' : 'draftSaving'] = true
        if (id) {
          await apiClient.sheet.update(id, this.form.model)
        } else {
          await apiClient.sheet.create(this.form.model)
        }
      } catch (error) {
        this.form[status === 'PUBLISHED' ? 'saveErrored' : 'draftSaveErrored'] = true
        this.$log.error(error)
      } finally {
        setTimeout(() => {
          this.form.saving = false
          this.form.draftSaving = false
        }, 400)
      }
    },
    /**
     * Handle saved callback event
     */
    handleSavedCallback() {
      if (this.form.saveErrored || this.form.draftSaveErrored) {
        this.form.saveErrored = false
        this.form.draftSaveErrored = false
      } else {
        this.savedCallback && this.savedCallback()
      }
    },
    /**
     * Handle list custom templates
     */
    async handleListCustomTemplates() {
      try {
        const response = await apiClient.theme.listCustomSheetTemplates()
        this.templates = response.data
      } catch (error) {
        this.$log.error(error)
      }
    },
    /**
     * Handle create time selected event
     */
    onCreateTimeSelect(value) {
      this.form.model.createTime = value.valueOf()
    },
    /**
     * Generate slug
     */
    handleGenerateSlug() {
      if (this.form.model.title) {
        if (pinyin.isSupported()) {
          let result = ''
          const tokens = pinyin.parse(this.form.model.title.replace(/\s+/g, '').toLowerCase())
          let lastToken
          tokens.forEach(token => {
            if (token.type === 2) {
              const target = token.target ? token.target.toLowerCase() : ''
              result += result && !/\n|\s/.test(lastToken.target) ? '-' + target : target
            } else {
              result += (lastToken && lastToken.type === 2 ? '-' : '') + token.target
            }
            lastToken = token
          })
          this.$set(this.form.model, 'slug', result)
        }
      }
    },
    /**
     * Select sheet thumbnail
     * @param data
     */
    handleSelectSheetThumbnail({ raw }) {
      if (raw.length) {
        this.form.model.thumbnail = encodeURI(raw[0].path)
      }
      this.attachmentSelectVisible = false
    },
    /**
     * Handle modal close event
     */
    onClosed() {
      this.$emit('onClose')
      this.$emit('onUpdate', this.form.model)
    }
  }
}
</script>
