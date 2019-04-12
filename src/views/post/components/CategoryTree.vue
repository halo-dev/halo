<template>
  <a-tree
    checkable
    :treeData="categoryTree"
    :defaultExpandAll="true"
    @check="onCheck"
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
  model: {
    prop: 'categoryIds',
    event: 'check'
  },
  props: {
    categoryIds: {
      type: Array,
      required: false,
      default: () => []
    },
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
  },
  methods: {
    onCheck(checkedKeys, e) {
      this.$log.debug('Chekced keys', checkedKeys)
      this.$log.debug('e', e)
      const categoryIds = e.checkedNodes
        .filter(node => {
          return node.data.props.isLeaf
        })
        .map(node => node.key)

      this.$log.debug('Effectively selected category ids', categoryIds)

      this.$emit('check', categoryIds)
    }
  }
}
</script>

<style>
</style>
