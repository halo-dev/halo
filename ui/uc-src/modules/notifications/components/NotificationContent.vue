<script lang="ts" setup>
import type { Notification } from "@halo-dev/api-client";
import { computed } from "vue";
import { OverlayScrollbarsComponent } from "overlayscrollbars-vue";

const props = withDefaults(
  defineProps<{
    notification?: Notification;
  }>(),
  { notification: undefined }
);

const htmlContent = computed(() => {
  const styles = `
  <style>
    html {
        font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji";
        font-size: 0.875rem;
        line-height: 1.25rem;
    }
  </style>
  `;

  if (!props.notification?.spec?.htmlContent) {
    return "";
  }

  return styles + props.notification?.spec?.htmlContent;
});
</script>

<template>
  <div class="h-full w-full overflow-auto">
    <OverlayScrollbarsComponent
      element="div"
      :options="{ scrollbars: { autoHide: 'scroll' } }"
      class="h-full w-full"
      defer
    >
      <iframe class="h-full w-full p-2" :srcdoc="htmlContent"></iframe>
    </OverlayScrollbarsComponent>
  </div>
</template>
