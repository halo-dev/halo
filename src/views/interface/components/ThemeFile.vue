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
    handleSelectFile(file) {
      this.$emit('listenToSelect', file)
    },
    renderNode(h, file) {
      const _this = this
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
        },
        nativeOn: {
          click: function() {
            _this.handleSelectFile(file)
          }
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
    return h('p', '没有文件')
  }
}
</script>
