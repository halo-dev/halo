<script lang="ts" setup>
import type { Tag } from "@halo-dev/api-client";
import { VTag } from "@halo-dev/components";
import { useRouter } from "vue-router";

const props = withDefaults(
  defineProps<{
    tag: Tag;
    rounded?: boolean;
    route?: boolean;
    closable?: boolean;
  }>(),
  {
    rounded: false,
    route: false,
    closable: false,
  }
);

const emit = defineEmits<{
  (event: "close", mouseEvent: MouseEvent): void;
}>();

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
    :closable="closable"
    :clickable="route"
    :color="tag.spec.color || undefined"
    :rounded="rounded"
    @click="handleRouteToDetail"
    @close="emit('close', $event)"
  >
    {{ tag.spec.displayName }}
  </VTag>
</template>
