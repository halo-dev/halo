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
      as-child
      as="div"
      :disabled="disabled"
      :aria-disabled="disabled || undefined"
    >
      <slot :open="isOpen" :show="show" :hide="hide" />
    </PopoverTrigger>
    <PopoverPortal>
      <PopoverContent
        :side="sideAndAlign.side"
        :align="sideAndAlign.align"
        :side-offset="offset"
        :class="[
          'v-popover-content z-[10000] max-w-[calc(100vw-2rem)]',
          'rounded-md border border-gray-200 bg-white p-1.5 text-gray-900 shadow-lg outline-none',
          { 'w-[var(--reka-popover-trigger-width)]': matchTriggerWidth },
          contentClass,
        ]"
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

  &[data-state="closed"] {
    pointer-events: none;
    animation: v-popover-fade-out 150ms ease-in;
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

@keyframes v-popover-fade-out {
  from {
    opacity: 1;
  }

  to {
    opacity: 0;
  }
}
</style>
