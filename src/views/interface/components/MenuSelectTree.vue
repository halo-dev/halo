<template>
  <a-tree-select
    :treeData="menuTreeData"
    placeholder="请选择上级菜单，默认为顶级菜单"
    treeDefaultExpandAll
    :treeDataSimpleMode="true"
    :allowClear="true"
    :value="menuIdString"
    @change="handleSelectionChange"
  >
  </a-tree-select>
</template>

<script>
export default {
  name: 'MenuSelectTree',
  model: {
    prop: 'menuId',
    event: 'change'
  },
  props: {
    menuId: {
      type: Number,
      required: true,
      default: 0
    },
    menus: {
      type: Array,
      required: false,
      default: () => []
    }
  },
  computed: {
    menuTreeData() {
      return this.menus.map(menu => {
        return {
          id: menu.id,
          title: menu.name,
          value: menu.id.toString(),
          pId: menu.parentId
        }
      })
    },
    menuIdString() {
      return this.menuId.toString()
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
