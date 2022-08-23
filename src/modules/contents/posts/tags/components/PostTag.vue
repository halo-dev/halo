<script lang="ts" setup>
import { VTag } from "@halo-dev/components";
import type { Tag } from "@halo-dev/api-client";
import { computed } from "vue";
// @ts-ignore
import Color from "colorjs.io";

const props = defineProps<{
  tag: Tag;
}>();

const labelColor = computed(() => {
  const { color } = props.tag.spec;
  if (!color) {
    return "inherit";
  }
  const onWhite = Math.abs(Color.contrast(color, "white", "APCA"));
  const onBlack = Math.abs(Color.contrast(color, "black", "APCA"));
  return onWhite > onBlack ? "white" : "#333";
});
</script>
<template>
  <VTag :styles="{ background: tag.spec.color, color: labelColor }">
    {{ tag.spec.displayName }}
  </VTag>
</template>
