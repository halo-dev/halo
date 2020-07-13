<template>
  <div :class="prefixCls">
    <div class="float-left">
      <slot name="extra">{{ extra }}</slot>
    </div>
    <div class="float-right">
      <slot></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FooterToolBar',
  props: {
    prefixCls: {
      type: String,
      default: 'ant-pro-footer-toolbar'
    },
    extra: {
      type: [String, Object],
      default: ''
    }
  }
}
</script>
