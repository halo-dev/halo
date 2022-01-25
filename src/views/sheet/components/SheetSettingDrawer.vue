<template>
  <a-drawer
    :afterVisibleChange="handleAfterVisibleChanged"
    :visible="visible"
    :width="isMobile() ? '100%' : '480'"
    closable
    destroyOnClose
    placement="right"
    title="页面设置"
    @close="onClose"
  >
    <div class="post-setting-drawer-content">
      <div class="mb-4">
        <h3 class="post-setting-drawer-title">基本设置</h3>
        <div class="post-setting-drawer-item">
          <a-form>
            <a-form-item v-if="needTitle" label="页面标题：">
              <a-input v-model="selectedSheet.title" />
            </a-form-item>
            <a-form-item :help="fullPath" label="页面别名：">
              <a-input v-model="selectedSheet.slug">
                <template #addonAfter>
                  <a-popconfirm
                    cancel-text="取消"
                    ok-text="确定"
                    placement="left"
                    title="是否确定根据标题重新生成别名？"
                    @confirm="handleSetPinyinSlug"
                  >
                    <a-icon class="cursor-pointer" type="sync" />
                  </a-popconfirm>
                </template>
              </a-input>
            </a-form-item>
            <a-form-item label="发表时间：">
              <a-date-picker
                :defaultValue="pickerDefaultValue"
                format="YYYY-MM-DD HH:mm:ss"
                placeholder="选择页面发表时间"
                showTime
                @change="onSheetDateChange"
                @ok="onSheetDateOk"
              />
            </a-form-item>
            <a-form-item label="开启评论：">
              <a-radio-group v-model="selectedSheet.disallowComment" :defaultValue="false">
                <a-radio :value="false">开启</a-radio>
                <a-radio :value="true">关闭</a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item v-if="customTpls.length > 0" label="自定义模板：">
              <a-select v-model="selectedSheet.template" :loading="customTplsLoading">
                <a-select-option key="" value="">无</a-select-option>
                <a-select-option v-for="tpl in customTpls" :key="tpl" :value="tpl">{{ tpl }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-form>
        </div>
      </div>
      <a-divider />

      <div class="mb-4">
        <h3 class="post-setting-drawer-title">摘要</h3>
        <div class="post-setting-drawer-item">
          <a-form>
            <a-form-item>
              <a-input
                v-model="selectedSheet.summary"
                :autoSize="{ minRows: 5 }"
                placeholder="不填写则会自动生成"
                type="textarea"
              />
            </a-form-item>
          </a-form>
        </div>
      </div>
      <a-divider />

      <div class="mb-4">
        <h3 class="post-setting-drawer-title">封面图</h3>
        <div class="post-setting-drawer-item">
          <div class="sheet-thumb">
            <a-space direction="vertical">
              <img
                :src="selectedSheet.thumbnail || '/images/placeholder.jpg'"
                class="img"
                @click="thumbDrawerVisible = true"
              />
              <a-input v-model="selectedSheet.thumbnail" placeholder="点击封面图选择图片，或者输入外部链接"></a-input>
              <a-button type="dashed" @click="selectedSheet.thumbnail = null">移除</a-button>
            </a-space>
          </div>
        </div>
      </div>
      <a-divider class="divider-transparent" />
    </div>

    <AttachmentSelectModal :multiSelect="false" :visible.sync="thumbDrawerVisible" @confirm="handleSelectSheetThumb" />
    <a-drawer
      :visible="advancedVisible"
      :width="isMobile() ? '100%' : '480'"
      closable
      destroyOnClose
      placement="right"
      title="高级设置"
      @close="advancedVisible = false"
    >
      <div class="post-setting-drawer-content">
        <div class="mb-4">
          <h3 class="post-setting-drawer-title">SEO 设置</h3>
          <div class="post-setting-drawer-item">
            <a-form>
              <a-form-item label="自定义关键词：">
                <a-input v-model="selectedSheet.metaKeywords" placeholder="多个关键词以英文逗号隔开" />
              </a-form-item>
              <a-form-item label="自定义描述：">
                <a-input
                  v-model="selectedSheet.metaDescription"
                  :autoSize="{ minRows: 5 }"
                  placeholder="如不填写，会从页面中自动截取"
                  type="textarea"
                />
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-divider />
        <div class="mb-4">
          <h3 class="post-setting-drawer-title">元数据</h3>
          <a-form>
            <a-form-item v-for="(meta, index) in selectedMetas" :key="index" :prop="'meta.' + index + '.value'">
              <a-row :gutter="5">
                <a-col :span="12">
                  <a-input v-model="meta.key"><i slot="addonBefore">K</i></a-input>
                </a-col>
                <a-col :span="12">
                  <a-input v-model="meta.value">
                    <i slot="addonBefore">V</i>
                    <a slot="addonAfter" href="javascript:void(0);" @click.prevent="handleRemoveSheetMeta(meta)">
                      <a-icon type="close" />
                    </a>
                  </a-input>
                </a-col>
              </a-row>
            </a-form-item>
            <a-form-item>
              <a-button type="dashed" @click="handleInsertSheetMeta()">新增</a-button>
            </a-form-item>
          </a-form>
        </div>
        <a-divider class="divider-transparent" />
      </div>
      <div class="bottom-control">
        <a-space>
          <a-button type="primary" @click="advancedVisible = false">返回</a-button>
        </a-space>
      </div>
    </a-drawer>

    <div class="bottom-control">
      <a-space>
        <a-button type="dashed" @click="advancedVisible = true">高级</a-button>
        <ReactiveButton
          v-if="saveDraftButton"
          :errored="draftSaveErrored"
          :loading="draftSaving"
          erroredText="保存失败"
          loadedText="保存成功"
          text="保存草稿"
          type="danger"
          @callback="handleSavedCallback"
          @click="handleDraftClick"
        ></ReactiveButton>
        <ReactiveButton
          :errored="saveErrored"
          :erroredText="`${selectedSheet.id ? '保存' : '发布'}失败`"
          :loadedText="`${selectedSheet.id ? '保存' : '发布'}成功`"
          :loading="saving"
          :text="`${selectedSheet.id ? '保存' : '发布'}`"
          @callback="handleSavedCallback"
          @click="handlePublishClick()"
        ></ReactiveButton>
      </a-space>
    </div>
  </a-drawer>
</template>
<script>
// libs
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import { datetimeFormat } from '@/utils/datetime'
import { mapGetters } from 'vuex'
import pinyin from 'tiny-pinyin'

// apis
import apiClient from '@/utils/api-client'

export default {
  name: 'SheetSettingDrawer',
  mixins: [mixin, mixinDevice],
  data() {
    return {
      thumbDrawerVisible: false,
      advancedVisible: false,
      customTplsLoading: false,
      selectedSheet: this.sheet,
      customTpls: [],
      saving: false,
      saveErrored: false,
      draftSaving: false,
      draftSaveErrored: false
    }
  },
  props: {
    sheet: {
      type: Object,
      required: true
    },
    metas: {
      type: Array,
      required: true
    },
    needTitle: {
      type: Boolean,
      required: false,
      default: false
    },
    saveDraftButton: {
      type: Boolean,
      required: false,
      default: true
    },
    visible: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  watch: {
    sheet(val) {
      this.selectedSheet = val
    },
    selectedSheet(val) {
      this.$emit('onRefreshSheet', val)
    },
    selectedMetas(val) {
      this.$emit('onRefreshSheetMetas', val)
    }
  },
  computed: {
    ...mapGetters(['options']),
    selectedMetas() {
      return this.metas
    },
    pickerDefaultValue() {
      if (this.selectedSheet.createTime) {
        const date = new Date(this.selectedSheet.createTime)
        return datetimeFormat(date, 'YYYY-MM-DD HH:mm:ss')
      }
      return datetimeFormat(new Date(), 'YYYY-MM-DD HH:mm:ss')
    },
    fullPath() {
      const permalinkType = this.options.sheet_permalink_type
      const blogUrl = this.options.blog_url
      const sheetPrefix = this.options.sheet_prefix
      const pathSuffix = this.options.path_suffix ? this.options.path_suffix : ''
      switch (permalinkType) {
        case 'SECONDARY':
          return `${blogUrl}/${sheetPrefix}/${
            this.selectedSheet.slug ? this.selectedSheet.slug : '{slug}'
          }${pathSuffix}`
        case 'ROOT':
          return `${blogUrl}/${this.selectedSheet.slug ? this.selectedSheet.slug : '{slug}'}${pathSuffix}`
        default:
          return ''
      }
    }
  },
  methods: {
    handleAfterVisibleChanged(visible) {
      if (visible) {
        this.handleListCustomTpls()
        this.handleListPresetMetasField()
        if (!this.selectedSheet.slug && !this.selectedSheet.id) {
          this.handleSetPinyinSlug()
        }
      }
    },
    handleListPresetMetasField() {
      if (this.metas.length <= 0) {
        apiClient.theme.getActivatedTheme().then(response => {
          const fields = response.data.sheetMetaField
          if (fields && fields.length > 0) {
            for (let i = 0, len = fields.length; i < len; i++) {
              this.selectedMetas.push({
                value: '',
                key: fields[i]
              })
            }
          }
        })
      }
    },
    handleListCustomTpls() {
      this.customTplsLoading = true
      apiClient.theme
        .listCustomSheetTemplates()
        .then(response => {
          this.customTpls = response.data
        })
        .finally(() => {
          this.customTplsLoading = false
        })
    },
    handleSelectSheetThumb({ raw }) {
      if (raw.length) {
        this.selectedSheet.thumbnail = encodeURI(raw[0].path)
      }
      this.thumbDrawerVisible = false
    },
    handlePublishClick() {
      this.selectedSheet.status = 'PUBLISHED'
      this.createOrUpdateSheet()
    },
    handleDraftClick() {
      this.selectedSheet.status = 'DRAFT'
      this.createOrUpdateSheet()
    },
    createOrUpdateSheet() {
      if (!this.selectedSheet.title) {
        this.$notification['error']({
          message: '提示',
          description: '页面标题不能为空！'
        })
        return
      }
      this.selectedSheet.metas = this.selectedMetas
      if (this.selectedSheet.status === 'DRAFT') {
        this.draftSaving = true
      } else {
        this.saving = true
      }
      if (this.selectedSheet.id) {
        apiClient.sheet
          .update(this.selectedSheet.id, this.selectedSheet)
          .catch(() => {
            if (this.selectedSheet.status === 'DRAFT') {
              this.draftSaveErrored = true
            } else {
              this.saveErrored = true
            }
          })
          .finally(() => {
            setTimeout(() => {
              this.saving = false
              this.draftSaving = false
            }, 400)
          })
      } else {
        apiClient.sheet
          .create(this.selectedSheet)
          .catch(() => {
            if (this.selectedSheet.status === 'DRAFT') {
              this.draftSaveErrored = true
            } else {
              this.saveErrored = true
            }
          })
          .then(response => {
            this.selectedSheet = response.data
          })
          .finally(() => {
            setTimeout(() => {
              this.saving = false
              this.draftSaving = false
            }, 400)
          })
      }
    },
    handleSavedCallback() {
      if (this.draftSaveErrored || this.saveErrored) {
        this.draftSaveErrored = false
        this.saveErrored = false
      } else {
        this.$emit('onSaved', true)
        this.$router.push({ name: 'SheetList', query: { activeKey: 'custom' } })
      }
    },
    onClose() {
      this.$emit('close', false)
    },
    onSheetDateChange(value) {
      this.selectedSheet.createTime = value.valueOf()
    },
    onSheetDateOk(value) {
      this.selectedSheet.createTime = value.valueOf()
    },
    handleRemoveSheetMeta(item) {
      const index = this.selectedMetas.indexOf(item)
      if (index !== -1) {
        this.selectedMetas.splice(index, 1)
      }
    },
    handleInsertSheetMeta() {
      this.selectedMetas.push({
        value: '',
        key: ''
      })
    },
    handleSetPinyinSlug() {
      if (this.selectedSheet.title) {
        if (pinyin.isSupported()) {
          let result = ''
          const tokens = pinyin.parse(this.selectedSheet.title.replace(/\s+/g, '').toLowerCase())
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
          this.$set(this.selectedSheet, 'slug', result)
        }
      }
    }
  }
}
</script>
