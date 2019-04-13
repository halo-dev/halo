<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col :xl="24" :lg="24" :md="24" :sm="24" :xs="24">
        <a-card>
          <div style="margin-bottom: 16px">
            <a-input
              v-model="postToStage.title"
              v-decorator="['title', { rules: [{ required: true, message: '请输入文章标题' }] }]"
              size="large"
              placeholder="请输入文章标题"
            />
          </div>
          <a-button type="primary" @click="showDrawer">发布</a-button>
        </a-card>

        <a-card>
          <div id="editor">
            <mavon-editor
              :toolbars="markdownOption"
              v-model="postToStage.originalContent"
              :boxShadow="false"
              :ishljs="true"
            />
          </div>
        </a-card>
      </a-col>

      <a-col :xl="24" :lg="24" :md="24" :sm="24" :xs="24">
        <a-drawer
          title="文章设置"
          :width="drawerWidth"
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
                    <a-input v-model="postToStage.url"/>
                  </a-form-item>
                  <a-form-item label="文章密码：">
                    <a-input type="password" v-model="postToStage.password"/>
                  </a-form-item>
                  <a-form-item label="是否关闭评论：">
                    <a-radio-group v-model="postToStage.disallowComment" :defaultValue="false">
                      <a-radio :value="false">开启</a-radio>
                      <a-radio :value="true">关闭</a-radio>
                    </a-radio-group>
                  </a-form-item>
                </a-form>
              </div>
            </div>
            <a-divider/>

            <div :style="{ marginBottom: '16px' }">
              <h3 class="post-setting-drawer-title">分类目录</h3>
              <div class="post-setting-drawer-item">
                <category-tree v-model="selectedCategoryIds" :categories="categories"/>
              </div>
            </div>
            <a-divider/>

            <div :style="{ marginBottom: '16px' }">
              <h3 class="post-setting-drawer-title">标签</h3>
              <div class="post-setting-drawer-item">
                <a-form layout="vertical">
                  <a-form-item>
                    <a-select
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
                    </a-select>
                  </a-form-item>
                </a-form>
              </div>
            </div>
            <a-divider/>

            <div :style="{ marginBottom: '16px' }">
              <h3 class="post-setting-drawer-title">缩略图</h3>
              <div class="post-setting-drawer-item">
                <div class="post-thum">
                  <img class="img" src="https://os.alipayobjects.com/rmsportal/mgesTPFxodmIwpi.png" @click="showAttachDrawer">
                </div>
              </div>
            </div>
            <a-divider/>
          </div>
          <a-drawer
            title="选择图片"
            width="320"
            closable
            :visible="childDrawerVisible"
            @close="onChildClose"
          >
          </a-drawer>
          <div class="post-control">
            <a-button style="marginRight: 8px" @click="handleDraftClick">保存草稿</a-button>
            <a-button @click="handlePublishClick" type="primary">{{ publishText }}</a-button>
          </div>
        </a-drawer>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import CategoryTree from './components/CategoryTree'
import { mavonEditor } from 'mavon-editor'
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
  readmodel: true, // 沉浸式阅读
  undo: true, // 上一步
  redo: true, // 下一步
  navigation: true, // 导航目录
  subfield: true, // 单双栏模式
  preview: true // 预览
}
export default {
  name: 'Editor',
  components: {
    mavonEditor,
    CategoryTree
  },
  mixins: [mixin, mixinDevice],
  data() {
    return {
      wrapperCol: {
        xl: { span: 24 },
        sm: { span: 24 },
        xs: { span: 24 }
      },
      visible: false,
      childDrawerVisible: false,
      drawerWidth: '460',
      tags: [],
      categories: [],
      selectedCategoryIds: [],
      selectedTagIds: [],
      markdownOption: toolbars,
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
  mounted() {
    if (this.isMobile()) {
      this.drawerWidth = '100%'
    } else {
      this.drawerWidth = '460'
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
      this.childDrawerVisible = true
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
    onChildClose() {
      this.childDrawerVisible = false
    }
  }
}
</script>
<style scoped>
#editor {
  margin: auto;
  width: 100%;
}

.v-note-wrapper {
  z-index: 1000;
}

.ant-card {
  margin-bottom: 16px;
}

.ant-form-vertical .ant-form-item {
  padding-bottom: 0;
}

.post-control {
  position: absolute;
  bottom: 0px;
  width: 100%;
  border-top: 1px solid rgb(232, 232, 232);
  padding: 10px 16px;
  text-align: right;
  left: 0px;
  background: rgb(255, 255, 255);
  border-radius: 0px 0px 4px 4px;
}

.ant-form-vertical .ant-form-item {
  padding-bottom: 0;
}

.post-thum .img {
  width: 100%;
  cursor: pointer;
}

.mavonEditor {
  width: 100%;
  height: 560px;
}
</style>
