<template>
  <a-tree checkable :treeData="categoryTree" defaultExpandAll checkStrictly :checkedKeys="categoryIds" @check="onCheck">
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

      this.$emit('check', checkedKeys.checked)
    }
  }
}
</script>
