<script>
import Tooltip from 'ant-design-vue/es/tooltip'

const getStrFullLength = (str = '') =>
  str.split('').reduce((pre, cur) => {
    const charCode = cur.charCodeAt(0)
    if (charCode >= 0 && charCode <= 128) {
      return pre + 1
    }
    return pre + 2
  }, 0)

const cutStrByFullLength = (str = '', maxLength) => {
  let showLength = 0
  return str.split('').reduce((pre, cur) => {
    const charCode = cur.charCodeAt(0)
    if (charCode >= 0 && charCode <= 128) {
      showLength += 1
    } else {
      showLength += 2
    }
    if (showLength <= maxLength) {
      return pre + cur
    }
    return pre
  }, '')
}

export default {
  name: 'Ellipsis',
  components: {
    Tooltip
  },
  props: {
    prefixCls: {
      type: String,
      default: 'ant-pro-ellipsis'
    },
    tooltip: {
      type: Boolean
    },
    length: {
      type: Number,
      required: true
    },
    lines: {
      type: Number,
      default: 1
    },
    fullWidthRecognition: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    getStrDom(str, fullLength) {
      return <span>{cutStrByFullLength(str, this.length) + (fullLength > this.length ? '...' : '')}</span>
    },
    getTooltip(fullStr, fullLength) {
      return (
        <Tooltip>
          <template slot="title">{fullStr}</template>
          {this.getStrDom(fullStr, fullLength)}
        </Tooltip>
      )
    }
  },
  render() {
    const { tooltip, length } = this.$props
    const str = this.$slots.default.map(vNode => vNode.text).join('')
    const fullLength = getStrFullLength(str)
    return tooltip && fullLength > length ? this.getTooltip(str, fullLength) : this.getStrDom(str, fullLength)
  }
}
</script>
