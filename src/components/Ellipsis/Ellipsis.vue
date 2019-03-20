<script>
import Tooltip from 'ant-design-vue/es/tooltip'
import { cutStrByFullLength, getStrFullLength } from '@/components/_util/util'
/*
    const isSupportLineClamp = document.body.style.webkitLineClamp !== undefined;

    const TooltipOverlayStyle = {
      overflowWrap: 'break-word',
      wordWrap: 'break-word',
    };
  */

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
    getStrDom (str, fullLength) {
      return (
        <span>{ cutStrByFullLength(str, this.length) + (fullLength > this.length ? '...' : '') }</span>
      )
    },
    getTooltip (fullStr, fullLength) {
      return (
        <Tooltip>
          <template slot="title">{ fullStr }</template>
          { this.getStrDom(fullStr, fullLength) }
        </Tooltip>
      )
    }
  },
  render () {
    const { tooltip, length } = this.$props
    const str = this.$slots.default.map(vNode => vNode.text).join('')
    const fullLength = getStrFullLength(str)
    const strDom = tooltip && fullLength > length ? this.getTooltip(str, fullLength) : this.getStrDom(str, fullLength)
    return (
      strDom
    )
  }
}
</script>
