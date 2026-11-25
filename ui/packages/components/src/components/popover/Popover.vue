<script lang="ts" setup>
import {
  PopoverArrow,
  PopoverContent,
  PopoverPortal,
  PopoverRoot,
  PopoverTrigger,
} from "reka-ui";
import { computed, onBeforeUnmount, ref, watch } from "vue";
import type { VPopoverPlacement } from "./types";

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
     * Whether the panel closes once the element it is anchored to leaves the
     * document, for example a trigger rendered with `v-if` or a row inside a
     * panel that is closing. Without an anchor the floating layer falls back
     * to the page corner, so closing is the default instead of letting the
     * panel jump there.
     */
    closeOnTriggerDetached?: boolean;
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
    closeOnTriggerDetached: true,
    contentClass: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:open", value: boolean): void;
  /**
   * Emitted before focus moves into a newly opened panel, so consumers can
   * call `preventDefault()` to keep focus where it is.
   */
  (event: "open-auto-focus", value: Event): void;
  /**
   * Emitted when a panel closes and focus is about to return to the trigger.
   * Consumers can `preventDefault()` and move focus themselves, for example
   * back into an editor.
   */
  (event: "close-auto-focus", value: Event): void;
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

/**
 * Panels opened from inside another panel are portaled to `body` as well, so
 * they are not DOM descendants of the panel that opened them. Reka already
 * treats an interaction inside a dismissable layer that comes later in the
 * document as inside the current one, for both pointer and focus events, so
 * nested panels need no bookkeeping here.
 *
 * The remaining gap is an anchor that leaves the document: the floating layer
 * then falls back to the page corner. Watch only the trigger's parent, since a
 * document-wide observer would run its callback on every DOM mutation in the
 * application while a panel is open.
 */
let anchorObserver: MutationObserver | null = null;
let observedTrigger: HTMLElement | null = null;

function stopObservingAnchor() {
  anchorObserver?.disconnect();
  anchorObserver = null;
  observedTrigger = null;
}

/**
 * Reka binds the trigger to the panel with `aria-controls`, so a trigger that
 * was replaced rather than removed still has a valid anchor: the replacement
 * carries the same `aria-controls` value.
 */
function findReplacementTrigger(trigger: HTMLElement) {
  const contentId = trigger.getAttribute("aria-controls");
  if (!contentId) {
    return null;
  }

  return (
    Array.from(
      (trigger.ownerDocument ?? document).querySelectorAll<HTMLElement>(
        "[aria-controls]"
      )
    ).find(
      (element) =>
        element !== trigger &&
        element.getAttribute("aria-controls") === contentId
    ) ?? null
  );
}

/**
 * Reka points the trigger at the panel with `aria-controls`, which identifies
 * the element that actually anchors the panel even when the slotted trigger
 * renders a wrapper or a fragment.
 */
function resolveTriggerElement() {
  const contentElement = (contentRef.value?.$el ?? null) as HTMLElement | null;
  const contentId = contentElement?.id;

  if (contentId) {
    const bound = Array.from(
      document.querySelectorAll<HTMLElement>("[aria-controls]")
    ).find((element) => element.getAttribute("aria-controls") === contentId);
    if (bound) {
      return bound;
    }
  }

  const slotted = triggerRef.value?.$el;
  return slotted instanceof HTMLElement ? slotted : null;
}

function observeAnchor() {
  stopObservingAnchor();

  if (!props.closeOnTriggerDetached) {
    return;
  }

  const trigger = resolveTriggerElement();
  const parent = trigger?.parentElement;
  if (!trigger || !parent) {
    return;
  }

  observedTrigger = trigger;

  anchorObserver = new MutationObserver(() => {
    const currentTrigger = observedTrigger;
    if (!currentTrigger || currentTrigger.isConnected) {
      return;
    }

    const replacement = findReplacementTrigger(currentTrigger);

    if (replacement) {
      observedTrigger = replacement;
      return;
    }

    stopObservingAnchor();
    hide();
  });
  anchorObserver.observe(parent, { childList: true });
}

watch(
  contentRef,
  (instance) => {
    if (!instance) {
      stopObservingAnchor();
      return;
    }

    observeAnchor();
  },
  { flush: "post" }
);

onBeforeUnmount(stopObservingAnchor);

function onFocusOutside(event: Event) {
  if (!props.closeOnFocusOutside) {
    event.preventDefault();
  }
}

function onOpenAutoFocus(event: Event) {
  emit("open-auto-focus", event);
}

function onCloseAutoFocus(event: Event) {
  emit("close-auto-focus", event);
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
        @focus-outside="onFocusOutside"
        @open-auto-focus="onOpenAutoFocus"
        @close-auto-focus="onCloseAutoFocus"
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
/**
 * Closing unmounts the panel immediately. A closing animation would keep the
 * panel mounted while its trigger is already gone, and the floating position
 * would then fall back to the page corner. If an exit animation is added,
 * panels opened from inside this one must close before it unmounts.
 */
.v-popover-content {
  &[data-state="open"] {
    animation: v-popover-fade-in 150ms ease-out;
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
