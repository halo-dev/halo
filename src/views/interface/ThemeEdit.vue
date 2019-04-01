<template>
  <div class="page-header-index-wide">
    <a-row :gutter="12">
      <a-col :xl="18" :lg="18" :md="18" :sm="24" :xs="24">
        <a-card hoverable>
          <codemirror :value="value"></codemirror>
        </a-card>
      </a-col>
      <a-col :xl="6" :lg="6" :md="6" :sm="24" :xs="24">
        <a-card hoverable title="Anatole 主题">
          <a-directory-tree multiple @select="onSelect" @expand="onExpand">
            <a-tree-node title="source" key="0-0">
              <a-tree-node title="css" key="0-0-1">
                <a-tree-node title="style.css" key="0-0-1-2" isLeaf />
              </a-tree-node>
              <a-tree-node title="js" key="0-0-2" isLeaf />
              <a-tree-node title="images" key="0-0-3" isLeaf />
            </a-tree-node>
            <a-tree-node title="module" key="0-1">
              <a-tree-node title="options.ftl" key="0-1-0" isLeaf />
            </a-tree-node>
            <a-tree-node title="index.ftl" key="0-2-0" />
            <a-tree-node title="posts.ftl" key="0-3-0" />
            <a-tree-node title="category.ftl" key="0-4-0" />
            <a-tree-node title="tags.ftl" key="0-5-0" />
            <a-tree-node title="links.ftl" key="0-6-0" />
            <a-tree-node title="page_author.ftl" key="0-7-0" />
            <a-tree-node title="page_search.ftl" key="0-8-0" />
          </a-directory-tree>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import { codemirror } from 'vue-codemirror-lite'
export default {
  components: {
    codemirror
  },
  data() {
    return {
      value: 'Hello World'
    }
  },
  methods: {
    onSelect(keys) {
      console.log('Trigger Select', keys)
    },
    onExpand() {
      console.log('Trigger Expand')
    }
  }
}
</script>

<style>
.CodeMirror-gutters {
  border-right: 1px solid #fff3f3;
  background-color: #ffffff;
}
</style>
