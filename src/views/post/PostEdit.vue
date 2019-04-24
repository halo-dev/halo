<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col
        :xl="24"
        :lg="24"
        :md="24"
        :sm="24"
        :xs="24"
      >
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
              </div>
            </div>
            <a-divider />

            <div :style="{ marginBottom: '16px' }">
              <h3 class="post-setting-drawer-title">标签</h3>
              <div class="post-setting-drawer-item">
                <a-form layout="vertical">
                  <a-form-item>
                    <TagSelect v-model="selectedTagIds" />
                    <!-- <a-select
                      v-model="selectedTagIds"
                      allowClear
                      mode="multiple"
                      placeholder="选择或输入标签"
                    >
                      <a-select-option
                        v-for="tag in tags"
                        :key="tag.id"
                        :value="tag.id"
                      >{{ tag.name }}</a-select-option>
                    </a-select> -->
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
                    @click="showThumbDrawer"
                  >
                </div>
              </div>
            </div>
            <a-divider />
          </div>
          <AttachmentSelectDrawer
            v-model="thumDrawerVisible"
            @listenToSelect="selectPostThumb"
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
            >{{ publishText }}</a-button>
          </div>
        </a-drawer>
      </a-col>
    </a-row>

    <AttachmentDrawer v-model="attachmentDrawerVisible" />

    <footer-tool-bar :style="{ width: isSideMenu() && isDesktop() ? `calc(100% - ${sidebarOpened ? 256 : 80}px)` : '100%'}">
      <a-button
        type="primary"
        @click="showDrawer"
      >发布</a-button>
      <a-button
        type="dashed"
        @click="showAttachDrawer"
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
import FooterToolBar from '@/components/FooterToolbar'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import 'mavon-editor/dist/css/index.css'
import tagApi from '@/api/tag'
import categoryApi from '@/api/category'
import postApi from '@/api/post'
const toolbars = {
  bold: true, // 粗体
  italic: true, // 斜体
  header: true, // 标题
  underline: true, // 下划线
  strikethrough: true, // 中划线
  quote: true, // 引用
  ol: true, // 有序列表
  ul: true, // 无序列表
  link: true, // 链接
  imagelink: true, // 图片链接
  code: true, // code
  table: true, // 表格
  fullscreen: true, // 全屏编辑
  readmodel: true, // 沉浸式阅读
  htmlcode: true, // 展示html源码
  undo: true, // 上一步
  redo: true, // 下一步
  trash: true, // 清空
  navigation: true, // 导航目录
  subfield: true, // 单双栏模式
  preview: true // 预览
}
export default {
  components: {
    TagSelect,
    mavonEditor,
    CategoryTree,
    FooterToolBar,
    AttachmentDrawer,
    AttachmentSelectDrawer
  },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      wrapperCol: {
        xl: { span: 24 },
        sm: { span: 24 },
        xs: { span: 24 }
      },
      attachmentDrawerVisible: false,
      visible: false,
      thumDrawerVisible: false,
      tags: [],
      categories: [],
      selectedCategoryIds: [],
      selectedTagIds: [],
      toolbars: toolbars,
      postToStage: {}
    }
  },
  computed: {
    publishText() {
      if (this.postToStage.id) {
        return '更新并发布'
      }
      return '创建并发布'
    }
  },
  created() {
    this.loadTags()
    this.loadCategories()
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
    showDrawer() {
      this.visible = true
    },
    showAttachDrawer() {
      this.attachmentDrawerVisible = true
    },
    showThumbDrawer() {
      this.thumDrawerVisible = true
    },
    handlePublishClick() {
      this.postToStage.status = 'PUBLISHED'
      this.createOrUpdatePost()
    },
    handleDraftClick() {
      this.postToStage.status = 'DRAFT'
      this.createOrUpdatePost()
    },
    onClose() {
      this.visible = false
    },
    selectPostThumb(data) {
      this.postToStage.thumbnail = data.path
      this.thumDrawerVisible = false
    }
  }
}
</script>

<style scoped>
.v-note-wrapper {
  z-index: 1000;
  min-height: 580px;
}

.post-thum .img {
  width: 100%;
  cursor: pointer;
}
</style>
