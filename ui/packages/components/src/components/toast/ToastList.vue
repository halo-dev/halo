<script lang="ts" setup>
import { ToastRoot, ToastViewport, useToastManager } from "reka-ui";
import { computed, onUnmounted, watch } from "vue";
import { useComponentsI18n } from "../../locales";
import type { ToastItemData } from "./internal";
import { cancelToastClosing, closingIds, TOAST_HOTKEY } from "./toast-state";
import ToastContent from "./Toast.vue";

const { toasts } = useToastManager<ToastItemData>();
const { t } = useComponentsI18n();

// Reka UI derives its stack order from registration order, so mount the oldest
// toast first. The newest toast then gets index 0 and sits closest to the top.
const orderedToasts = computed(() => [...toasts.value].reverse());

const viewportLabel = (hotkey: string) =>
  `${t("toast.notifications")} (${hotkey})`;

watch(
  toasts,
  (items) => {
    for (const toast of items) {
      if (toast.open) {
        cancelToastClosing(toast.id);
      }
    }
  },
  { immediate: true, flush: "post" }
);

onUnmounted(() => {
  for (const toast of toasts.value) {
    cancelToastClosing(toast.id);
  }
});
</script>

<template>
  <ToastRoot
    v-for="toast in orderedToasts"
    :key="toast.id"
    :toast="toast"
    :force-mount="closingIds.has(toast.id)"
    :class="['toast-wrapper', toast.data?.customClass]"
  >
    <ToastContent :toast="toast" />
  </ToastRoot>
  <ToastViewport
    class="toast-container"
    :hotkey="TOAST_HOTKEY"
    :label="viewportLabel"
  />
</template>

<style lang="scss">
.toast-container {
  @apply fixed z-[9999] m-0 list-none p-0;
  --toast-edge: 1rem;
  /* The viewport width is the widest a toast may grow; individual toasts
     shrink to fit their content. */
  width: min(22rem, calc(100vw - 2rem));
  left: 50%;
  top: var(--toast-edge);
  transform: translateX(-50%);
  pointer-events: none;
}
</style>
