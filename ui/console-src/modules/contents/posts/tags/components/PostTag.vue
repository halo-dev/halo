<script lang="ts" setup>
import { VTag } from "@halo-dev/components";
import type { Tag } from "@halo-dev/api-client";
import { computed } from "vue";
import { useRouter } from "vue-router";
import Color from "colorjs.io";

const props = withDefaults(
  defineProps<{
    tag: Tag;
    rounded?: boolean;
    route?: boolean;
  }>(),
  {
    rounded: false,
    route: false,
  }
);

const labelColor = computed(() => {
  const { color } = props.tag.spec;
  if (!color) {
    return "inherit";
  }
  const onWhite = Math.abs(Color.contrast(color, "white", "APCA"));
  const onBlack = Math.abs(Color.contrast(color, "black", "APCA"));
  return onWhite > onBlack ? "white" : "#333";
});

const router = useRouter();

const handleRouteToDetail = () => {
  if (!props.route) {
    return;
  }
  router.push({
    name: "Tags",
    query: { name: props.tag.metadata.name },
  });
};
</script>
<template>
  <VTag
    :styles="{
      background: tag.spec.color,
      color: labelColor,
    }"
    :rounded="rounded"
    @click="handleRouteToDetail"
  >
    {{ tag.spec.displayName }}

    <template #rightIcon>
      <slot name="rightIcon" />
    </template>
  </VTag>
</template>
