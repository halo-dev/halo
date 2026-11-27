<script lang="ts" setup>
import {
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogOverlay,
  AlertDialogPortal,
  AlertDialogRoot,
  AlertDialogTitle,
  VisuallyHidden,
} from "reka-ui";
import {
  computed,
  markRaw,
  onBeforeUnmount,
  ref,
  useSlots,
  watch,
  type Component,
  type Raw,
} from "vue";
import type { DialogProps, DialogType } from "@/components/dialog/types";
import {
  IconCheckboxCircle,
  IconClose,
  IconErrorWarning,
  IconForbidLine,
  IconInformation,
} from "../../icons/icons";
import { useComponentsI18n } from "../../locales";
import { VButton } from "../button";
import { VScrollArea } from "../scroll-area";

defineOptions({
  inheritAttrs: false,
});

const props = withDefaults(defineProps<DialogProps>(), {
  type: "info",
  size: "md",
  title: undefined,
  description: "",
  confirmText: undefined,
  confirmType: "primary",
  showCancel: true,
  showClose: true,
  closeOnEsc: true,
  layerClosable: false,
  cancelText: undefined,
  visible: false,
  width: undefined,
  icon: undefined,
  onConfirm: () => {
    return;
  },
  onCancel: () => {
    return;
  },
});

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const slots = useSlots();

const { t } = useComponentsI18n();

const sizes = {
  sm: 400,
  md: 450,
  lg: 600,
} as const;

// Keep the utility classes as complete literals so Tailwind can detect them.
const variants: Record<
  DialogType,
  { icon: Raw<Component>; colorClass: string }
> = {
  success: {
    icon: markRaw(IconCheckboxCircle),
    colorClass: "text-green-500",
  },
  info: {
    icon: markRaw(IconInformation),
    colorClass: "text-sky-500",
  },
  warning: {
    icon: markRaw(IconErrorWarning),
    colorClass: "text-orange-500",
  },
  error: {
    icon: markRaw(IconForbidLine),
    colorClass: "text-red-500",
  },
};

const titleText = computed(() => props.title ?? t("dialog.title"));

const confirmText = computed(() => props.confirmText ?? t("common.confirm"));

const cancelText = computed(() => props.cancelText ?? t("common.cancel"));

const closeLabel = computed(() => t("common.close"));

const hasDescription = computed(
  () => !!props.description || !!slots.description
);

const contentStyle = computed(() => ({
  maxWidth: `${props.width ?? sizes[props.size]}px`,
}));

const internalOpen = ref(false);
const closed = ref(false);
const loading = ref(false);
let unmounted = false;

watch(
  () => props.visible,
  (visible) => {
    internalOpen.value = !!visible;

    if (visible) {
      closed.value = false;
      loading.value = false;
    } else {
      closed.value = true;
    }
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  unmounted = true;
});

function finalizeClose() {
  if (closed.value) {
    return;
  }

  closed.value = true;
  loading.value = false;
  internalOpen.value = false;
  emit("update:visible", false);
  emit("close");
}

function handleCancel() {
  if (loading.value || closed.value) {
    return;
  }

  props.onCancel?.();
  finalizeClose();
}

function handleConfirm() {
  if (loading.value || closed.value) {
    return;
  }

  loading.value = true;

  Promise.resolve()
    .then(() => props.onConfirm?.())
    .then(() => {
      if (unmounted) {
        return;
      }

      loading.value = false;
      finalizeClose();
    })
    .catch((error) => {
      // Keep the dialog open so the user can retry the failed action.
      console.error("Failed to execute onConfirm: ", error);

      if (!unmounted) {
        loading.value = false;
      }
    });
}

function handleOpenChange(open: boolean) {
  if (open) {
    closed.value = false;
    internalOpen.value = true;
    return;
  }

  // Escape and the cancel button are the only ways reka-ui reports a close.
  handleCancel();
}

function handleEscapeKeyDown(event: KeyboardEvent) {
  if (!props.closeOnEsc || loading.value) {
    event.preventDefault();
  }
}

function handleOverlayClick() {
  if (props.layerClosable) {
    handleCancel();
  }
}

function close() {
  finalizeClose();
}

defineExpose({
  close,
});
</script>
<template>
  <AlertDialogRoot :open="internalOpen" @update:open="handleOpenChange">
    <AlertDialogPortal>
      <AlertDialogOverlay class="dialog-overlay" @click="handleOverlayClick" />
      <AlertDialogContent
        v-bind="$attrs"
        class="dialog-content"
        :style="contentStyle"
        @escape-key-down="handleEscapeKeyDown"
      >
        <VScrollArea class="dialog-body" content-class="dialog-body-content">
          <div class="dialog-header">
            <div class="flex min-w-0 flex-1 items-center gap-3">
              <slot name="icon">
                <component
                  :is="icon ?? variants[type].icon"
                  aria-hidden="true"
                  class="h-6 w-6 flex-none"
                  :class="variants[type].colorClass"
                ></component>
              </slot>
              <AlertDialogTitle class="dialog-title">
                {{ titleText }}
              </AlertDialogTitle>
            </div>
            <button
              v-if="showClose"
              :aria-label="closeLabel"
              :disabled="loading"
              class="dialog-close"
              data-dialog-close
              type="button"
              @click="handleCancel"
            >
              <IconClose aria-hidden="true" class="h-4 w-4" />
            </button>
          </div>
          <AlertDialogDescription
            v-if="hasDescription"
            class="dialog-description"
          >
            <slot name="description">{{ description }}</slot>
          </AlertDialogDescription>
          <VisuallyHidden v-else>
            <AlertDialogDescription>
              {{ description }}
            </AlertDialogDescription>
          </VisuallyHidden>
          <slot />
        </VScrollArea>
        <div class="dialog-footer">
          <slot name="footer">
            <VButton
              :loading="loading"
              :type="confirmType"
              data-dialog-confirm
              @click="handleConfirm"
            >
              {{ confirmText }}
            </VButton>
            <AlertDialogCancel
              v-if="showCancel"
              :disabled="loading"
              class="btn btn-md btn-default"
              data-dialog-cancel
              type="button"
            >
              {{ cancelText }}
            </AlertDialogCancel>
          </slot>
        </div>
      </AlertDialogContent>
    </AlertDialogPortal>
  </AlertDialogRoot>
</template>

<style lang="scss">
.dialog-overlay {
  @apply fixed inset-0 z-[2000] bg-gray-500 bg-opacity-75;

  &[data-state="open"] {
    animation: dialog-overlay-in 200ms ease-out;
  }

  &[data-state="closed"] {
    animation: dialog-overlay-out 100ms ease-in;
  }
}

.dialog-content {
  @apply fixed left-1/2 top-1/2 z-[2000] flex w-[calc(100vw-20px)] -translate-x-1/2 -translate-y-1/2 flex-col overflow-hidden rounded-base bg-white shadow-xl outline-none;
  max-height: calc(100dvh - 20px);

  &[data-state="open"] {
    animation: dialog-in 200ms ease-out;
  }

  &[data-state="closed"] {
    animation: dialog-out 100ms ease-in;
  }
}

.dialog-body {
  @apply flex min-h-0 flex-col overflow-hidden;
  flex: 1 1 auto;

  > .v-scroll-area__viewport {
    flex: 1 1 auto;
    min-height: 0;
  }
}

.dialog-body-content {
  height: 100%;
  @apply px-4 pb-4 pt-5;
}

.dialog-header {
  @apply mb-2 flex items-start justify-between gap-3;
}

.dialog-title {
  @apply text-base font-bold text-gray-900;
}

.dialog-description {
  @apply break-words text-sm text-gray-700;
}

.dialog-close {
  @apply -mr-1 -mt-1 inline-flex h-7 w-7 shrink-0 cursor-pointer items-center justify-center rounded-full text-gray-600 transition-colors hover:bg-gray-100 hover:text-gray-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary disabled:cursor-not-allowed disabled:opacity-50;
}

.dialog-footer {
  @apply flex flex-row flex-wrap gap-3 border-t px-4 py-3;
}

@keyframes dialog-overlay-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes dialog-overlay-out {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

@keyframes dialog-in {
  from {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

@keyframes dialog-out {
  from {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  to {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.98);
  }
}

@media (prefers-reduced-motion: reduce) {
  .dialog-overlay,
  .dialog-content {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
