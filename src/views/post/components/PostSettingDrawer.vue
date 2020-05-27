<template>
  <a-drawer
    title="文章设置"
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
      :paragraph="{ rows: 24 }"
    >
      <div class="post-setting-drawer-content">
        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">基本设置</h3>
          <div class="post-setting-drawer-item">
            <a-form layout="vertical">
              <a-form-item
                label="文章标题："
                v-if="needTitle"
              >
                <a-input v-model="selectedPost.title" />
              </a-form-item>
              <a-form-item label="文章别名：">
                <template slot="help">
                  <span v-if="options.post_permalink_type === 'DEFAULT'">{{ options.blog_url }}/{{ options.archives_prefix }}/{{ selectedPost.slug?selectedPost.slug:'${slug}' }}{{ (options.path_suffix?options.path_suffix:'') }}</span>
                  <span v-else-if="options.post_permalink_type === 'DATE'">{{ options.blog_url }}{{ selectedPost.createTime?selectedPost.createTime:new Date() | moment_post_date }}{{ selectedPost.slug?selectedPost.slug:'${slug}' }}{{ (options.path_suffix?options.path_suffix:'') }}</span>
                  <span v-else-if="options.post_permalink_type === 'DAY'">{{ options.blog_url }}{{ selectedPost.createTime?selectedPost.createTime:new Date() | moment_post_day }}{{ selectedPost.slug?selectedPost.slug:'${slug}' }}{{ (options.path_suffix?options.path_suffix:'') }}</span>
                  <span v-else-if="options.post_permalink_type === 'ID'">{{ options.blog_url }}/?p={{ selectedPost.id?selectedPost.id:'${id}' }}</span>
                </template>
                <a-input v-model="selectedPost.slug" />
              </a-form-item>

              <a-form-item label="发表时间：">
                <a-date-picker
                  showTime
                  :defaultValue="pickerDefaultValue"
                  format="YYYY-MM-DD HH:mm:ss"
                  placeholder="选择文章发表时间"
                  @change="onPostDateChange"
                  @ok="onPostDateOk"
                />
              </a-form-item>
              <a-form-item label="开启评论：">
                <a-radio-group
                  v-model="selectedPost.disallowComment"
                  :defaultValue="false"
                >
                  <a-radio :value="false">开启</a-radio>
                  <a-radio :value="true">关闭</a-radio>
                </a-radio-group>
              </a-form-item>
              <a-form-item label="是否置顶：">
                <a-radio-group
                  v-model="selectedPost.topPriority"
                  :defaultValue="0"
                >
                  <a-radio :value="1">是</a-radio>
                  <a-radio :value="0">否</a-radio>
                </a-radio-group>
              </a-form-item>
              <a-form-item
                label="自定义模板："
                v-if="customTpls.length > 0"
              >
                <a-select v-model="selectedPost.template">
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
          <h3 class="post-setting-drawer-title">分类目录</h3>
          <div class="post-setting-drawer-item">
            <a-form layout="vertical">
              <a-form-item>
                <category-tree
                  v-model="selectedCategoryIds"
                  :categories="categories"
                />
              </a-form-item>
              <a-form-item v-if="categoryFormVisible">
                <category-select-tree
                  :categories="categories"
                  v-model="categoryToCreate.parentId"
                />
              </a-form-item>
              <a-form-item v-if="categoryFormVisible">
                <a-input
                  placeholder="分类名称"
                  v-model="categoryToCreate.name"
                />
              </a-form-item>
              <a-form-item v-if="categoryFormVisible">
                <a-input
                  placeholder="分类路径"
                  v-model="categoryToCreate.slug"
                />
              </a-form-item>
              <a-form-item>
                <a-button
                  type="primary"
                  style="marginRight: 8px"
                  v-if="categoryFormVisible"
                  @click="handlerCreateCategory"
                >保存</a-button>
                <a-button
                  type="dashed"
                  style="marginRight: 8px"
                  v-if="!categoryFormVisible"
                  @click="toggleCategoryForm"
                >新增</a-button>
                <a-button
                  v-if="categoryFormVisible"
                  @click="toggleCategoryForm"
                >取消</a-button>
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-divider />

        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">标签</h3>
          <div class="post-setting-drawer-item">
            <a-form layout="vertical">
              <a-form-item>
                <TagSelect v-model="selectedTagIds" />
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
                  v-model="selectedPost.summary"
                  placeholder="如不填写，会从文章中自动截取"
                />
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-divider />

        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">封面图</h3>
          <div class="post-setting-drawer-item">
            <div class="post-thumb">
              <img
                class="img"
                :src="selectedPost.thumbnail || '/images/placeholder.jpg'"
                @click="thumbDrawerVisible=true"
              >

              <a-form layout="vertial">
                <a-form-item>
                  <a-input
                    v-model="selectedPost.thumbnail"
                    placeholder="点击封面图选择图片，或者输入外部链接"
                  ></a-input>
                </a-form-item>
              </a-form>

              <a-button
                class="post-thumb-remove"
                type="dashed"
                @click="handleRemoveThumb"
              >移除</a-button>
            </div>
          </div>
        </div>
        <a-divider class="divider-transparent" />
      </div>
    </a-skeleton>
    <AttachmentSelectDrawer
      v-model="thumbDrawerVisible"
      @listenToSelect="handleSelectPostThumb"
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
          <h3 class="post-setting-drawer-title">加密设置</h3>
          <div class="post-setting-drawer-item">
            <a-form layout="vertical">
              <a-form-item label="访问密码：">
                <a-input-password
                  v-model="selectedPost.password"
                  autocomplete="new-password"
                />
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-divider />
        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">SEO 设置</h3>
          <div class="post-setting-drawer-item">
            <a-form layout="vertical">
              <a-form-item label="自定义关键词：">
                <a-input
                  v-model="selectedPost.metaKeywords"
                  placeholder="多个关键词以英文逗号隔开，如不填写，将自动使用标签作为关键词"
                />
              </a-form-item>
              <a-form-item label="自定义描述：">
                <a-input
                  type="textarea"
                  :autoSize="{ minRows: 5 }"
                  v-model="selectedPost.metaDescription"
                  placeholder="如不填写，会从文章中自动截取"
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
              :prop="'metas.' + index + '.value'"
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
                      @click.prevent="handleRemovePostMeta(meta)"
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
                @click="handleInsertPostMeta"
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
        style="marginRight: 8px"
        @click="handleDraftClick"
        v-if="saveDraftButton"
        :loading="draftSaving"
      >保存草稿</a-button>
      <a-button
        @click="handlePublishClick"
        type="primary"
        :loading="saving"
      > {{ selectedPost.id?'保存':'发布' }} </a-button>
    </div>
  </a-drawer>
</template>
<script>
import { mixin, mixinDevice } from '@/utils/mixin.js'
import moment from 'moment'
import CategoryTree from './CategoryTree'
import CategorySelectTree from './CategorySelectTree'
import TagSelect from './TagSelect'
import AttachmentSelectDrawer from '../../attachment/components/AttachmentSelectDrawer'
import { mapGetters } from 'vuex'
import categoryApi from '@/api/category'
import postApi from '@/api/post'
import themeApi from '@/api/theme'
export default {
  name: 'PostSettingDrawer',
  mixins: [mixin, mixinDevice],
  components: {
    CategoryTree,
    CategorySelectTree,
    TagSelect,
    AttachmentSelectDrawer
  },
  data() {
    return {
      thumbDrawerVisible: false,
      categoryFormVisible: false,
      advancedVisible: false,
      settingLoading: true,
      selectedPost: this.post,
      selectedTagIds: this.tagIds,
      selectedCategoryIds: this.categoryIds,
      categories: [],
      categoryToCreate: {},
      customTpls: [],
      saving: false,
      draftSaving: false
    }
  },
  props: {
    post: {
      type: Object,
      required: true
    },
    tagIds: {
      type: Array,
      required: true
    },
    categoryIds: {
      type: Array,
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
    post(val) {
      this.selectedPost = val
    },
    selectedPost(val) {
      this.$emit('onRefreshPost', val)
    },
    tagIds(val) {
      this.selectedTagIds = val
    },
    selectedTagIds(val) {
      this.$emit('onRefreshTagIds', val)
    },
    categoryIds(val) {
      this.selectedCategoryIds = val
    },
    selectedCategoryIds(val) {
      this.$emit('onRefreshCategoryIds', val)
    },
    selectedMetas(val) {
      this.$emit('onRefreshPostMetas', val)
    },
    visible: function(newValue, oldValue) {
      if (newValue) {
        this.loadSkeleton()
        this.loadCategories()
        this.loadPresetMetasField()
        this.loadCustomTpls()
      }
    }
  },
  computed: {
    selectedMetas() {
      // 不能将selectedMetas直接定义在data里
      // 还没有获取到值就渲染视图,可以直接使用metas
      return this.metas
    },
    pickerDefaultValue() {
      if (this.selectedPost.createTime) {
        var date = new Date(this.selectedPost.createTime)
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
    loadCategories() {
      categoryApi.listAll().then(response => {
        this.categories = response.data.data
      })
    },
    loadPresetMetasField() {
      if (this.metas.length <= 0) {
        themeApi.getActivatedTheme().then(response => {
          const fields = response.data.data.postMetaField
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
      themeApi.customPostTpls().then(response => {
        this.customTpls = response.data.data
      })
    },
    handleSelectPostThumb(data) {
      this.selectedPost.thumbnail = encodeURI(data.path)
      this.thumbDrawerVisible = false
    },
    handleRemoveThumb() {
      this.selectedPost.thumbnail = null
    },
    handlerCreateCategory() {
      if (!this.categoryToCreate.name) {
        this.$notification['error']({
          message: '提示',
          description: '分类名称不能为空！'
        })
        return
      }
      categoryApi.create(this.categoryToCreate).then(response => {
        this.loadCategories()
        this.categoryToCreate = {}
        this.toggleCategoryForm()
      })
    },
    toggleCategoryForm() {
      this.categoryFormVisible = !this.categoryFormVisible
    },
    handleDraftClick() {
      this.selectedPost.status = 'DRAFT'
      this.createOrUpdatePost()
    },
    handlePublishClick() {
      if (this.selectedPost.status === 'DRAFT') {
        this.selectedPost.createTime = new Date()
      }
      this.selectedPost.status = 'PUBLISHED'
      this.createOrUpdatePost()
    },
    createOrUpdatePost() {
      if (!this.selectedPost.title) {
        this.$notification['error']({
          message: '提示',
          description: '文章标题不能为空！'
        })
        return
      }
      // Set category ids
      this.selectedPost.categoryIds = this.selectedCategoryIds
      // Set tag ids
      this.selectedPost.tagIds = this.selectedTagIds
      // Set post metas
      this.selectedPost.metas = this.selectedMetas
      if (this.selectedPost.status === 'DRAFT') {
        this.draftSaving = true
      } else {
        this.saving = true
      }
      if (this.selectedPost.id) {
        // Update the post
        postApi
          .update(this.selectedPost.id, this.selectedPost, false)
          .then(response => {
            this.$log.debug('Updated post', response.data.data)

            if (this.selectedPost.status === 'DRAFT') {
              this.$message.success('草稿保存成功！')
            } else {
              this.$message.success('文章更新成功！')
            }

            this.$emit('onSaved', true)
            this.$router.push({ name: 'PostList' })
          })
          .finally(() => {
            this.saving = false
            this.draftSaving = false
          })
      } else {
        // Create the post
        postApi
          .create(this.selectedPost, false)
          .then(response => {
            this.$log.debug('Created post', response.data.data)

            if (this.selectedPost.status === 'DRAFT') {
              this.$message.success('草稿保存成功！')
            } else {
              this.$message.success('文章发布成功！')
            }

            this.$emit('onSaved', true)
            this.$router.push({ name: 'PostList' })
            this.selectedPost = response.data.data
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
    onPostDateChange(value, dateString) {
      this.selectedPost.createTime = value.valueOf()
    },
    onPostDateOk(value) {
      this.selectedPost.createTime = value.valueOf()
    },
    handleRemovePostMeta(item) {
      var index = this.selectedMetas.indexOf(item)
      if (index !== -1) {
        this.selectedMetas.splice(index, 1)
      }
    },
    handleInsertPostMeta() {
      this.selectedMetas.push({
        value: '',
        key: ''
      })
    }
  }
}
</script>
