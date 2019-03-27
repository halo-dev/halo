<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col :xl="18" :lg="18" :md="18" :sm="24" :xs="24">
        <div style="margin-bottom: 16px">
          <a-input
            v-decorator="['title', { rules: [{ required: true, message: '请输入文章标题' }] }]"
            size="large"
            placeholder="请输入文章标题"
          />
        </div>
        <div style="margin-bottom: 16px">
          <a-input addonBefore="https://ryanc.cc/archives/" defaultValue="mysite" style="width: 360px" />
        </div>

        <div id="editor">
          <mavon-editor v-model="value"/>
        </div>
      </a-col>

      <a-col :xl="6" :lg="6" :md="6" :sm="24" :xs="24">
        this is other
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

  .v-note-wrapper{
    z-index: 1000;
  }
</style>
