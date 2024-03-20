<script lang="ts" setup>
import { NodeViewWrapper } from "@halo-dev/richtext-editor";
</script>
<template>
  <node-view-wrapper as="div" class="inline-block-box inline-block">
    <slot></slot>
  </node-view-wrapper>
</template>
<style scoped lang="scss">
.inline-block-box {
  width: calc(100% - 1px);
}
</style>
