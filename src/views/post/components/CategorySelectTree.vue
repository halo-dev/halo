<template>
  <a-tree-select
    :treeData="categoryTreeData"
    placeholder="请选择上级目录，默认为顶级目录"
    treeDefaultExpandAll
    :treeDataSimpleMode="true"
    :allowClear="true"
    :value="categoryIdString"
    @change="handleSelectionChange"
  >
  </a-tree-select>
</template>

<script>
export default {
  name: 'CategorySelectTree',
  model: {
    prop: 'categoryId',
    event: 'change'
  },
  props: {
    /**
     * Category id.
     */
    categoryId: {
      type: Number,
      required: true,
      default: 0
    },
    categories: {
      type: Array,
      required: false,
      default: () => []
    }
  },
  computed: {
    categoryTreeData() {
      return this.categories.map(category => {
        return {
          id: category.id,
          title: category.name,
          value: category.id.toString(),
          pId: category.parentId
        }
      })
    },
    categoryIdString() {
      return this.categoryId.toString()
    }
  },
  methods: {
    handleSelectionChange(value, label, extra) {
      this.$log.debug('value: ', value)
      this.$log.debug('label: ', label)
      this.$log.debug('extra: ', extra)
      this.$emit('change', value ? parseInt(value) : 0)
    }
  }
}
</script>
