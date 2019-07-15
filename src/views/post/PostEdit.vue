<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col :span="24">
        <div style="margin-bottom: 16px">
          <a-input
            v-model="postToStage.title"
            v-decorator="['title', { rules: [{ required: true, message: '请输入文章标题' }] }]"
            size="large"
            placeholder="请输入文章标题"
          />
        </div>

        <div id="editor">
          <mavon-editor
            ref="md"
            v-model="postToStage.originalContent"
            :boxShadow="false"
            :toolbars="toolbars"
            :ishljs="true"
            :autofocus="false"
            @imgAdd="pictureUploadHandle"
          />
        </div>
      </a-col>
    </a-row>

    <a-drawer
      title="文章设置"
      :width="isMobile()?'100%':'460'"
      placement="right"
      closable
      @close="()=>this.postSettingVisible=false"
      :visible="postSettingVisible"
    >
      <div class="post-setting-drawer-content">
        <div :style="{ marginBottom: '16px' }">
          <h3 class="post-setting-drawer-title">基本设置</h3>
          <div class="post-setting-drawer-item">
            <a-form layout="vertical">
              <a-form-item
                label="文章路径："
                :help="options.blog_url+'/archives/' + (postToStage.url ? postToStage.url : '{auto_generate}')"
              >
                <a-input v-model="postToStage.url" />
              </a-form-item>
              <a-form-item label="开启评论：">
                <a-radio-group
                  v-model="postToStage.disallowComment"
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
                <a-form-item v-if="categoryForm">
                  <category-select-tree
                    :categories="categories"
                    v-model="categoryToCreate.parentId"
                  />
                </a-form-item>
                <a-form-item v-if="categoryForm">
                  <a-input
                    placeholder="分类名称"
                    v-model="categoryToCreate.name"
                  />
                </a-form-item>
                <a-form-item v-if="categoryForm">
                  <a-input
                    placeholder="分类路径"
                    v-model="categoryToCreate.slugNames"
                  />
                </a-form-item>
                <a-form-item>
                  <a-button
                    type="primary"
                    style="marginRight: 8px"
                    v-if="categoryForm"
                    @click="handlerCreateCategory"
                  >保存</a-button>
                  <a-button
                    type="dashed"
                    style="marginRight: 8px"
                    v-if="!categoryForm"
                    @click="toggleCategoryForm"
                  >新增</a-button>
                  <a-button
                    v-if="categoryForm"
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
                  v-model="postToStage.summary"
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
            <div class="post-thum">
              <img
                class="img"
                :src="postToStage.thumbnail || '//i.loli.net/2019/05/05/5ccf007c0a01d.png'"
                @click="()=>this.thumDrawerVisible=true"
              >
              <a-button
                class="post-thum-remove"
                type="dashed"
                @click="handlerRemoveThumb"
              >移除</a-button>
            </div>
          </div>
        </div>
        <a-divider class="divider-transparent" />
      </div>
      <AttachmentSelectDrawer
        v-model="thumDrawerVisible"
        @listenToSelect="handleSelectPostThumb"
        :drawerWidth="460"
      />
      <div class="bottom-control">
        <a-button
          style="marginRight: 8px"
          @click="handleDraftClick"
        >保存草稿</a-button>
        <a-button
          @click="handlePublishClick"
          type="primary"
        >发布</a-button>
      </div>
    </a-drawer>

    <AttachmentDrawer v-model="attachmentDrawerVisible" />

    <footer-tool-bar :style="{ width: isSideMenu() && isDesktop() ? `calc(100% - ${sidebarOpened ? 256 : 80}px)` : '100%'}">
      <a-button
        type="primary"
        @click="()=>this.postSettingVisible = true"
      >发布</a-button>
      <a-button
        type="dashed"
        @click="()=>this.attachmentDrawerVisible = true"
        style="margin-left: 8px;"
      >附件库</a-button>
    </footer-tool-bar>
  </div>
</template>

<script>
import CategoryTree from './components/CategoryTree'
import TagSelect from './components/TagSelect'
import { mavonEditor } from 'mavon-editor'
import AttachmentDrawer from '../attachment/components/AttachmentDrawer'
import AttachmentSelectDrawer from '../attachment/components/AttachmentSelectDrawer'
import CategorySelectTree from './components/CategorySelectTree'
import FooterToolBar from '@/components/FooterToolbar'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import { toolbars } from '@/core/const'
import 'mavon-editor/dist/css/index.css'
import categoryApi from '@/api/category'
import postApi from '@/api/post'
import optionApi from '@/api/option'
import attachmentApi from '@/api/attachment'
export default {
  components: {
    TagSelect,
    mavonEditor,
    CategoryTree,
    FooterToolBar,
    AttachmentDrawer,
    AttachmentSelectDrawer,
    CategorySelectTree
  },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      toolbars,
      wrapperCol: {
        xl: { span: 24 },
        sm: { span: 24 },
        xs: { span: 24 }
      },
      attachmentDrawerVisible: false,
      postSettingVisible: false,
      thumDrawerVisible: false,
      categoryForm: false,
      categories: [],
      selectedCategoryIds: [],
      selectedTagIds: [],
      postToStage: {},
      categoryToCreate: {},
      timer: null,
      options: [],
      keys: ['blog_url']
    }
  },
  created() {
    this.loadCategories()
    this.loadOptions()
    clearInterval(this.timer)
    this.timer = null
    this.autoSaveTimer()
  },
  destroyed: function() {
    clearInterval(this.timer)
    this.timer = null
  },
  beforeRouteLeave(to, from, next) {
    if (this.timer !== null) {
      clearInterval(this.timer)
    }
    // Auto save the post
    this.autoSavePost()
    next()
  },
  beforeRouteEnter(to, from, next) {
    // Get post id from query
    const postId = to.query.postId

    next(vm => {
      if (postId) {
        postApi.get(postId).then(response => {
          const post = response.data.data
          vm.postToStage = post
          vm.selectedTagIds = post.tagIds
          vm.selectedCategoryIds = post.categoryIds
        })
      }
    })
  },
  methods: {
    loadCategories() {
      categoryApi.listAll().then(response => {
        this.categories = response.data.data
      })
    },
    loadOptions() {
      optionApi.listAll(this.keys).then(response => {
        this.options = response.data.data
      })
    },
    createOrUpdatePost(createSuccess, updateSuccess, autoSave) {
      // Set category ids
      this.postToStage.categoryIds = this.selectedCategoryIds
      // Set tag ids
      this.postToStage.tagIds = this.selectedTagIds

      if (this.postToStage.id) {
        // Update the post
        postApi.update(this.postToStage.id, this.postToStage, autoSave).then(response => {
          this.$log.debug('Updated post', response.data.data)
          if (updateSuccess) {
            updateSuccess()
          }
        })
      } else {
        // Create the post
        postApi.create(this.postToStage, autoSave).then(response => {
          this.$log.debug('Created post', response.data.data)
          if (createSuccess) {
            createSuccess()
          }
          this.postToStage = response.data.data
        })
      }
    },
    savePost() {
      this.createOrUpdatePost(
        () => this.$message.success('文章创建成功'),
        () => this.$message.success('文章更新成功'),
        false
      )
    },
    autoSavePost() {
      if (this.postToStage.title != null && this.postToStage.originalContent != null) {
        this.createOrUpdatePost(null, null, true)
      }
    },
    toggleCategoryForm() {
      this.categoryForm = !this.categoryForm
    },
    handlePublishClick() {
      this.postToStage.status = 'PUBLISHED'
      this.savePost()
    },
    handleDraftClick() {
      this.postToStage.status = 'DRAFT'
      this.savePost()
    },
    handlerRemoveThumb() {
      this.postToStage.thumbnail = null
    },
    handlerCreateCategory() {
      categoryApi.create(this.categoryToCreate).then(response => {
        this.loadCategories()
        this.categoryToCreate = {}
      })
    },
    handleSelectPostThumb(data) {
      this.postToStage.thumbnail = data.path
      this.thumDrawerVisible = false
    },
    autoSaveTimer() {
      if (this.timer == null) {
        this.timer = setInterval(() => {
          this.autoSavePost()
        }, 15000)
      }
    },
    pictureUploadHandle(pos, $file) {
      var formdata = new FormData()
      formdata.append('file', $file)
      attachmentApi.upload(formdata).then((response) => {
        var responseObject = response.data

        if (responseObject.status === 200) {
          var MavonEditor = this.$refs.md
          MavonEditor.$img2Url(pos, responseObject.data.path)
          this.$message.success('图片上传成功')
        } else {
          this.$message.error('图片上传失败：' + responseObject.message)
        }
      })
    }
  }
}
</script>

<style lang="less" scoped>
.v-note-wrapper {
  z-index: 1000;
  min-height: 580px;
}
</style>
