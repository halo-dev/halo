<template>
  <a-drawer
    title="页面设置"
    :width="isMobile()?'100%':'480'"
    placement="right"
    closable
    destroyOnClose
    @close="onClose"
    :visible="visible"
  >
    <a-skeleton
      active
      :loading="settingLoading"
      :paragraph="{ rows: 18 }"
    >
      <div class="post-setting-drawer-content">
        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">基本设置</h3>
          <div class="post-setting-drawer-item">
            <a-form layout="vertical">
              <a-form-item
                label="页面标题："
                v-if="needTitle"
              >
                <a-input v-model="selectedSheet.title" />
              </a-form-item>
              <a-form-item
                label="页面别名："
                :help="options.blog_url+'/'+options.sheet_prefix+'/'+ (selectedSheet.slug ? selectedSheet.slug : '{slug}')+(options.path_suffix?options.path_suffix:'')"
              >
                <a-input v-model="selectedSheet.slug" />
              </a-form-item>
              <a-form-item label="发表时间：">
                <a-date-picker
                  showTime
                  :defaultValue="pickerDefaultValue"
                  format="YYYY-MM-DD HH:mm:ss"
                  placeholder="选择页面发表时间"
                  @change="onSheetDateChange"
                  @ok="onSheetDateOk"
                />
              </a-form-item>
              <a-form-item label="开启评论：">
                <a-radio-group
                  v-model="selectedSheet.disallowComment"
                  :defaultValue="false"
                >
                  <a-radio :value="false">开启</a-radio>
                  <a-radio :value="true">关闭</a-radio>
                </a-radio-group>
              </a-form-item>
              <a-form-item
                label="自定义模板："
                v-if="customTpls.length>0"
              >
                <a-select v-model="selectedSheet.template">
                  <a-select-option
                    key=""
                    value=""
                  >无</a-select-option>
                  <a-select-option
                    v-for="tpl in customTpls"
                    :key="tpl"
                    :value="tpl"
                  >{{ tpl }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-divider />

        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">摘要</h3>
          <div class="post-setting-drawer-item">
            <a-form layout="vertical">
              <a-form-item>
                <a-input
                  type="textarea"
                  :autoSize="{ minRows: 5 }"
                  v-model="selectedSheet.summary"
                  placeholder="不填写则会自动生成"
                />
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-divider />

        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">封面图</h3>
          <div class="post-setting-drawer-item">
            <div class="sheet-thumb">
              <img
                class="img"
                :src="selectedSheet.thumbnail || '/images/placeholder.jpg'"
                @click="thumbDrawerVisible = true"
              >

              <a-form layout="vertial">
                <a-form-item>
                  <a-input
                    v-model="selectedSheet.thumbnail"
                    placeholder="点击封面图选择图片，或者输入外部链接"
                  ></a-input>
                </a-form-item>
              </a-form>

              <a-button
                class="sheet-thumb-remove"
                type="dashed"
                @click="handlerRemoveThumb"
              >移除</a-button>
            </div>
          </div>
        </div>
        <a-divider class="divider-transparent" />
      </div>
    </a-skeleton>
    <AttachmentSelectDrawer
      v-model="thumbDrawerVisible"
      @listenToSelect="handleSelectSheetThumb"
      :drawerWidth="480"
    />

    <a-drawer
      title="高级设置"
      :width="isMobile()?'100%':'480'"
      placement="right"
      closable
      destroyOnClose
      @close="onAdvancedClose"
      :visible="advancedVisible"
    >
      <div class="post-setting-drawer-content">
        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">SEO 设置</h3>
          <div class="post-setting-drawer-item">
            <a-form layout="vertical">
              <a-form-item label="自定义关键词：">
                <a-input
                  v-model="selectedSheet.metaKeywords"
                  placeholder="多个关键词以英文逗号隔开"
                />
              </a-form-item>
              <a-form-item label="自定义描述：">
                <a-input
                  type="textarea"
                  :autoSize="{ minRows: 5 }"
                  v-model="selectedSheet.metaDescription"
                  placeholder="如不填写，会从页面中自动截取"
                />
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-divider />
        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">元数据</h3>
          <a-form layout="vertical">
            <a-form-item
              v-for="(meta, index) in selectedMetas"
              :key="index"
              :prop="'meta.' + index + '.value'"
            >
              <a-row :gutter="5">
                <a-col :span="12">
                  <a-input v-model="meta.key"><i slot="addonBefore">K</i></a-input>
                </a-col>
                <a-col :span="12">
                  <a-input v-model="meta.value">
                    <i slot="addonBefore">V</i>
                    <a
                      href="javascript:void(0);"
                      slot="addonAfter"
                      @click.prevent="handleRemoveSheetMeta(meta)"
                    >
                      <a-icon type="close" />
                    </a>
                  </a-input>
                </a-col>
              </a-row>
            </a-form-item>
            <a-form-item>
              <a-button
                type="dashed"
                @click="handleInsertSheetMeta()"
              >新增</a-button>
            </a-form-item>
          </a-form>
        </div>
        <a-divider class="divider-transparent" />
      </div>
    </a-drawer>

    <div class="bottom-control">
      <a-button
        style="marginRight: 8px"
        type="dashed"
        @click="advancedVisible = true"
      >高级</a-button>
      <a-button
        v-if="saveDraftButton"
        style="marginRight: 8px"
        @click="handleDraftClick"
        :loading="draftSaving"
      >保存草稿</a-button>
      <a-button
        type="primary"
        @click="handlePublishClick"
        :loading="saving"
      >{{ selectedSheet.id?'保存':'发布' }}</a-button>
    </div>
  </a-drawer>
</template>
<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import moment from 'moment'
import AttachmentSelectDrawer from '../../attachment/components/AttachmentSelectDrawer'
import { mapGetters } from 'vuex'
import themeApi from '@/api/theme'
import sheetApi from '@/api/sheet'
export default {
  name: 'SheetSettingDrawer',
  mixins: [mixin, mixinDevice],
  components: {
    AttachmentSelectDrawer
  },
  data() {
    return {
      thumbDrawerVisible: false,
      advancedVisible: false,
      settingLoading: true,
      selectedSheet: this.sheet,
      customTpls: [],
      saving: false,
      draftSaving: false
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
  created() {
    this.loadSkeleton()
    this.loadCustomTpls()
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
    },
    visible: function(newValue, oldValue) {
      if (newValue) {
        this.loadSkeleton()
        this.loadPresetMetasField()
      }
    }
  },
  computed: {
    selectedMetas() {
      return this.metas
    },
    pickerDefaultValue() {
      if (this.selectedSheet.createTime) {
        var date = new Date(this.selectedSheet.createTime)
        return moment(date, 'YYYY-MM-DD HH:mm:ss')
      }
      return moment(new Date(), 'YYYY-MM-DD HH:mm:ss')
    },
    ...mapGetters(['options'])
  },
  methods: {
    loadSkeleton() {
      this.settingLoading = true
      setTimeout(() => {
        this.settingLoading = false
      }, 500)
    },
    loadPresetMetasField() {
      if (this.metas.length <= 0) {
        themeApi.getActivatedTheme().then(response => {
          const fields = response.data.data.sheetMetaField
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
    loadCustomTpls() {
      themeApi.customSheetTpls().then(response => {
        this.customTpls = response.data.data
      })
    },
    handleSelectSheetThumb(data) {
      this.selectedSheet.thumbnail = encodeURI(data.path)
      this.thumbDrawerVisible = false
    },
    handlerRemoveThumb() {
      this.selectedSheet.thumbnail = null
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
        sheetApi
          .update(this.selectedSheet.id, this.selectedSheet, false)
          .then(response => {
            this.$log.debug('Updated sheet', response.data.data)

            if (this.selectedSheet.status === 'DRAFT') {
              this.$message.success('草稿保存成功！')
            } else {
              this.$message.success('页面更新成功！')
            }

            this.$emit('onSaved', true)
            this.$router.push({ name: 'SheetList', query: { activeKey: 'custom' } })
          })
          .finally(() => {
            this.saving = false
            this.draftSaving = false
          })
      } else {
        sheetApi
          .create(this.selectedSheet, false)
          .then(response => {
            this.$log.debug('Created sheet', response.data.data)

            if (this.selectedSheet.status === 'DRAFT') {
              this.$message.success('草稿保存成功！')
            } else {
              this.$message.success('页面发布成功！')
            }

            this.$emit('onSaved', true)
            this.$router.push({ name: 'SheetList', query: { activeKey: 'custom' } })
            this.selectedSheet = response.data.data
          })
          .finally(() => {
            this.saving = false
            this.draftSaving = false
          })
      }
    },
    onClose() {
      this.$emit('close', false)
    },
    onAdvancedClose() {
      this.advancedVisible = false
    },
    onSheetDateChange(value, dateString) {
      this.selectedSheet.createTime = value.valueOf()
    },
    onSheetDateOk(value) {
      this.selectedSheet.createTime = value.valueOf()
    },
    handleRemoveSheetMeta(item) {
      var index = this.selectedMetas.indexOf(item)
      if (index !== -1) {
        this.selectedMetas.splice(index, 1)
      }
    },
    handleInsertSheetMeta() {
      this.selectedMetas.push({
        value: '',
        key: ''
      })
    }
  }
}
</script>
