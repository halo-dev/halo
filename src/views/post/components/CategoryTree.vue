<template>
  <a-tree
    checkable
    :treeData="categoryTree"
    :defaultExpandAll="true"
  >
    <span
      slot="title0010"
      style="color: #1890ff"
    >sss</span>
  </a-tree>
</template>

<script>
import categoryApi from '@/api/category'

export default {
  name: 'CategoryTree',
  props: {
    categories: {
      type: Array,
      required: false,
      default: () => []
    }
  },
  computed: {
    categoryTree() {
      return categoryApi.concreteTree(this.categories)
    }
  }
}
</script>

<style>
</style>
