<script lang="ts" setup>
import type { Notification } from "@halo-dev/api-client";
import { VScrollArea } from "@halo-dev/components";
import sanitize from "sanitize-html";
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    notification?: Notification;
  }>(),
  { notification: undefined }
);

const content = computed(() => {
  return sanitize(props.notification?.spec?.htmlContent || "");
});
</script>

<template>
  <VScrollArea class="h-full w-full">
    <div class="markdown-body h-full w-full p-2 text-sm" v-html="content"></div>
  </VScrollArea>
</template>

<style scoped lang="scss">
.markdown-body :deep(ul) {
  list-style: disc !important;
}

.markdown-body :deep(ol) {
  list-style: decimal !important;
}
</style>
