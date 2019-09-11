<template>
  <a-drawer
    title="页面设置"
    :width="isMobile()?'100%':'460'"
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
                label="页面路径："
                :help="options.blog_url+'/s/'+ (selectedSheet.url ? selectedSheet.url : '{auto_generate}')"
              >
                <a-input v-model="selectedSheet.url" />
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
              <a-form-item label="自定义模板：">
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
          <h3 class="post-setting-drawer-title">缩略图</h3>
          <div class="post-setting-drawer-item">
            <div class="sheet-thumb">
              <img
                class="img"
                :src="selectedSheet.thumbnail || '//i.loli.net/2019/05/05/5ccf007c0a01d.png'"
                @click="()=>this.thumbDrawerVisible = true"
              >
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
      :drawerWidth="460"
    />
    <div class="bottom-control">
      <a-button
        style="marginRight: 8px"
        @click="handleDraftClick"
      >保存草稿</a-button>
      <a-button
        type="primary"
        @click="handlePublishClick"
      >发布</a-button>
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
  name: 'SheetSetting',
  mixins: [mixin, mixinDevice],
  components: {
    AttachmentSelectDrawer
  },
  data() {
    return {
      thumbDrawerVisible: false,
      settingLoading: true,
      selectedSheet: this.sheet,
      customTpls: []
    }
  },
  props: {
    sheet: {
      type: Object,
      required: true
    },
    needTitle: {
      type: Boolean,
      required: false,
      default: false
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
    visible: function(newValue, oldValue) {
      if (newValue) {
        this.loadSkeleton()
      }
    }
  },
  computed: {
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
    loadCustomTpls() {
      themeApi.customTpls().then(response => {
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
      this.saveSheet()
    },
    handleDraftClick() {
      this.selectedSheet.status = 'DRAFT'
      this.saveSheet()
    },
    saveSheet() {
      this.createOrUpdateSheet(
        () => this.$message.success('页面发布成功！'),
        () => this.$message.success('页面发布成功！'),
        false
      )
    },
    createOrUpdateSheet(createSuccess, updateSuccess, autoSave) {
      if (!this.selectedSheet.title) {
        this.$notification['error']({
          message: '提示',
          description: '页面标题不能为空！'
        })
        return
      }
      if (!this.selectedSheet.originalContent) {
        this.$notification['error']({
          message: '提示',
          description: '页面内容不能为空！'
        })
        return
      }
      if (this.selectedSheet.id) {
        sheetApi.update(this.selectedSheet.id, this.selectedSheet, autoSave).then(response => {
          this.$log.debug('Updated sheet', response.data.data)
          if (updateSuccess) {
            updateSuccess()
          }
        })
      } else {
        sheetApi.create(this.selectedSheet, autoSave).then(response => {
          this.$log.debug('Created sheet', response.data.data)
          if (createSuccess) {
            createSuccess()
          }
          this.selectedSheet = response.data.data
        })
      }
    },
    onClose() {
      this.$emit('close', false)
    },
    onSheetDateChange(value, dateString) {
      this.selectedSheet.createTime = value.valueOf()
    },
    onSheetDateOk(value) {
      this.selectedSheet.createTime = value.valueOf()
    }
  }
}
</script>
