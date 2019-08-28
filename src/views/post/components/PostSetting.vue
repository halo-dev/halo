<template>
  <a-drawer
    title="文章设置"
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
              <a-form-item
                label="文章路径："
                :help="options.blog_url+'/archives/' + (selectedPost.url ? selectedPost.url : '{auto_generate}')"
              >
                <a-input v-model="selectedPost.url" />
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
            </a-form>
          </div>
        </div>
        <a-divider />

        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">分类目录</h3>
          <div class="post-setting-drawer-item">
            <category-tree
              v-model="selectedCategoryIds"
              :categories="categories"
            />
            <div>
              <a-form layout="vertical">
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
                    v-model="categoryToCreate.slugNames"
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
                  :autosize="{ minRows: 5 }"
                  v-model="selectedPost.summary"
                  placeholder="不填写则会自动生成"
                />
              </a-form-item>
            </a-form>
          </div>
        </div>
        <a-divider />

        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">缩略图</h3>
          <div class="post-setting-drawer-item">
            <div class="post-thumb">
              <img
                class="img"
                :src="selectedPost.thumbnail || '//i.loli.net/2019/05/05/5ccf007c0a01d.png'"
                @click="()=>this.thumbDrawerVisible=true"
              >
              <a-button
                class="post-thumb-remove"
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
      @listenToSelect="handleSelectPostThumb"
      :drawerWidth="460"
    />
    <div class="bottom-control">
      <a-button
        style="marginRight: 8px"
        @click="handleDraftClick"
        v-if="saveDraftButton"
      >保存草稿</a-button>
      <a-button
        @click="handlePublishClick"
        type="primary"
        v-if="savePublishButton"
      >发布</a-button>
      <a-button
        @click="handlePublishClick"
        type="primary"
        v-if="saveButton"
      >保存</a-button>
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
import optionApi from '@/api/option'
import categoryApi from '@/api/category'
import postApi from '@/api/post'
export default {
  name: 'PostSetting',
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
      settingLoading: true,
      options: [],
      keys: ['blog_url'],
      selectedPost: this.post,
      selectedTagIds: this.tagIds,
      selectedCategoryIds: this.categoryIds,
      categories: [],
      categoryToCreate: {}
    }
  },
  // TODO delete this commented code
  // model: {
  //   prop: 'visible',
  //   event: 'close'
  // },
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
    visible: {
      type: Boolean,
      required: false,
      default: true
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
    savePublishButton: {
      type: Boolean,
      required: false,
      default: true
    },
    saveButton: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  created() {
    this.loadSkeleton()
    this.loadOptions()
    this.loadCategories()
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
    visible: function(newValue, oldValue) {
      if (newValue) {
        this.loadSkeleton()
      }
    }
  },
  computed: {
    pickerDefaultValue() {
      if (this.selectedPost.createTime) {
        var date = new Date(this.selectedPost.createTime)
        return moment(date, 'YYYY-MM-DD HH:mm:ss')
      }
      return moment(new Date(), 'YYYY-MM-DD HH:mm:ss')
    }
  },
  methods: {
    loadSkeleton() {
      this.settingLoading = true
      setTimeout(() => {
        this.settingLoading = false
      }, 500)
    },
    loadOptions() {
      optionApi.listAll(this.keys).then(response => {
        this.options = response.data.data
      })
    },
    loadCategories() {
      categoryApi.listAll().then(response => {
        this.categories = response.data.data
      })
    },
    handleSelectPostThumb(data) {
      this.selectedPost.thumbnail = encodeURI(data.path)
      this.thumbDrawerVisible = false
    },
    handlerRemoveThumb() {
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
      })
    },
    toggleCategoryForm() {
      this.categoryFormVisible = !this.categoryFormVisible
    },
    handleDraftClick() {
      this.selectedPost.status = 'DRAFT'
      this.savePost()
    },
    handlePublishClick() {
      this.selectedPost.status = 'PUBLISHED'
      this.savePost()
    },
    savePost() {
      this.createOrUpdatePost(
        () => this.$message.success('文章发布成功'),
        () => this.$message.success('文章发布成功'),
        false
      )
    },
    createOrUpdatePost(createSuccess, updateSuccess, autoSave) {
      if (!this.selectedPost.title) {
        this.$notification['error']({
          message: '提示',
          description: '文章标题不能为空！'
        })
        return
      }
      if (!this.selectedPost.originalContent) {
        this.$notification['error']({
          message: '提示',
          description: '文章内容不能为空！'
        })
        return
      }
      // Set category ids
      this.selectedPost.categoryIds = this.selectedCategoryIds
      // Set tag ids
      this.selectedPost.tagIds = this.selectedTagIds

      if (this.selectedPost.id) {
        // Update the post
        postApi.update(this.selectedPost.id, this.selectedPost, autoSave).then(response => {
          this.$log.debug('Updated post', response.data.data)
          if (updateSuccess) {
            updateSuccess()
          }
        })
      } else {
        // Create the post
        postApi.create(this.selectedPost, autoSave).then(response => {
          this.$log.debug('Created post', response.data.data)
          if (createSuccess) {
            createSuccess()
          }
          this.selectedPost = response.data.data
        })
      }
    },
    onClose() {
      this.$emit('close', false)
    },
    onPostDateChange(value, dateString) {
      this.selectedPost.createTime = value.valueOf()
    },
    onPostDateOk(value) {
      this.selectedPost.createTime = value.valueOf()
    }
  }
}
</script>
<style lang="less">
</style>
