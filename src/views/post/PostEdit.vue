<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col :xl="18" :lg="18" :md="18" :sm="24" :xs="24">
        <a-card>
          <div style="margin-bottom: 16px">
            <a-input
              v-decorator="['title', { rules: [{ required: true, message: '请输入文章标题' }] }]"
              size="large"
              placeholder="请输入文章标题"
            />
          </div>
          <div style="margin-bottom: 16px">
            <a-input
              addonBefore="https://ryanc.cc/archives/"
              defaultValue="mysite"
              style="width: 360px"
            />
          </div>
        </a-card>

        <a-card>
          <div id="editor">
            <mavon-editor v-model="value"/>
          </div>
        </a-card>
      </a-col>

      <a-col :xl="6" :lg="6" :md="6" :sm="24" :xs="24">
        <a-card title="发布">
          <a-form layout="vertical">
            <a-form-item label="开启评论：">
              <a-select defaultValue="true">
                <a-select-option value="true">是</a-select-option>
                <a-select-option value="false">否</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="文章密码：">
              <a-input type="password"/>
            </a-form-item>
            <a-form-item>
              <a-button type="primary">保存</a-button>
            </a-form-item>
          </a-form>
        </a-card>
        <a-card title="分类目录"></a-card>
        <a-card title="标签"></a-card>
        <a-card title="缩略图">
          <img alt="example" src="https://ryanc.cc/material/screenshot.png" slot="cover">
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import { mavonEditor } from 'mavon-editor'
import 'mavon-editor/dist/css/index.css'
export default {
  name: 'Editor',
  components: {
    mavonEditor
  },
  data() {
    return {
      wrapperCol: {
        xl: { span: 24 },
        sm: { span: 24 },
        xs: { span: 24 }
      },
      value: 'Hello World'
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
</style>
