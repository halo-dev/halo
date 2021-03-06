<template>
  <a-drawer
    title="文章设置"
    :width="isMobile() ? '100%' : '480'"
    placement="right"
    closable
    destroyOnClose
    @close="onClose"
    :visible="visible"
    :afterVisibleChange="handleAfterVisibleChanged"
  >
    <div class="post-setting-drawer-content">
      <div class="mb-4">
        <h3 class="post-setting-drawer-title">基本设置</h3>
        <div class="post-setting-drawer-item">
          <a-form layout="vertical">
            <a-form-item label="文章标题：" v-if="needTitle">
              <a-input v-model="selectedPost.title" />
            </a-form-item>
            <a-form-item label="文章别名：" :help="fullPath">
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
              <a-radio-group v-model="selectedPost.disallowComment" :defaultValue="false">
                <a-radio :value="false">开启</a-radio>
                <a-radio :value="true">关闭</a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item label="是否置顶：">
              <a-radio-group v-model="selectedPost.topPriority" :defaultValue="0">
                <a-radio :value="1">是</a-radio>
                <a-radio :value="0">否</a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item label="自定义模板：" v-if="customTpls.length > 0">
              <a-select v-model="selectedPost.template">
                <a-select-option key="" value="">无</a-select-option>
                <a-select-option v-for="tpl in customTpls" :key="tpl" :value="tpl">{{ tpl }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-form>
        </div>
      </div>
      <a-divider />

      <div class="mb-4">
        <h3 class="post-setting-drawer-title">分类目录</h3>
        <div class="post-setting-drawer-item">
          <a-form layout="vertical">
            <a-form-item>
              <category-tree v-model="selectedCategoryIds" :categories="categories" />
            </a-form-item>
            <a-form-item v-if="categoryFormVisible">
              <category-select-tree :categories="categories" v-model="categoryToCreate.parentId" />
            </a-form-item>
            <a-form-item v-if="categoryFormVisible">
              <a-input placeholder="分类名称" v-model="categoryToCreate.name" />
            </a-form-item>
            <a-form-item v-if="categoryFormVisible">
              <a-input placeholder="分类路径" v-model="categoryToCreate.slug" />
            </a-form-item>
            <a-form-item>
              <a-space>
                <a-button type="primary" v-if="categoryFormVisible" @click="handlerCreateCategory">保存</a-button>
                <a-button type="dashed" v-if="!categoryFormVisible" @click="categoryFormVisible = true">新增</a-button>
                <a-button v-if="categoryFormVisible" @click="categoryFormVisible = false">取消</a-button>
              </a-space>
            </a-form-item>
          </a-form>
        </div>
      </div>
      <a-divider />

      <div class="mb-4">
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

      <div class="mb-4">
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

      <div class="mb-4">
        <h3 class="post-setting-drawer-title">封面图</h3>
        <div class="post-setting-drawer-item">
          <div class="post-thumb">
            <img
              class="img"
              :src="selectedPost.thumbnail || '/images/placeholder.jpg'"
              @click="thumbDrawerVisible = true"
            />

            <a-form layout="vertial">
              <a-form-item>
                <a-input v-model="selectedPost.thumbnail" placeholder="点击封面图选择图片，或者输入外部链接"></a-input>
              </a-form-item>
            </a-form>

            <a-button class="post-thumb-remove" type="dashed" @click="selectedPost.thumbnail = null">移除</a-button>
          </div>
        </div>
      </div>
      <a-divider class="divider-transparent" />
    </div>
    <AttachmentSelectDrawer v-model="thumbDrawerVisible" @listenToSelect="handleSelectPostThumb" :drawerWidth="480" />

    <a-drawer
      title="高级设置"
      :width="isMobile() ? '100%' : '480'"
      placement="right"
      closable
      destroyOnClose
      @close="advancedVisible = false"
      :visible="advancedVisible"
    >
      <div class="post-setting-drawer-content">
        <div class="mb-4">
          <h3 class="post-setting-drawer-title">加密设置</h3>
          <div class="post-setting-drawer-item">
            <a-form layout="vertical">
              <a-form-item label="访问密码：">
                <a-input-password v-model="selectedPost.password" autocomplete="new-password" />
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-divider />
        <div class="mb-4">
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
        <div class="mb-4">
          <h3 class="post-setting-drawer-title">元数据</h3>
          <a-form layout="vertical">
            <a-form-item v-for="(meta, index) in selectedMetas" :key="index" :prop="'metas.' + index + '.value'">
              <a-row :gutter="5">
                <a-col :span="12">
                  <a-input v-model="meta.key"><i slot="addonBefore">K</i></a-input>
                </a-col>
                <a-col :span="12">
                  <a-input v-model="meta.value">
                    <i slot="addonBefore">V</i>
                    <a href="javascript:void(0);" slot="addonAfter" @click.prevent="handleRemovePostMeta(meta)">
                      <a-icon type="close" />
                    </a>
                  </a-input>
                </a-col>
              </a-row>
            </a-form-item>
            <a-form-item>
              <a-button type="dashed" @click="handleInsertPostMeta">新增</a-button>
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
          type="danger"
          v-if="saveDraftButton"
          @click="handleDraftClick"
          @callback="handleSavedCallback"
          :loading="draftSaving"
          :errored="draftSavedErrored"
          text="保存草稿"
          loadedText="保存成功"
          erroredText="保存失败"
        ></ReactiveButton>
        <ReactiveButton
          @click="handlePublishClick()"
          @callback="handleSavedCallback"
          :loading="saving"
          :errored="savedErrored"
          :text="`${selectedPost.id ? '保存' : '发布'}`"
          :loadedText="`${selectedPost.id ? '保存' : '发布'}成功`"
          :erroredText="`${selectedPost.id ? '保存' : '发布'}失败`"
        ></ReactiveButton>
      </a-space>
    </div>
  </a-drawer>
</template>
<script>
// components
import CategoryTree from './CategoryTree'
import CategorySelectTree from './CategorySelectTree'
import TagSelect from './TagSelect'

// libs
import { mixin, mixinDevice } from '@/mixins/mixin.js'
import { datetimeFormat } from '@/utils/datetime'
import pinyin from 'tiny-pinyin'
import { mapGetters } from 'vuex'

// apis
import categoryApi from '@/api/category'
import postApi from '@/api/post'
import themeApi from '@/api/theme'

export default {
  name: 'PostSettingDrawer',
  mixins: [mixin, mixinDevice],
  components: {
    CategoryTree,
    CategorySelectTree,
    TagSelect
  },
  data() {
    return {
      thumbDrawerVisible: false,
      categoryFormVisible: false,
      advancedVisible: false,
      selectedPost: this.post,
      selectedTagIds: this.tagIds,
      selectedCategoryIds: this.categoryIds,
      categories: [],
      categoryToCreate: {},
      customTpls: [],
      saving: false,
      savedErrored: false,
      draftSaving: false,
      draftSavedErrored: false
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
    }
  },
  computed: {
    ...mapGetters(['options']),
    selectedMetas() {
      return this.metas
    },
    pickerDefaultValue() {
      if (this.selectedPost.createTime) {
        var date = new Date(this.selectedPost.createTime)
        return datetimeFormat(date, 'YYYY-MM-DD HH:mm:ss')
      }
      return datetimeFormat(new Date(), 'YYYY-MM-DD HH:mm:ss')
    },
    fullPath() {
      const permalinkType = this.options.post_permalink_type
      const blogUrl = this.options.blog_url
      const archivesPrefix = this.options.archives_prefix
      const pathSuffix = this.options.path_suffix ? this.options.path_suffix : ''
      switch (permalinkType) {
        case 'DEFAULT':
          return `${blogUrl}/${archivesPrefix}/${
            this.selectedPost.slug ? this.selectedPost.slug : '{slug}'
          }${pathSuffix}`
        case 'YEAR':
          return `${blogUrl}${datetimeFormat(
            this.selectedPost.createTime ? this.selectedPost.createTime : new Date(),
            '/YYYY/'
          )}${this.selectedPost.slug ? this.selectedPost.slug : '{slug}'}${pathSuffix}`
        case 'DATE':
          return `${blogUrl}${datetimeFormat(
            this.selectedPost.createTime ? this.selectedPost.createTime : new Date(),
            '/YYYY/MM/'
          )}${this.selectedPost.slug ? this.selectedPost.slug : '{slug}'}${pathSuffix}`
        case 'DAY':
          return `${blogUrl}${datetimeFormat(
            this.selectedPost.createTime ? this.selectedPost.createTime : new Date(),
            '/YYYY/MM/DD/'
          )}${this.selectedPost.slug ? this.selectedPost.slug : '{slug}'}${pathSuffix}`
        case 'ID':
          return `${blogUrl}/?p=${this.selectedPost.id ? this.selectedPost.id : '{id}'}`
        case 'ID_SLUG':
          return `${blogUrl}/${archivesPrefix}/${this.selectedPost.id ? this.selectedPost.id : '{id}'}${pathSuffix}`
        default:
          return ''
      }
    }
  },
  methods: {
    handleAfterVisibleChanged(visible) {
      if (visible) {
        this.handleListCategories()
        this.handleListPresetMetasField()
        this.handleListCustomTpls()
        this.handleSetPinyinSlug()
      }
    },
    handleListCategories() {
      categoryApi.listAll().then(response => {
        this.categories = response.data.data
      })
    },
    handleListPresetMetasField() {
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
    handleListCustomTpls() {
      themeApi.customPostTpls().then(response => {
        this.customTpls = response.data.data
      })
    },
    handleSelectPostThumb(data) {
      this.selectedPost.thumbnail = encodeURI(data.path)
      this.thumbDrawerVisible = false
    },
    handlerCreateCategory() {
      if (!this.categoryToCreate.name) {
        this.$notification['error']({
          message: '提示',
          description: '分类名称不能为空！'
        })
        return
      }
      categoryApi
        .create(this.categoryToCreate)
        .then(() => {
          this.categoryToCreate = {}
          this.categoryFormVisible = false
        })
        .finally(() => {
          this.handleListCategories()
        })
    },
    handleDraftClick() {
      this.selectedPost.status = 'DRAFT'
      this.createOrUpdatePost()
    },
    handlePublishClick() {
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
          .catch(() => {
            if (this.selectedPost.status === 'DRAFT') {
              this.draftSavedErrored = true
            } else {
              this.savedErrored = true
            }
          })
          .finally(() => {
            setTimeout(() => {
              this.saving = false
              this.draftSaving = false
            }, 400)
          })
      } else {
        // Create the post
        postApi
          .create(this.selectedPost, false)
          .catch(() => {
            if (this.selectedPost.status === 'DRAFT') {
              this.draftSavedErrored = true
            } else {
              this.savedErrored = true
            }
          })
          .then(response => {
            this.selectedPost = response.data.data
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
      if (this.draftSavedErrored || this.savedErrored) {
        this.draftSavedErrored = false
        this.savedErrored = false
      } else {
        this.$emit('onSaved', true)
        this.$router.push({ name: 'PostList' })
      }
    },
    onClose() {
      this.$emit('close', false)
    },
    onPostDateChange(value) {
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
    },
    handleSetPinyinSlug() {
      if (this.selectedPost.title && this.selectedPost.title !== '' && !this.selectedPost.id) {
        if (pinyin.isSupported()) {
          let result = ''
          const tokens = pinyin.parse(this.selectedPost.title)
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
          this.$set(this.selectedPost, 'slug', result)
        }
      }
    }
  }
}
</script>
