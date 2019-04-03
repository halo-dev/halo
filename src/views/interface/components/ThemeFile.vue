<script>
export default {
  name: 'ThemeFile',
  props: {
    files: {
      type: Array,
      required: false,
      default: () => []
    }
  },
  methods: {
    renderNode(h, file) {
      if (file.node && file.node.length) {
        return h(
          'a-tree-node',
          {
            props: {
              key: file.path,
              title: file.name,
              isLeaf: file.isFile
            }
          },
          file.node.map(child => {
            return this.renderNode(h, child)
          })
        )
      }
      return h('a-tree-node', {
        props: {
          key: file.path,
          title: file.name,
          isLeaf: file.isFile
        }
      })
    }
  },
  render(h) {
    if (this.files.length) {
      return h(
        'a-directory-tree',
        this.files.map(file => {
          return this.renderNode(h, file)
        })
      )
    }
    return h('p', 'No files')
  }
}
</script>

<style lang="less" scoped>
.ant-tree-child-tree {
  li {
    overflow: hidden;
  }
}
</style>
