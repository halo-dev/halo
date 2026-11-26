<script lang="ts" setup>
import { ToastRoot, ToastViewport, useToastManager } from "reka-ui";
import { computed, onUnmounted, watch } from "vue";
import { useComponentsI18n } from "../../locales";
import type { ToastItemData } from "./internal";
import { cancelToastClosing, closingIds, toastConfig } from "./toast-state";
import ToastContent from "./Toast.vue";

const { toasts } = useToastManager<ToastItemData>();
const { t } = useComponentsI18n();

const placementClass = computed(
  () => `toast-placement-${toastConfig.placement}`
);

// Reka UI derives its stack order from registration order, so mount the oldest
// toast first. The newest toast then gets index 0 and sits closest to the
// screen edge it was placed at.
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
    :class="['toast-container', placementClass]"
    :hotkey="toastConfig.hotkey"
    :label="viewportLabel"
  />
</template>

<style lang="scss">
.toast-container {
  @apply fixed z-[9999] m-0 list-none p-0;
  --stack-dir: 1;
  --toast-edge: 1rem;
  /* The viewport width is the widest a toast may grow; individual toasts
     shrink to fit their content. */
  width: min(22rem, calc(100vw - 2rem));
  pointer-events: none;

  &.toast-placement-top-center,
  &.toast-placement-bottom-center {
    left: 50%;
    transform: translateX(-50%);
  }

  &.toast-placement-top-left,
  &.toast-placement-bottom-left {
    left: var(--toast-edge);
  }

  &.toast-placement-top-right,
  &.toast-placement-bottom-right {
    right: var(--toast-edge);
  }

  &.toast-placement-top-center,
  &.toast-placement-top-left,
  &.toast-placement-top-right {
    top: var(--toast-edge);
  }

  &.toast-placement-bottom-center,
  &.toast-placement-bottom-left,
  &.toast-placement-bottom-right {
    bottom: var(--toast-edge);
    --stack-dir: -1;
  }
}

.toast-container[class*="toast-placement-bottom"] .toast-wrapper {
  top: auto;
  bottom: 0;
}

.toast-container[class*="-right"] .toast-wrapper {
  left: auto;
  right: 0;
}

.toast-container[class*="-center"] .toast-wrapper {
  left: 0;
  right: 0;
  margin-inline: auto;
}
</style>
