import service from '@/utils/service'

const baseUrl = '/api/admin/categories'

const categoryApi = {}

categoryApi.listAll = (more = false) => {
  return service({
    url: `${baseUrl}`,
    params: {
      more: more
    },
    method: 'get'
  })
}

categoryApi.listTree = () => {
  return service({
    url: `${baseUrl}/tree_view`,
    method: 'get'
  })
}

categoryApi.create = category => {
  return service({
    url: baseUrl,
    data: category,
    method: 'post'
  })
}

categoryApi.delete = categoryId => {
  return service({
    url: `${baseUrl}/${categoryId}`,
    method: 'delete'
  })
}

categoryApi.get = categoryId => {
  return service({
    url: `${baseUrl}/${categoryId}`,
    method: 'get'
  })
}

categoryApi.update = (categoryId, category) => {
  return service({
    url: `${baseUrl}/${categoryId}`,
    data: category,
    method: 'put'
  })
}

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

categoryApi.concreteTree = categories => {
  const topCategoryNode = {
    key: 0,
    title: 'top',
    children: []
  }
  concreteTree(topCategoryNode, categories)
  return topCategoryNode.children
}

export default categoryApi
