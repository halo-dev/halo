<template>
  <a-tag :color="tag.color" :style="{ color: labelColor }">
    {{ tag.name }}
  </a-tag>
</template>
<script>
import { isHex, isLight } from '@/utils/colorUtil'

export default {
  name: 'PostTag',
  props: {
    tag: {
      type: Object,
      default: () => {}
    }
  },
  computed: {
    labelColor() {
      const { color } = this.tag || {}
      if (!color) return 'inherit'
      if (!isHex(color)) {
        return 'inherit'
      }
      return !isLight(color) ? '#fff' : 'inherit'
    }
  }
}
</script>
