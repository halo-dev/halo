<script lang="ts" setup>
import {
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogOverlay,
  DialogPortal,
  DialogRoot,
  DialogTitle,
  VisuallyHidden,
} from "reka-ui";
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  useSlots,
  watch,
} from "vue";
import { IconClose } from "../../icons/icons";
import {
  isHorizontalPlacement,
  normalizeSnapPoints,
  resolveDrawerSize,
  resolveSnapRelease,
  shouldDismissOnDrag,
  type DrawerSnapPoint,
} from "./drag";
import type { DrawerProps } from "./types";

defineOptions({
  inheritAttrs: false,
});

const props = withDefaults(defineProps<DrawerProps>(), {
  visible: undefined,
  defaultVisible: false,
  placement: "right",
  size: undefined,
  title: undefined,
  description: undefined,
  ariaLabel: undefined,
  modal: true,
  inset: false,
  layerClosable: true,
  closeOnEsc: true,
  beforeClose: undefined,
  showClose: true,
  closeLabel: "Close",
  showHandle: false,
  swipeToDismiss: true,
  handleOnly: false,
  swipeThreshold: 0.25,
  snapPoints: undefined,
  snapPoint: undefined,
  defaultSnapPoint: undefined,
  snapToSequentialPoints: false,
  mountToBody: true,
  unmountOnHide: true,
  bodyClass: undefined,
  contentClass: undefined,
});

const emit = defineEmits<{
  (event: "update:visible", value: boolean): void;
  (event: "update:snapPoint", value: DrawerSnapPoint): void;
  (event: "open"): void;
  (event: "close"): void;
}>();

const slots = useSlots();

const DETACH_THRESHOLD = 4;
const SNAP_TRANSITION_MS = 220;
const DISMISS_TRANSITION_MS = 180;
const VELOCITY_THRESHOLD = 0.5;
const INSET_SPACING = 16;
const INSET_MEDIA_QUERY = "(min-width: 640px)";

const internalOpen = ref(props.defaultVisible);
const internalSnapIndex = ref(0);

const viewportSize = ref(
  typeof window === "undefined" ? 0 : window.innerHeight
);

const isInsetViewport = ref(false);

const insetSize = computed(() => (isInsetViewport.value ? INSET_SPACING : 0));

const usableViewportSize = computed(() =>
  Math.max(0, viewportSize.value - insetSize.value * 2)
);

const isOpen = computed<boolean>({
  get: () => props.visible ?? internalOpen.value,
  set: (value) => {
    if (value === isOpen.value) {
      return;
    }

    internalOpen.value = value;
    emit("update:visible", value);

    if (value) {
      emit("open");
    } else {
      emit("close");
    }
  },
});

const horizontal = computed(() => isHorizontalPlacement(props.placement));

const snapEntries = computed(() => {
  if (props.placement !== "bottom") {
    return [];
  }

  return normalizeSnapPoints(props.snapPoints, usableViewportSize.value);
});

const hasSnapPoints = computed(() => snapEntries.value.length > 0);

const snapIndex = computed(() => {
  const entries = snapEntries.value;

  if (entries.length === 0) {
    return 0;
  }

  if (props.snapPoint !== undefined) {
    const index = entries.findIndex((entry) => entry.value === props.snapPoint);
    return index >= 0 ? index : entries.length - 1;
  }

  return Math.min(Math.max(internalSnapIndex.value, 0), entries.length - 1);
});

const snapHeight = computed(() => snapEntries.value[snapIndex.value]?.height);

const canDrag = computed(() => props.swipeToDismiss || hasSnapPoints.value);

const hasVisibleTitle = computed(
  () => !slots.header && (!!props.title || !!slots.title)
);

const hasVisibleDescription = computed(
  () => !slots.header && (!!props.description || !!slots.description)
);

const hasHeader = computed(
  () =>
    !!slots.header ||
    hasVisibleTitle.value ||
    hasVisibleDescription.value ||
    !!slots.actions ||
    props.showClose
);

const resolvedAriaLabel = computed(
  () => props.ariaLabel || props.title || "Drawer"
);

const isDragging = ref(false);
const isSnapping = ref(false);
const dragDismissing = ref(false);
const dragOffset = ref(0);
const dragHeight = ref<number | null>(null);

const panelClasses = computed(() => [
  "drawer-panel",
  `drawer-panel--${props.placement}`,
  {
    "drawer-panel--inset": props.inset,
    "drawer-panel--dragging": isDragging.value,
    "drawer-panel--snapping": isSnapping.value,
    "drawer-panel--dismissing": dragDismissing.value,
  },
  ...(props.contentClass ?? []),
]);

const panelStyles = computed(() => {
  const styles: Record<string, string> = {};

  if (horizontal.value) {
    styles.width = resolveDrawerSize(props.size, "400px");
  } else {
    styles.height = resolveDrawerSize(props.size, "auto");
  }

  if (hasSnapPoints.value && snapHeight.value !== undefined) {
    styles.height = `${dragHeight.value ?? snapHeight.value}px`;
  }

  styles["--drawer-drag-x"] = horizontal.value
    ? `${dragOffset.value}px`
    : "0px";
  styles["--drawer-drag-y"] = horizontal.value
    ? "0px"
    : `${dragOffset.value}px`;

  return styles;
});

let snapTimer: ReturnType<typeof setTimeout> | undefined;
let dismissTimer: ReturnType<typeof setTimeout> | undefined;

interface DragSession {
  pointerId: number;
  startX: number;
  startY: number;
  axis: "x" | "y";
  sign: 1 | -1;
  lastDistance: number;
  lastTime: number;
  velocity: number;
  active: boolean;
  panel: HTMLElement;
  startHeight: number;
  dragDistance: number;
}

let dragSession: DragSession | null = null;

function defaultSnapIndex() {
  const entries = snapEntries.value;

  if (entries.length === 0) {
    return 0;
  }

  if (props.defaultSnapPoint !== undefined) {
    const index = entries.findIndex(
      (entry) => entry.value === props.defaultSnapPoint
    );

    if (index >= 0) {
      return index;
    }
  }

  return entries.length - 1;
}

function setSnapIndex(index: number) {
  const entries = snapEntries.value;

  if (entries.length === 0) {
    return;
  }

  const next = Math.min(Math.max(index, 0), entries.length - 1);

  if (next === snapIndex.value) {
    return;
  }

  internalSnapIndex.value = next;
  emit("update:snapPoint", entries[next].value);
}

function measureViewport() {
  viewportSize.value = window.visualViewport?.height ?? window.innerHeight;
}

let insetMediaQuery: MediaQueryList | undefined;

function updateInsetViewport() {
  isInsetViewport.value = !!props.inset && (insetMediaQuery?.matches ?? false);
}

function clearTimers() {
  if (snapTimer !== undefined) {
    clearTimeout(snapTimer);
    snapTimer = undefined;
  }

  if (dismissTimer !== undefined) {
    clearTimeout(dismissTimer);
    dismissTimer = undefined;
  }
}

function removeDragListeners() {
  if (typeof window === "undefined") {
    return;
  }

  window.removeEventListener("pointermove", onPointerMove);
  window.removeEventListener("pointerup", onPointerUp);
  window.removeEventListener("pointercancel", onPointerCancel);
}

function resetDrag() {
  removeDragListeners();
  clearTimers();
  dragSession = null;
  isDragging.value = false;
  isSnapping.value = false;
  dragOffset.value = 0;
  dragHeight.value = null;
}

function setOpen(value: boolean) {
  isOpen.value = value;
}

let closing = false;

async function requestClose(): Promise<boolean> {
  if (closing) {
    return false;
  }

  closing = true;

  try {
    if (props.beforeClose) {
      const allowed = await props.beforeClose();

      if (!allowed) {
        return false;
      }
    }

    setOpen(false);
    return true;
  } catch (error) {
    console.error("Failed to execute beforeClose: ", error);
    return false;
  } finally {
    closing = false;
  }
}

function close() {
  void requestClose();
}

function open() {
  setOpen(true);
}

function snapTo(index: number) {
  setSnapIndex(index);
}

function canStartScrollDrag(element: HTMLElement) {
  if (horizontal.value) {
    return true;
  }

  if (props.placement === "bottom") {
    return element.scrollTop <= 0;
  }

  return element.scrollTop + element.clientHeight >= element.scrollHeight - 1;
}

function onPointerDown(event: PointerEvent) {
  if (!canDrag.value || !isOpen.value || dragSession) {
    return;
  }

  if (event.button !== 0 || event.isPrimary === false) {
    return;
  }

  const target = event.target as HTMLElement | null;

  if (!target) {
    return;
  }

  if (
    target.closest(
      "input, textarea, select, option, [contenteditable='true'], [data-drawer-no-drag]"
    )
  ) {
    return;
  }

  if (target.closest("button, a, [role='button']")) {
    return;
  }

  const fromHandle = !!target.closest("[data-drawer-handle]");
  const isTouchLike = event.pointerType !== "mouse" && event.pointerType !== "";

  if ((props.handleOnly || isTouchLike) && !fromHandle) {
    return;
  }

  const panel = target.closest<HTMLElement>("[data-drawer-panel]");

  if (!panel) {
    return;
  }

  const scrollElement = target.closest<HTMLElement>("[data-drawer-scroll]");

  if (scrollElement && !canStartScrollDrag(scrollElement)) {
    return;
  }

  const now = event.timeStamp || performance.now();

  dragSession = {
    pointerId: event.pointerId,
    startX: event.clientX,
    startY: event.clientY,
    axis: horizontal.value ? "x" : "y",
    sign: props.placement === "right" || props.placement === "bottom" ? 1 : -1,
    lastDistance: 0,
    lastTime: now,
    velocity: 0,
    active: false,
    panel,
    startHeight: snapHeight.value ?? 0,
    dragDistance: 0,
  };

  window.addEventListener("pointermove", onPointerMove, { passive: false });
  window.addEventListener("pointerup", onPointerUp);
  window.addEventListener("pointercancel", onPointerCancel);
}

function onPointerMove(event: PointerEvent) {
  const session = dragSession;

  if (!session || event.pointerId !== session.pointerId) {
    return;
  }

  const delta =
    session.axis === "x"
      ? event.clientX - session.startX
      : event.clientY - session.startY;
  const signedDistance = delta * session.sign;
  const usesSnapPoints = hasSnapPoints.value;
  const metric = usesSnapPoints ? signedDistance : Math.max(0, signedDistance);

  if (!session.active) {
    if (Math.abs(metric) < DETACH_THRESHOLD) {
      return;
    }

    session.active = true;
    session.lastTime = event.timeStamp || performance.now();
    isDragging.value = true;
  }

  if (event.cancelable) {
    event.preventDefault();
  }

  const now = event.timeStamp || performance.now();
  const elapsed = now - session.lastTime;

  if (elapsed > 0) {
    session.velocity = (metric - session.lastDistance) / elapsed;
    session.lastTime = now;
  }

  session.lastDistance = metric;

  if (usesSnapPoints) {
    session.dragDistance = signedDistance;
    dragHeight.value = Math.min(
      Math.max(session.startHeight - signedDistance, 0),
      usableViewportSize.value
    );
    return;
  }

  const maxDistance = horizontal.value ? window.innerWidth : window.innerHeight;
  session.dragDistance = Math.max(0, signedDistance);
  dragOffset.value = Math.min(session.dragDistance, maxDistance) * session.sign;
}

function onPointerUp(event: PointerEvent) {
  const session = dragSession;

  if (!session || event.pointerId !== session.pointerId) {
    return;
  }

  finishDrag(session);
}

function onPointerCancel(event: PointerEvent) {
  const session = dragSession;

  if (!session || event.pointerId !== session.pointerId) {
    return;
  }

  finishDrag(session, true);
}

function suppressNextClick() {
  const handler = (event: MouseEvent) => {
    event.preventDefault();
    event.stopPropagation();
  };

  window.addEventListener("click", handler, { capture: true, once: true });
  window.setTimeout(() => {
    window.removeEventListener("click", handler, { capture: true });
  }, 0);
}

function finishDrag(session: DragSession, cancelled = false) {
  const distance = cancelled ? 0 : session.dragDistance;
  const velocity = cancelled ? 0 : session.velocity;
  const { active, panel } = session;

  removeDragListeners();
  dragSession = null;
  isDragging.value = false;

  if (!active) {
    dragOffset.value = 0;
    dragHeight.value = null;
    return;
  }

  suppressNextClick();

  const size = horizontal.value ? panel.clientWidth : panel.clientHeight;

  if (hasSnapPoints.value) {
    const result = resolveSnapRelease({
      heights: snapEntries.value.map((entry) => entry.height),
      currentIndex: snapIndex.value,
      dragDistance: distance,
      velocity,
      dismissible: props.swipeToDismiss,
      sequential: props.snapToSequentialPoints,
      velocityThreshold: VELOCITY_THRESHOLD,
    });

    if (result.dismiss) {
      dismissWithDrag(panel);
      return;
    }

    snapBackTo(result.index, panel);
    return;
  }

  if (
    !cancelled &&
    props.swipeToDismiss &&
    shouldDismissOnDrag({
      distance,
      size,
      velocity,
      threshold: props.swipeThreshold,
      velocityThreshold: VELOCITY_THRESHOLD,
    })
  ) {
    dismissWithDrag(panel);
    return;
  }

  snapBack(panel);
}

async function snapBack(panel: HTMLElement) {
  isSnapping.value = true;
  await nextTick();
  void panel.offsetHeight;
  dragOffset.value = 0;
  dragHeight.value = null;

  clearTimers();
  snapTimer = setTimeout(() => {
    isSnapping.value = false;
    snapTimer = undefined;
  }, SNAP_TRANSITION_MS);
}

async function snapBackTo(index: number, panel: HTMLElement) {
  const target = snapEntries.value[index];

  isSnapping.value = true;
  await nextTick();
  void panel.offsetHeight;
  dragOffset.value = 0;

  if (target) {
    dragHeight.value = target.height;
  }

  setSnapIndex(index);

  clearTimers();
  snapTimer = setTimeout(() => {
    dragHeight.value = null;
    isSnapping.value = false;
    snapTimer = undefined;
  }, SNAP_TRANSITION_MS);
}

async function dismissWithDrag(panel: HTMLElement) {
  dragDismissing.value = true;
  isSnapping.value = true;
  await nextTick();
  void panel.offsetHeight;

  const direction =
    props.placement === "right" || props.placement === "bottom" ? 1 : -1;
  const distance =
    (horizontal.value
      ? panel.clientWidth
      : (dragHeight.value ?? panel.clientHeight)) + insetSize.value;
  dragOffset.value = distance * direction;

  clearTimers();
  dismissTimer = setTimeout(async () => {
    dismissTimer = undefined;
    const closed = await requestClose();

    if (!closed) {
      dragDismissing.value = false;
      await snapBack(panel);
    }
  }, DISMISS_TRANSITION_MS);
}

function handleOpenChange(value: boolean) {
  if (value) {
    setOpen(true);
  } else {
    void requestClose();
  }
}

function handleEscapeKeyDown(event: KeyboardEvent) {
  if (!props.closeOnEsc) {
    event.preventDefault();
  }
}

function handlePointerDownOutside(event: Event) {
  if (!props.layerClosable) {
    event.preventDefault();
  }
}

function handleFocusOutside(event: Event) {
  if (!props.layerClosable) {
    event.preventDefault();
  }
}

watch(
  isOpen,
  (value) => {
    if (value) {
      dragDismissing.value = false;
      internalSnapIndex.value = defaultSnapIndex();
    } else if (dragOffset.value === 0) {
      dragDismissing.value = false;
    }

    resetDrag();
  },
  { immediate: true }
);

watch(
  () => snapEntries.value.length,
  (length) => {
    if (length > 0) {
      internalSnapIndex.value = Math.min(internalSnapIndex.value, length - 1);
    }
  }
);

onMounted(() => {
  insetMediaQuery = window.matchMedia?.(INSET_MEDIA_QUERY);
  updateInsetViewport();
  insetMediaQuery?.addEventListener("change", updateInsetViewport);
  measureViewport();
  window.addEventListener("resize", measureViewport);
  window.visualViewport?.addEventListener("resize", measureViewport);
});

onBeforeUnmount(() => {
  insetMediaQuery?.removeEventListener("change", updateInsetViewport);
  insetMediaQuery = undefined;
  window.removeEventListener("resize", measureViewport);
  window.visualViewport?.removeEventListener("resize", measureViewport);
  resetDrag();
});

watch(
  () => props.inset,
  () => updateInsetViewport()
);

defineExpose({
  open,
  close,
  snapTo,
});
</script>

<template>
  <DialogRoot
    :open="isOpen"
    :modal="modal"
    :unmount-on-hide="unmountOnHide"
    @update:open="handleOpenChange"
  >
    <DialogPortal :disabled="!mountToBody">
      <DialogOverlay v-if="modal" class="drawer-overlay" />
      <DialogContent
        v-bind="$attrs"
        :class="panelClasses"
        :style="panelStyles"
        data-drawer-panel
        :data-placement="placement"
        :data-snap-points="hasSnapPoints ? '' : undefined"
        @pointerdown="onPointerDown"
        @escape-key-down="handleEscapeKeyDown"
        @pointer-down-outside="handlePointerDownOutside"
        @focus-outside="handleFocusOutside"
      >
        <VisuallyHidden v-if="!hasVisibleTitle">
          <DialogTitle>{{ resolvedAriaLabel }}</DialogTitle>
        </VisuallyHidden>
        <VisuallyHidden v-if="!hasVisibleDescription">
          <DialogDescription>
            <slot name="description">{{ description }}</slot>
          </DialogDescription>
        </VisuallyHidden>

        <div
          v-if="showHandle"
          aria-hidden="true"
          class="drawer-handle"
          data-drawer-handle
        >
          <slot name="handle">
            <span class="drawer-handle-bar" />
          </slot>
        </div>

        <div v-if="hasHeader" class="drawer-header" data-drawer-handle>
          <slot name="header">
            <div class="drawer-header-text">
              <DialogTitle v-if="hasVisibleTitle" class="drawer-title">
                <slot name="title">{{ title }}</slot>
              </DialogTitle>
              <DialogDescription
                v-if="hasVisibleDescription"
                class="drawer-description"
              >
                <slot name="description">{{ description }}</slot>
              </DialogDescription>
            </div>
            <div v-if="$slots.actions" class="drawer-header-actions">
              <slot name="actions" />
            </div>
            <DialogClose
              v-if="showClose"
              :aria-label="closeLabel"
              class="drawer-close"
            >
              <IconClose aria-hidden="true" />
            </DialogClose>
          </slot>
        </div>

        <div :class="bodyClass" class="drawer-body" data-drawer-scroll>
          <slot />
        </div>

        <div v-if="$slots.footer" class="drawer-footer">
          <slot name="footer" />
        </div>
      </DialogContent>
    </DialogPortal>
  </DialogRoot>
</template>

<style lang="scss">
.drawer-overlay {
  @apply fixed inset-0 z-[2000] bg-gray-500 bg-opacity-75;

  &[data-state="open"] {
    animation: drawer-overlay-in 200ms ease-out;
  }

  &[data-state="closed"] {
    animation: drawer-overlay-out 150ms ease-in;
  }
}

.drawer-panel {
  @apply fixed z-[2000] flex flex-col overflow-hidden bg-white shadow-xl outline-none;
  --drawer-exit: 0px;
  max-width: 100vw;
  max-height: 100dvh;
  transform: translate3d(
    var(--drawer-drag-x, 0px),
    var(--drawer-drag-y, 0px),
    0
  );

  &--right {
    @apply right-0 top-0 h-[100dvh];

    &[data-state="open"] {
      animation: drawer-in-right 240ms cubic-bezier(0.32, 0.72, 0, 1);
    }

    &[data-state="closed"] {
      animation: drawer-out-right 180ms ease-in;
    }
  }

  &--left {
    @apply left-0 top-0 h-[100dvh];

    &[data-state="open"] {
      animation: drawer-in-left 240ms cubic-bezier(0.32, 0.72, 0, 1);
    }

    &[data-state="closed"] {
      animation: drawer-out-left 180ms ease-in;
    }
  }

  &--top {
    @apply left-0 top-0 w-full rounded-b-base;

    &[data-state="open"] {
      animation: drawer-in-top 240ms cubic-bezier(0.32, 0.72, 0, 1);
    }

    &[data-state="closed"] {
      animation: drawer-out-top 180ms ease-in;
    }
  }

  &--bottom {
    @apply bottom-0 left-0 w-screen rounded-t-base;

    &[data-state="open"] {
      animation: drawer-in-bottom 240ms cubic-bezier(0.32, 0.72, 0, 1);
    }

    &[data-state="closed"] {
      animation: drawer-out-bottom 180ms ease-in;
    }
  }

  @media (min-width: 640px) {
    &--inset {
      --drawer-exit: 16px;
      border-radius: 8px;

      &.drawer-panel--right,
      &.drawer-panel--left {
        top: 16px;
        bottom: 16px;
        height: auto;
        max-width: calc(100vw - 32px);
        max-height: calc(100dvh - 32px);
      }

      &.drawer-panel--right {
        right: 16px;
      }

      &.drawer-panel--left {
        left: 16px;
      }

      &.drawer-panel--top,
      &.drawer-panel--bottom {
        left: 16px;
        right: 16px;
        width: auto;
        max-width: calc(100vw - 32px);
      }

      &.drawer-panel--top {
        top: 16px;
      }

      &.drawer-panel--bottom {
        bottom: 16px;
      }
    }
  }

  &--dragging {
    @apply cursor-grabbing select-none;
    will-change: transform;

    * {
      @apply select-none;
    }
  }

  &--snapping {
    transition:
      transform 200ms cubic-bezier(0.32, 0.72, 0, 1),
      width 200ms cubic-bezier(0.32, 0.72, 0, 1),
      height 200ms cubic-bezier(0.32, 0.72, 0, 1);
    will-change: transform, width, height;
  }

  &--dismissing[data-state="closed"] {
    animation: none;
  }
}

.drawer-handle {
  @apply flex shrink-0 cursor-grab items-center justify-center py-2;
  touch-action: none;
}

.drawer-handle-bar {
  @apply h-1 w-10 rounded-full bg-gray-300;
}

.drawer-header {
  @apply flex shrink-0 items-start justify-between gap-3 border-b border-gray-200 px-4 py-3;
  touch-action: none;
}

.drawer-header-text {
  @apply min-w-0 flex-1;
}

.drawer-title {
  @apply truncate text-base font-medium text-gray-900;
}

.drawer-description {
  @apply mt-1 text-sm text-gray-500;
}

.drawer-header-actions {
  @apply flex shrink-0 items-center gap-2;
}

.drawer-close {
  @apply inline-flex h-7 w-7 shrink-0 cursor-pointer items-center justify-center rounded-full text-gray-600 transition-colors hover:bg-gray-100 hover:text-gray-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary;
}

.drawer-body {
  @apply min-h-0 flex-1 overflow-y-auto px-4 py-3;
  overscroll-behavior: contain;
}

.drawer-footer {
  @apply shrink-0 border-t border-gray-200 px-4 py-3;
}

@keyframes drawer-overlay-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes drawer-overlay-out {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

@keyframes drawer-in-right {
  from {
    transform: translate3d(calc(100% + var(--drawer-exit)), 0, 0);
  }
  to {
    transform: translate3d(0, 0, 0);
  }
}

@keyframes drawer-out-right {
  from {
    transform: translate3d(0, 0, 0);
  }
  to {
    transform: translate3d(calc(100% + var(--drawer-exit)), 0, 0);
  }
}

@keyframes drawer-in-left {
  from {
    transform: translate3d(calc(-100% - var(--drawer-exit)), 0, 0);
  }
  to {
    transform: translate3d(0, 0, 0);
  }
}

@keyframes drawer-out-left {
  from {
    transform: translate3d(0, 0, 0);
  }
  to {
    transform: translate3d(calc(-100% - var(--drawer-exit)), 0, 0);
  }
}

@keyframes drawer-in-top {
  from {
    transform: translate3d(0, calc(-100% - var(--drawer-exit)), 0);
  }
  to {
    transform: translate3d(0, 0, 0);
  }
}

@keyframes drawer-out-top {
  from {
    transform: translate3d(0, 0, 0);
  }
  to {
    transform: translate3d(0, calc(-100% - var(--drawer-exit)), 0);
  }
}

@keyframes drawer-in-bottom {
  from {
    transform: translate3d(0, calc(100% + var(--drawer-exit)), 0);
  }
  to {
    transform: translate3d(0, 0, 0);
  }
}

@keyframes drawer-out-bottom {
  from {
    transform: translate3d(0, 0, 0);
  }
  to {
    transform: translate3d(0, calc(100% + var(--drawer-exit)), 0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .drawer-overlay,
  .drawer-panel {
    animation-duration: 0.01ms !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
