<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col :xl="24" :lg="24" :md="24" :sm="24" :xs="24">
        <div style="margin-bottom: 16px">
          <a-input
            v-model="postToStage.title"
            v-decorator="['title', { rules: [{ required: true, message: '请输入页面标题' }] }]"
            size="large"
            placeholder="请输入页面标题"
          />
        </div>
        <div id="editor">
          <mavon-editor v-model="postToStage.originalContent" :boxShadow="false" :ishljs="true"/>
        </div>
      </a-col>

      <a-col :xl="24" :lg="24" :md="24" :sm="24" :xs="24">
        <a-drawer
          title="页面设置"
          :width="drawerWidth"
          :closable="true"
          @close="onClose"
          :visible="visible"
        >
          <div class="post-setting-drawer-content">
            <div :style="{ marginBottom: '16px' }">
              <h3 class="post-setting-drawer-title">基本设置</h3>
              <div class="post-setting-drawer-item">
                <a-form layout="vertical">
                  <a-form-item
                    label="页面路径："
                    :help="'https://localhost:8090/p/'+ (postToStage.url ? postToStage.url : '{auto_generate}')"
                  >
                    <a-input v-model="postToStage.url"/>
                  </a-form-item>
                  <a-form-item label="页面密码：">
                    <a-input type="password" v-model="postToStage.password"/>
                  </a-form-item>
                  <a-form-item label="是否关闭评论：">
                    <a-radio-group v-model="postToStage.disallowComment" :defaultValue="false">
                      <a-radio :value="false">开启</a-radio>
                      <a-radio :value="true">关闭</a-radio>
                    </a-radio-group>
                  </a-form-item>
                  <a-form-item label="自定义模板：">
                    <a-select v-model="postToStage.template">
                      <a-select-option v-for="tpl in customTpls" :key="tpl" :value="tpl">{{ tpl }}</a-select-option>
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
                  <img class="img" src="https://os.alipayobjects.com/rmsportal/mgesTPFxodmIwpi.png">
                </div>
              </div>
            </div>
            <a-divider/>
          </div>
          <div class="postControl">
            <a-button style="marginRight: 8px" @click="handleDraftClick">保存草稿</a-button>
            <a-button type="primary" @click="handlePublishClick">{{ publishText }}</a-button>
          </div>
        </a-drawer>
      </a-col>
    </a-row>

    <footer-tool-bar
      :style="{ width: isSideMenu() && isDesktop() ? `calc(100% - ${sidebarOpened ? 256 : 80}px)` : '100%'}"
    >
      <a-button type="primary" @click="showDrawer">发布</a-button>
      <a-button type="dashed" style="margin-left: 8px;">附件库</a-button>
    </footer-tool-bar>
  </div>
</template>

<script>
import { mavonEditor } from 'mavon-editor'
import FooterToolBar from '@/components/FooterToolbar'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import 'mavon-editor/dist/css/index.css'
import postApi from '@/api/post'
import themeApi from '@/api/theme'

export default {
  name: 'Editor',
  components: {
    mavonEditor,
    FooterToolBar
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
      drawerWidth: '460',
      postUrl: 'hello-world',
      customTpls: [],
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
    this.loadCustomTpls()
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
    loadCustomTpls() {
      themeApi.customTpls().then(response => {
        this.customTpls = response.data.data
      })
    },
    showDrawer() {
      this.visible = true
    },
    handlePublishClick() {
      this.postToStage.status = 'PUBLISHED'
      this.createOrUpdatePost()
    },
    handleDraftClick() {
      this.postToStage.status = 'DRAFT'
      this.createOrUpdatePost()
    },
    createOrUpdatePost() {
      if (this.postToStage.id) {
        // Update the post
        postApi.update(this.postToStage.id, this.postToStage).then(response => {
          this.$log.debug('Updated post', response.data.data)
          this.$message.success('页面更新成功')
        })
      } else {
        // Create the post
        postApi.create(this.postToStage).then(response => {
          this.$log.debug('Created post', response.data.data)
          this.$message.success('页面创建成功')
          this.postToStage = response.data.data
        })
      }
    },
    onClose() {
      this.visible = false
    }
  }
}
</script>
<style scoped>
.v-note-wrapper {
  z-index: 1000;
  min-height: 540px;
}

.ant-form-vertical .ant-form-item {
  padding-bottom: 0;
}

.postControl {
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
}
</style>
