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
            v-model="postToStage.originalContent"
            :boxShadow="false"
            :toolbars="toolbars"
            :ishljs="true"
          />
        </div>
      </a-col>

      <a-col
        :xl="24"
        :lg="24"
        :md="24"
        :sm="24"
        :xs="24"
      >
        <a-drawer
          title="文章设置"
          :width="isMobile()?'100%':'460'"
          closable
          @close="onClose"
          :visible="visible"
        >
          <div class="post-setting-drawer-content">
            <div :style="{ marginBottom: '16px' }">
              <h3 class="post-setting-drawer-title">基本设置</h3>
              <div class="post-setting-drawer-item">
                <a-form layout="vertical">
                  <a-form-item
                    label="文章路径："
                    :help="'/archives/' + (postToStage.url ? postToStage.url : '{auto_generate}')"
                  >
                    <a-input v-model="postToStage.url" />
                  </a-form-item>
                  <a-form-item label="文章密码：">
                    <a-input
                      type="password"
                      v-model="postToStage.password"
                    />
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
              <h3 class="post-setting-drawer-title">缩略图</h3>
              <div class="post-setting-drawer-item">
                <div class="post-thum">
                  <img
                    class="img"
                    :src="postToStage.thumbnail || 'https://os.alipayobjects.com/rmsportal/mgesTPFxodmIwpi.png'"
                    @click="handleShowThumbDrawer"
                  >
                  <a-button
                    class="post-thum-remove"
                    type="dashed"
                    @click="handlerRemoveThumb"
                  >移除</a-button>
                </div>
              </div>
            </div>
            <a-divider />
          </div>
          <AttachmentSelectDrawer
            v-model="thumDrawerVisible"
            @listenToSelect="handleSelectPostThumb"
            :drawerWidth="460"
          />
          <a-divider />
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
      </a-col>
    </a-row>

    <AttachmentDrawer v-model="attachmentDrawerVisible" />

    <footer-tool-bar :style="{ width: isSideMenu() && isDesktop() ? `calc(100% - ${sidebarOpened ? 256 : 80}px)` : '100%'}">
      <a-button
        type="primary"
        @click="handleShowDrawer"
      >发布</a-button>
      <a-button
        type="dashed"
        @click="handleShowAttachDrawer"
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
import tagApi from '@/api/tag'
import categoryApi from '@/api/category'
import postApi from '@/api/post'
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
      visible: false,
      thumDrawerVisible: false,
      categoryForm: false,
      tags: [],
      categories: [],
      selectedCategoryIds: [],
      selectedTagIds: [],
      postToStage: {},
      categoryToCreate: {},
      timer: null
    }
  },
  created() {
    this.loadTags()
    this.loadCategories()
    clearInterval(this.timer)
    this.timer = null
    this.autoSaveTimer()
  },
  destroyed: function() {
    clearInterval(this.timer)
    this.timer = null
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
    loadTags() {
      tagApi.listAll(true).then(response => {
        this.tags = response.data.data
      })
    },
    loadCategories() {
      categoryApi.listAll().then(response => {
        this.categories = response.data.data
      })
    },
    createOrUpdatePost() {
      // Set category ids
      this.postToStage.categoryIds = this.selectedCategoryIds
      // Set tag ids
      this.postToStage.tagIds = this.selectedTagIds

      if (this.postToStage.id) {
        // Update the post
        postApi.update(this.postToStage.id, this.postToStage).then(response => {
          this.$log.debug('Updated post', response.data.data)
          this.$message.success('文章更新成功')
        })
      } else {
        // Create the post
        postApi.create(this.postToStage).then(response => {
          this.$log.debug('Created post', response.data.data)
          this.$message.success('文章创建成功')
          this.postToStage = response.data.data
        })
      }
    },
    handleShowDrawer() {
      this.visible = true
    },
    handleShowAttachDrawer() {
      this.attachmentDrawerVisible = true
    },
    handleShowThumbDrawer() {
      this.thumDrawerVisible = true
    },
    toggleCategoryForm() {
      this.categoryForm = !this.categoryForm
    },
    handlePublishClick() {
      this.postToStage.status = 'PUBLISHED'
      this.createOrUpdatePost()
    },
    handleDraftClick() {
      this.postToStage.status = 'DRAFT'
      this.createOrUpdatePost()
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
    onClose() {
      this.visible = false
    },
    handleSelectPostThumb(data) {
      this.postToStage.thumbnail = data.path
      this.thumDrawerVisible = false
    },
    autoSaveTimer() {
      if (this.timer == null) {
        this.timer = setInterval(() => {
          if (this.postToStage.title != null && this.postToStage.originalContent != null) {
            this.postToStage.categoryIds = this.selectedCategoryIds
            this.postToStage.tagIds = this.selectedTagIds

            if (this.postToStage.id) {
              postApi.update(this.postToStage.id, this.postToStage).then(response => {
                this.$log.debug('Auto updated post', response.data.data)
              })
            } else {
              postApi.create(this.postToStage).then(response => {
                this.$log.debug('Auto saved post', response.data.data)
                this.postToStage = response.data.data
              })
            }
          }
        }, 15000)
      }
    }
  }
}
</script>

<style lang="less" scoped>
.v-note-wrapper {
  z-index: 1000;
  min-height: 580px;
}

.post-thum {
  .img {
    width: 100%;
    cursor: pointer;
    border-radius: 4px;
  }
  .post-thum-remove {
    margin-top: 16px;
  }
}
</style>
