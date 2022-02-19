<template>
  <a-tree-select
    :allowClear="true"
    :treeData="categoryTreeData"
    :treeDataSimpleMode="true"
    v-model="categoryIdString"
    placeholder="请选择上级目录，默认为顶级目录"
    treeDefaultExpandAll
  >
  </a-tree-select>
</template>

<script>
export default {
  name: 'CategorySelectTree',
  props: {
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
      return [
        {
          id: 0,
          title: '根目录',
          value: '0',
          pId: -1
        },
        ...this.categories.map(category => {
          return {
            id: category.id,
            title: category.name,
            value: category.id.toString(),
            pId: category.parentId
          }
        })
      ]
    },
    categoryIdString: {
      get() {
        return this.categoryId.toString()
      },
      set(value) {
        this.$emit('update:categoryId', value ? parseInt(value) : 0)
      }
    }
  }
}
</script>
