<script lang="ts" setup>
import type { UseOverlayScrollbarsParams } from "overlayscrollbars-vue";
import { useOverlayScrollbars } from "overlayscrollbars-vue";
import type { PointerDownOutsideEvent } from "reka-ui";
import {
  DialogContent,
  DialogOverlay,
  DialogRoot,
  DialogTitle,
  VisuallyHidden,
} from "reka-ui";
import {
  computed,
  onBeforeUnmount,
  onMounted,
  reactive,
  ref,
  useAttrs,
  useSlots,
  useTemplateRef,
  watch,
} from "vue";
import { IconClose } from "../../icons/icons";
import { useComponentsI18n } from "../../locales";
import { VButton } from "../button";

defineOptions({
  inheritAttrs: false,
});

const props = withDefaults(
  defineProps<{
    visible?: boolean;
    title?: string;
    ariaLabel?: string;
    width?: number | string;
    height?: number | string;
    fullscreen?: boolean;
    bodyClass?: string[];
    mountToBody?: boolean;
    centered?: boolean;
    layerClosable?: boolean;
    closeOnEsc?: boolean;
    showClose?: boolean;
    closeLabel?: string;
    beforeClose?: () => boolean | Promise<boolean>;
    unmountOnHide?: boolean;
  }>(),
  {
    visible: undefined,
    title: undefined,
    ariaLabel: undefined,
    width: 500,
    height: undefined,
    fullscreen: false,
    bodyClass: undefined,
    mountToBody: false,
    centered: true,
    layerClosable: false,
    closeOnEsc: true,
    showClose: true,
    closeLabel: undefined,
    beforeClose: undefined,
    unmountOnHide: false,
  }
);

const emit = defineEmits<{
  (event: "update:visible", value: boolean): void;
  (event: "close"): void;
}>();

// Keep the wrapper mounted a little longer than the exit animation so Reka
// Presence can finish its exit animation before the wrapper hides.
const CLOSE_DURATION = 200;
const LAYER_FEEDBACK_DURATION = 300;

const attrs = useAttrs();
const slots = useSlots();
const { t } = useComponentsI18n();

const internalOpen = ref(props.visible ?? false);
const rootVisible = ref(props.visible ?? false);
const layerFeedback = ref(false);

let closing = false;
let hideTimer: ReturnType<typeof setTimeout> | undefined;
let closeTimer: ReturnType<typeof setTimeout> | undefined;
let feedbackTimer: ReturnType<typeof setTimeout> | undefined;

const hasHeader = computed(() => {
  return !!props.title || !!slots.header;
});

const hasVisibleTitle = computed(() => {
  return !!props.title && !slots.header;
});

const resolvedAriaLabel = computed(() => {
  const ariaLabel = attrs["aria-label"];

  if (typeof ariaLabel === "string" && ariaLabel.trim()) {
    return ariaLabel;
  }

  return props.ariaLabel || props.title || t("modal.label");
});

const closeLabelText = computed(() => {
  return props.closeLabel ?? t("common.close");
});

const wrapperClasses = computed(() => {
  return {
    "modal-wrapper-fullscreen": props.fullscreen,
    "modal-wrapper-centered": props.centered,
  };
});

const contentStyles = computed(() => {
  return {
    maxWidth: toCssSize(props.width),
    height: toCssSize(props.height),
  };
});

const wrapperAttrs = computed(() => {
  return Object.fromEntries(
    Object.entries(attrs).filter(
      ([key]) => !key.startsWith("aria-") && key !== "role"
    )
  );
});

const contentAttrs = computed(() => {
  const result = Object.fromEntries(
    Object.entries(attrs).filter(
      ([key]) => key.startsWith("aria-") || key === "role"
    )
  );

  if (!Object.prototype.hasOwnProperty.call(attrs, "aria-modal")) {
    result["aria-modal"] = "true";
  }

  if (!Object.prototype.hasOwnProperty.call(attrs, "aria-describedby")) {
    result["aria-describedby"] = "";
  }

  return result;
});

watch(
  () => props.visible,
  (visible) => {
    if (visible !== undefined) {
      internalOpen.value = visible;
    }
  }
);

watch(internalOpen, (open) => {
  clearHideTimer();

  if (open) {
    rootVisible.value = true;
    clearCloseTimer();
    return;
  }

  hideTimer = setTimeout(() => {
    rootVisible.value = false;
    hideTimer = undefined;
    destroyScrollbars();
  }, CLOSE_DURATION);
});

onMounted(() => {
  if (props.visible === undefined) {
    internalOpen.value = true;
  }
});

onBeforeUnmount(() => {
  clearHideTimer();
  clearCloseTimer();
  clearFeedbackTimer();
  destroyScrollbars();
});

defineExpose({
  close,
});

function toCssSize(value: number | string | undefined) {
  if (value === undefined || value === "") {
    return undefined;
  }

  return typeof value === "number" ? `${value}px` : value;
}

function clearHideTimer() {
  if (hideTimer !== undefined) {
    clearTimeout(hideTimer);
    hideTimer = undefined;
  }
}

function clearCloseTimer() {
  if (closeTimer !== undefined) {
    clearTimeout(closeTimer);
    closeTimer = undefined;
  }
}

function clearFeedbackTimer() {
  if (feedbackTimer !== undefined) {
    clearTimeout(feedbackTimer);
    feedbackTimer = undefined;
  }
}

function scheduleCloseEvents() {
  clearCloseTimer();

  closeTimer = setTimeout(() => {
    closeTimer = undefined;
    emit("update:visible", false);
    emit("close");
  }, CLOSE_DURATION);
}

async function requestClose() {
  if (closing || !internalOpen.value) {
    return;
  }

  closing = true;

  try {
    if (props.beforeClose) {
      const allowed = await props.beforeClose();

      if (!allowed || !internalOpen.value) {
        return;
      }
    }

    internalOpen.value = false;
    scheduleCloseEvents();
  } catch (error) {
    console.error("Failed to execute beforeClose: ", error);
  } finally {
    closing = false;
  }
}

function close() {
  void requestClose();
}

function handleOpenChange(open: boolean) {
  if (open) {
    internalOpen.value = true;
    return;
  }

  void requestClose();
}

function handleEscapeKeyDown(event: KeyboardEvent) {
  if (event.defaultPrevented) {
    return;
  }

  event.preventDefault();

  if (props.closeOnEsc) {
    void requestClose();
  }
}

function handleOpenAutoFocus(event: Event) {
  // Reka focuses the first tabbable element by default, which is usually a
  // header action button. Focus the dialog itself to avoid triggering that
  // button's focus styles and tooltip as soon as the dialog opens.
  event.preventDefault();

  const container = event.target;

  if (container instanceof HTMLElement) {
    container.focus({ preventScroll: true });
  }
}

function handlePointerDownOutside(event: PointerDownOutsideEvent) {
  event.preventDefault();

  const originalEvent = event.detail.originalEvent;

  if (originalEvent.button !== 0 || originalEvent.ctrlKey) {
    return;
  }

  if (!props.layerClosable) {
    triggerLayerFeedback();
    return;
  }

  void requestClose();
}

function triggerLayerFeedback() {
  clearFeedbackTimer();
  layerFeedback.value = true;

  feedbackTimer = setTimeout(() => {
    layerFeedback.value = false;
    feedbackTimer = undefined;
  }, LAYER_FEEDBACK_DURATION);
}

const modalBody = useTemplateRef<HTMLElement>("modalBody");
const modalViewport = useTemplateRef<HTMLElement>("modalViewport");
const reactiveParams = reactive<UseOverlayScrollbarsParams>({
  options: {
    // Keep the host padding inside the scroll viewport. Otherwise the
    // viewport is inset by the padding and content that touches its edges,
    // such as rings and shadows, is clipped.
    paddingAbsolute: false,
    scrollbars: {
      autoHide: "scroll",
      autoHideDelay: 600,
    },
  },
  defer: true,
});
const [initialize, instance] = useOverlayScrollbars(reactiveParams);

function scrollbarInstance() {
  return instance();
}

function destroyScrollbars() {
  scrollbarInstance()?.destroy();

  // OverlayScrollbars leaves its inline styles on the custom viewport after
  // destroying the instance. Reset them once the modal is hidden so they are
  // not applied on top of the host padding when it is reopened.
  modalViewport.value?.removeAttribute("style");
}

watch(
  internalOpen,
  (open) => {
    if (open && modalBody.value && modalViewport.value) {
      initialize({
        target: modalBody.value,
        // Keep Vue's slot nodes inside their original parent during updates.
        elements: {
          viewport: modalViewport.value,
          content: modalViewport.value,
        },
      });
    }
  },
  { immediate: true, flush: "post" }
);
</script>

<template>
  <DialogRoot
    :open="internalOpen"
    :unmount-on-hide="unmountOnHide"
    @update:open="handleOpenChange"
  >
    <Teleport :disabled="!mountToBody" to="body">
      <div
        v-show="rootVisible"
        :class="wrapperClasses"
        class="modal-wrapper"
        v-bind="wrapperAttrs"
      >
        <DialogOverlay class="modal-layer" />
        <DialogContent
          :class="['modal-content', { 'modal-focus': layerFeedback }]"
          :style="contentStyles"
          v-bind="contentAttrs"
          @escape-key-down="handleEscapeKeyDown"
          @open-auto-focus="handleOpenAutoFocus"
          @pointer-down-outside="handlePointerDownOutside"
        >
          <VisuallyHidden v-if="!hasVisibleTitle">
            <DialogTitle as="h2">{{ resolvedAriaLabel }}</DialogTitle>
          </VisuallyHidden>

          <div v-if="hasHeader" class="modal-header group">
            <slot name="header">
              <DialogTitle as="h2" class="modal-header-title">
                {{ title }}
              </DialogTitle>
              <div v-if="$slots.center" class="modal-header-center">
                <slot name="center"></slot>
              </div>
              <div class="modal-header-actions">
                <slot name="actions"></slot>
                <VButton
                  v-if="showClose"
                  :aria-label="closeLabelText"
                  variant="ghost"
                  shape="circle"
                  size="sm"
                  @click="requestClose()"
                >
                  <template #icon>
                    <IconClose aria-hidden="true" />
                  </template>
                </VButton>
              </div>
            </slot>
          </div>

          <div ref="modalBody" :class="bodyClass" class="modal-body">
            <div ref="modalViewport">
              <slot />
            </div>
          </div>

          <div v-if="$slots.footer" class="modal-footer">
            <slot name="footer" />
          </div>
        </DialogContent>
      </div>
    </Teleport>
  </DialogRoot>
</template>

<style lang="scss">
@keyframes modal-layer-in {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes modal-layer-out {
  from {
    opacity: 1;
  }

  to {
    opacity: 0;
  }
}

@keyframes modal-content-in {
  from {
    opacity: 0;
    transform: translateY(1rem);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes modal-content-in-sm {
  from {
    opacity: 0;
    transform: translateY(0.5rem) scale(0.96);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes modal-content-out {
  from {
    opacity: 1;
    transform: translateY(0) scale(1);
  }

  to {
    opacity: 0;
    transform: translateY(1rem);
  }
}

@keyframes modal-content-out-sm {
  from {
    opacity: 1;
    transform: translateY(0) scale(1);
  }

  to {
    opacity: 0;
    transform: translateY(0) scale(0.96);
  }
}

.modal-wrapper {
  @apply fixed left-0 top-0 flex h-full w-full flex-row items-start justify-center py-10;
  margin: 0 !important;
  z-index: 2000;

  .modal-layer {
    @apply absolute left-0 top-0 h-full w-full flex-none bg-gray-500 bg-opacity-75;

    &[data-state="open"] {
      animation: modal-layer-in 240ms ease-out;
    }

    &[data-state="closed"] {
      animation: modal-layer-out 180ms ease-in forwards;
    }
  }

  .modal-content {
    @apply relative flex flex-col items-stretch rounded-base bg-white shadow-xl outline-none;
    width: calc(100vw - 20px);
    max-height: calc(100vh - 5rem);
    transition: transform 300ms ease;

    &[data-state="open"] {
      animation: modal-content-in 280ms cubic-bezier(0.32, 0.72, 0, 1);
    }

    &[data-state="closed"] {
      animation: modal-content-out 180ms cubic-bezier(0.4, 0, 1, 1) forwards;
    }

    &.modal-focus {
      @apply scale-[1.02] duration-300;
    }

    .modal-header {
      @apply flex items-center justify-between border-b;
      padding: 10px 16px;

      .modal-header-title {
        @apply truncate text-base font-medium;
      }

      .modal-header-actions {
        @apply flex flex-row items-center gap-2;
      }
    }

    .modal-body {
      @apply min-h-0 flex-1 overflow-x-hidden overflow-y-hidden;
      word-wrap: break-word;
      padding: 12px 16px;
    }

    .modal-footer {
      @apply border-t;
      padding: 12px 16px;
    }
  }

  &.modal-wrapper-centered {
    @apply items-center py-0;

    .modal-content {
      max-height: calc(100vh - 20px) !important;
    }
  }

  &.modal-wrapper-fullscreen {
    @apply py-0;

    .modal-content {
      width: 100vw !important;
      max-width: 100vw !important;
      height: 100vh !important;
      max-height: 100vh !important;
      border-radius: 0;
    }
  }
}

@media (min-width: 640px) {
  .modal-wrapper .modal-content[data-state="open"] {
    animation-name: modal-content-in-sm;
  }

  .modal-wrapper .modal-content[data-state="closed"] {
    animation-name: modal-content-out-sm;
  }
}

@media (prefers-reduced-motion: reduce) {
  .modal-wrapper .modal-layer,
  .modal-wrapper .modal-content {
    animation: none !important;
    transition: none;
  }
}
</style>
