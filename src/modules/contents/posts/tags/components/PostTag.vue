<script lang="ts" setup>
import { VTag } from "@halo-dev/components";
import type { Tag } from "@halo-dev/api-client";
import { computed } from "vue";
import { useRouter } from "vue-router";

const props = withDefaults(
  defineProps<{
    tag: Tag;
    route?: boolean;
  }>(),
  {
    route: false,
  }
);

const labelColor = computed(() => {
  const { color } = props.tag.spec;
  if (!color) {
    return "inherit";
  }
  // TODO computed label color
  return "#333";
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
      borderColor: tag.spec.color,
    }"
    @click="handleRouteToDetail"
  >
    {{ tag.spec.displayName }}
  </VTag>
</template>
