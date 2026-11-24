<script lang="ts" setup>
import {
  PopoverArrow,
  PopoverContent,
  PopoverPortal,
  PopoverRoot,
  PopoverTrigger,
} from "reka-ui";
import { computed, ref, watch } from "vue";
import type { VPopoverPlacement } from "./types";

interface OpenPanel {
  order: number;
  trigger: HTMLElement | null;
  hide: () => void;
}

/**
 * Panels are portaled to `body`, so a nested popover is not a DOM descendant of
 * the panel that opened it. Track the mount order of open panels to tell a
 * parent panel which clicks belong to a panel opened from inside it.
 */
let panelOrderSeed = 0;
const openPanels = new Map<HTMLElement, OpenPanel>();

let panelObserver: MutationObserver | null = null;

function disconnectPanelObserverWhenIdle() {
  if (panelObserver && openPanels.size === 0) {
    panelObserver.disconnect();
    panelObserver = null;
  }
}

/**
 * A panel anchored to an element that leaves the document (a toolbar item that
 * is re-rendered, or the row that opens a nested panel inside a closing one)
 * has no position to follow, and the floating layer falls back to the page
 * corner. Close it as soon as its trigger is gone instead of letting it jump.
 */
function observePanelAnchors() {
  if (panelObserver || openPanels.size === 0) {
    return;
  }

  panelObserver = new MutationObserver(() => {
    for (const [element, panel] of openPanels) {
      if (!panel.trigger || panel.trigger.isConnected) {
        continue;
      }

      // The trigger may have been replaced rather than removed: reka binds the
      // new element with `aria-controls`, and the panel keeps a valid anchor.
      const currentTrigger = element.id
        ? document.querySelector(`[aria-controls="${element.id}"]`)
        : null;

      if (!currentTrigger) {
        panel.hide();
      }
    }

    disconnectPanelObserverWhenIdle();
  });
  panelObserver.observe(document.body, { childList: true, subtree: true });
}

// The component renders a trigger and a teleported panel, so attributes belong
// to the slotted trigger element instead of a root element.
defineOptions({ inheritAttrs: false });

const props = withDefaults(
  defineProps<{
    /**
     * Controlled open state, bindable with `v-model:open`.
     */
    open?: boolean;
    /**
     * Whether the trigger is disabled.
     */
    disabled?: boolean;
    /**
     * Preferred placement of the panel.
     */
    placement?: VPopoverPlacement;
    /**
     * Distance in pixels between the trigger and the arrow tip, or the panel
     * when the arrow is disabled.
     */
    offset?: number;
    /**
     * Whether the popover behaves as a modal dialog.
     */
    modal?: boolean;
    /**
     * Whether to render the arrow pointing at the trigger.
     */
    arrow?: boolean;
    /**
     * Whether the panel width follows the trigger width.
     */
    matchTriggerWidth?: boolean;
    /**
     * Whether moving focus outside the panel closes it. Panels that apply
     * their value to a focused editor can set this to `false`, so refocusing
     * the editor does not dismiss the panel while the user is still adjusting
     * it.
     */
    closeOnFocusOutside?: boolean;
    /**
     * Extra classes applied to the panel. Useful for removing the default
     * padding, for example `!p-0`.
     */
    contentClass?: string | string[] | Record<string, boolean>;
  }>(),
  {
    open: undefined,
    disabled: false,
    placement: "bottom-start",
    offset: 4,
    modal: false,
    arrow: true,
    matchTriggerWidth: false,
    closeOnFocusOutside: true,
    contentClass: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:open", value: boolean): void;
}>();

const isOpen = ref(props.open ?? false);

watch(
  () => props.open,
  (value) => {
    if (value === undefined || value === isOpen.value) {
      return;
    }

    isOpen.value = value;
  }
);

const sideAndAlign = computed(() => {
  const [side, align] = props.placement.split("-");

  return {
    side: side as "top" | "right" | "bottom" | "left",
    align: (align ?? "center") as "start" | "center" | "end",
  };
});

function onOpenUpdate(value: boolean) {
  isOpen.value = value;
  emit("update:open", value);
}

function show() {
  onOpenUpdate(true);
}

function hide() {
  onOpenUpdate(false);
}

const contentRef = ref<InstanceType<typeof PopoverContent> | null>(null);
const triggerRef = ref<{ $el?: unknown } | null>(null);
let panelElement: HTMLElement | null = null;
let panelOrder = 0;

watch(
  contentRef,
  (instance) => {
    if (panelElement) {
      openPanels.delete(panelElement);
      panelElement = null;
      disconnectPanelObserverWhenIdle();
    }

    const element = (instance?.$el ?? null) as HTMLElement | null;
    if (!element) {
      return;
    }

    panelElement = element;
    panelOrder = ++panelOrderSeed;

    const trigger = (triggerRef.value?.$el ?? null) as HTMLElement | null;

    openPanels.set(element, { order: panelOrder, trigger, hide });
    observePanelAnchors();
  },
  { flush: "post" }
);

function isFocusInteraction(event: Event) {
  const originalEvent = (event as CustomEvent<{ originalEvent?: Event }>).detail
    ?.originalEvent;

  return originalEvent?.type === "focusin";
}

/**
 * Interacting with a panel opened from this one (for example a color picker
 * opened from another popover) must not dismiss this panel. Only panels opened
 * later count as children; panels opened earlier are siblings or parents.
 */
function onInteractOutside(event: Event) {
  if (!props.closeOnFocusOutside && isFocusInteraction(event)) {
    event.preventDefault();
    return;
  }

  const target = event.target;
  if (!(target instanceof Node)) {
    return;
  }

  for (const [element, panel] of openPanels) {
    if (panel.order > panelOrder && element.contains(target)) {
      event.preventDefault();
      return;
    }
  }
}

defineExpose({
  show,
  hide,
});
</script>

<template>
  <PopoverRoot :open="isOpen" :modal="modal" @update:open="onOpenUpdate">
    <!--
      `as="div"` keeps the trigger from overwriting a `type` prop on the
      slotted component (for example `VButton`), because with `as-child` only
      the slotted element is rendered.
    -->
    <PopoverTrigger
      ref="triggerRef"
      as-child
      as="div"
      :disabled="disabled"
      :aria-disabled="disabled || undefined"
    >
      <slot :open="isOpen" :show="show" :hide="hide" />
    </PopoverTrigger>
    <PopoverPortal>
      <PopoverContent
        ref="contentRef"
        :side="sideAndAlign.side"
        :align="sideAndAlign.align"
        :side-offset="offset"
        :class="[
          'v-popover-content z-[10000] max-w-[calc(100vw-2rem)]',
          'rounded-md border border-gray-200 bg-white p-1.5 text-gray-900 shadow-lg outline-none',
          { 'w-[var(--reka-popover-trigger-width)]': matchTriggerWidth },
          contentClass,
        ]"
        @interact-outside="onInteractOutside"
      >
        <slot name="content" :open="isOpen" :close="hide" />
        <!-- The SVG is scaled from a 12x6 viewBox, so widen the stroke to keep it at 1px. -->
        <PopoverArrow
          v-if="arrow"
          class="v-popover-arrow"
          :stroke-width="1.2"
        />
      </PopoverContent>
    </PopoverPortal>
  </PopoverRoot>
</template>

<style lang="scss">
.v-popover-content {
  &[data-state="open"] {
    animation: v-popover-fade-in 150ms ease-out;
  }

  /**
   * Closing unmounts the panel immediately. A closing animation would keep the
   * panel mounted while its trigger is already gone (for example the row that
   * opens a nested picker lives inside the parent panel that is unmounting),
   * and the floating position then falls back to the page corner.
   */
  &[data-state="closed"] {
    pointer-events: none;
  }
}

.v-popover-arrow {
  // The arrow sits flush with the panel edge, leaving the panel border visible
  // across its base. Nudge it inward so the fill covers the border and the
  // arrow opens into the panel. Every placement rotates the wrapper, so a
  // local -Y shift always points at the panel.
  transform: translateY(-1px);
  @apply fill-white stroke-gray-200;
}

@keyframes v-popover-fade-in {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}
</style>
