<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col :xl="24" :lg="24" :md="24" :sm="24" :xs="24">
        <a-card>
          <div style="margin-bottom: 16px">
            <a-input
              v-decorator="['title', { rules: [{ required: true, message: '请输入文章标题' }] }]"
              size="large"
              placeholder="请输入文章标题"
            />
          </div>
          <a-button type="primary" @click="showDrawer">发布</a-button>
        </a-card>

        <a-card>
          <div id="editor">
            <mavon-editor :toolbars="markdownOption" v-model="value" :boxShadow="false" :ishljs="true"/>
          </div>
        </a-card>
      </a-col>

      <a-col :xl="24" :lg="24" :md="24" :sm="24" :xs="24">
        <a-drawer title="文章设置" :width="drawerWidth" :closable="true" @close="onClose" :visible="visible">
          <div class="post-setting-drawer-content">
            <div :style="{ marginBottom: '16px' }">
              <h3 class="post-setting-drawer-title">基本设置</h3>
              <div class="post-setting-drawer-item">
                <a-form layout="vertical">
                  <a-form-item label="文章路径：" :help="'https://localhost:8090/archives/' + postUrl">
                    <a-input v-model="postUrl" />
                  </a-form-item>
                  <a-form-item label="文章密码：">
                    <a-input type="password" />
                  </a-form-item>
                  <a-form-item label="是否开启评论：">
                    <a-select defaultValue="1">
                      <a-select-option value="1">是</a-select-option>
                      <a-select-option value="0">否</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-form>
              </div>
            </div>
            <a-divider />

            <div :style="{ marginBottom: '16px' }">
              <h3 class="post-setting-drawer-title">分类目录</h3>
              <div class="post-setting-drawer-item">
                <a-tree checkable :treeData="treeData" :defaultExpandAll="true">
                  <span slot="title0010" style="color: #1890ff">sss</span>
                </a-tree>
              </div>
            </div>
            <a-divider />

            <div :style="{ marginBottom: '16px' }">
              <h3 class="post-setting-drawer-title">标签</h3>
              <div class="post-setting-drawer-item">
                <a-form layout="vertical">
                  <a-form-item>
                    <a-select mode="tags" placeholder="选择或输入标签">
                      <a-select-option v-for="tag in tags" :key="tag.id" :value="tag.id.toString()">{{
                        tag.name
                      }}</a-select-option>
                    </a-select>
                  </a-form-item>
                </a-form>
              </div>
            </div>
            <a-divider />

            <div :style="{ marginBottom: '16px' }">
              <h3 class="post-setting-drawer-title">缩略图</h3>
              <div class="post-setting-drawer-item">
                <div class="post-thum">
                  <img class="img" src="https://os.alipayobjects.com/rmsportal/mgesTPFxodmIwpi.png" />
                </div>
              </div>
            </div>
            <a-divider />
          </div>
          <div class="postControl">
            <a-button style="marginRight: 8px" @click="onClose">保存草稿</a-button>
            <a-button @click="onClose" type="primary">发布</a-button>
          </div>
        </a-drawer>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import { mavonEditor } from 'mavon-editor'
import { mixin, mixinDevice } from '@/utils/mixin.js'
import 'mavon-editor/dist/css/index.css'
import tagApi from '@/api/tag'
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
const treeData = [
  {
    title: '学习记录',
    key: '0-0',
    children: [
      {
        title: 'Java 学习',
        key: '0-0-0',
        children: [{ title: 'JVM', key: '0-0-0-0' }, { title: 'Spring', key: '0-0-0-1' }]
      },
      {
        title: 'PHP 学习',
        key: '0-0-1',
        children: [{ key: '0-0-1-0', title: 'Composer' }, { key: '0-0-1-1', title: 'MySQL' }]
      }
    ]
  },
  {
    title: '生活记录',
    key: '1-0'
  }
]
export default {
  name: 'Editor',
  components: {
    mavonEditor
  },

  mixins: [mixin, mixinDevice],
  data() {
    return {
      wrapperCol: {
        xl: { span: 24 },
        sm: { span: 24 },
        xs: { span: 24 }
      },
      value: 'Hello World',
      visible: false,
      drawerWidth: '460',
      postUrl: 'hello-world',
      tags: [],
      treeData,
      markdownOption: toolbars
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
  },
  methods: {
    loadTags() {
      tagApi.listAll(true).then(response => {
        this.tags = response.data.data
      })
    },
    showDrawer() {
      this.visible = true
    },
    onClose() {
      this.visible = false
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

.mavonEditor {
  width: 100%;
  height: 560px;
}
</style>
