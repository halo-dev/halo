<template>
  <a-tree
    :checkedKeys="categoryIds"
    :treeData="categoryTree"
    checkStrictly
    checkable
    defaultExpandAll
    showLine
    @check="onCheck"
  >
  </a-tree>
</template>

<script>
import apiClient from '@/utils/api-client'

function concreteTree(parentCategory, categories) {
  categories.forEach(category => {
    if (parentCategory.key === category.parentId) {
      if (!parentCategory.children) {
        parentCategory.children = []
      }
      parentCategory.children.push({
        key: category.id,
        title: category.name,
        isLeaf: false
      })
    }
  })

  if (parentCategory.children) {
    parentCategory.children.forEach(category => concreteTree(category, categories))
  } else {
    parentCategory.isLeaf = true
  }
}

function getConcreteTree(categories) {
  const topCategoryNode = {
    key: 0,
    title: 'top',
    children: []
  }
  concreteTree(topCategoryNode, categories)
  return topCategoryNode.children
}

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
    }
  },
  data() {
    return {
      categories: {
        data: [],
        loading: false
      }
    }
  },
  computed: {
    categoryTree() {
      if (!this.categories.data.length) {
        return []
      }
      return getConcreteTree(this.categories.data)
    }
  },
  created() {
    this.handleListCategories()
  },
  methods: {
    async handleListCategories() {
      try {
        this.categories.loading = true

        const { data } = await apiClient.category.list({ sort: [], more: false })

        this.categories.data = data
      } catch (error) {
        this.$log.error(error)
      } finally {
        this.categories.loading = false
      }
    },

    onCheck(checkedKeys, e) {
      this.$log.debug('Chekced keys', checkedKeys)
      this.$log.debug('e', e)

      this.$emit('check', checkedKeys.checked)
    }
  }
}
</script>
