<script lang="ts" setup>
import { RadioGroupItem, RadioGroupRoot } from "reka-ui";
import {
  computed,
  nextTick,
  onBeforeUnmount,
  onMounted,
  shallowRef,
  useTemplateRef,
  watch,
} from "vue";
import type {
  SegmentedControlDirection,
  SegmentedControlItem,
  SegmentedControlSize,
  SegmentedControlValue,
} from "./types";

const props = withDefaults(
  defineProps<{
    modelValue?: SegmentedControlValue;
    items: SegmentedControlItem[];
    direction?: SegmentedControlDirection;
    size?: SegmentedControlSize;
    stretch?: boolean;
    disabled?: boolean;
    ariaLabel?: string;
  }>(),
  {
    modelValue: undefined,
    direction: "row",
    size: "md",
    stretch: false,
    disabled: false,
    ariaLabel: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value: SegmentedControlValue): void;
  (event: "change", value: SegmentedControlValue): void;
}>();

const rootRef = useTemplateRef<HTMLElement>("rootRef");
const thumbStyle = shallowRef<Record<string, string>>({ opacity: "0" });

const classes = computed(() => {
  return [
    `segmented-control-direction-${props.direction}`,
    `segmented-control-${props.size}`,
    {
      "segmented-control-stretch": props.stretch,
    },
  ];
});

function isIconOnly(item: SegmentedControlItem) {
  return !item.label;
}

function findEnabledIndex(from: number, step: 1 | -1) {
  const length = props.items.length;

  for (let offset = 1; offset <= length; offset++) {
    const index = (from + step * offset + length * 2) % length;
    if (!props.items[index]?.disabled) {
      return index;
    }
  }

  return -1;
}

function firstEnabledIndex() {
  return props.items.findIndex((item) => !item.disabled);
}

function lastEnabledIndex() {
  for (let index = props.items.length - 1; index >= 0; index--) {
    if (!props.items[index]?.disabled) {
      return index;
    }
  }

  return -1;
}

function handleArrowKeydown(event: KeyboardEvent) {
  const target = event.target as HTMLElement | null;
  if (target?.getAttribute("role") !== "radio") {
    return;
  }

  const radios = Array.from(
    rootRef.value?.querySelectorAll<HTMLElement>('[role="radio"]') ?? []
  );
  const currentIndex = radios.indexOf(target);
  if (currentIndex < 0) {
    return;
  }

  const isColumn = props.direction === "column";
  let targetIndex = -1;

  if (event.key === (isColumn ? "ArrowDown" : "ArrowRight")) {
    targetIndex = findEnabledIndex(currentIndex, 1);
  } else if (event.key === (isColumn ? "ArrowUp" : "ArrowLeft")) {
    targetIndex = findEnabledIndex(currentIndex, -1);
  } else if (event.key === "Home") {
    targetIndex = firstEnabledIndex();
  } else if (event.key === "End") {
    targetIndex = lastEnabledIndex();
  } else {
    return;
  }

  if (targetIndex < 0) {
    return;
  }

  const targetItem = props.items[targetIndex];
  if (!targetItem) {
    return;
  }

  event.preventDefault();
  event.stopPropagation();

  handleUpdate(targetItem.value);
  void nextTick(() => {
    radios[targetIndex]?.focus();
  });
}

function syncThumb() {
  const root = rootRef.value;

  if (!root) {
    return;
  }

  const active = root.querySelector<HTMLElement>(
    '[role="radio"][data-state="checked"]'
  );

  if (!active) {
    thumbStyle.value = { opacity: "0" };
    return;
  }

  thumbStyle.value = {
    opacity: "1",
    width: `${active.offsetWidth}px`,
    height: `${active.offsetHeight}px`,
    transform: `translate(${active.offsetLeft}px, ${active.offsetTop}px)`,
  };
}

function scheduleThumbSync() {
  void nextTick(syncThumb);
}

function handleUpdate(value: unknown) {
  if (typeof value !== "string" && typeof value !== "number") {
    return;
  }

  emit("update:modelValue", value);
  emit("change", value);
  scheduleThumbSync();
}

let resizeObserver: ResizeObserver | undefined;

onMounted(() => {
  scheduleThumbSync();

  if (typeof ResizeObserver !== "undefined" && rootRef.value) {
    resizeObserver = new ResizeObserver(scheduleThumbSync);
    resizeObserver.observe(rootRef.value);
  }
});

onBeforeUnmount(() => {
  resizeObserver?.disconnect();
});

watch(
  () => props.modelValue,
  () => scheduleThumbSync(),
  { flush: "post" }
);

watch(
  () => props.direction,
  () => scheduleThumbSync(),
  { flush: "post" }
);

watch(
  () => props.items,
  () => scheduleThumbSync(),
  { deep: true, flush: "post" }
);

if (import.meta.env.DEV) {
  if (!props.ariaLabel) {
    console.warn(
      "[VSegmentedControl] An accessible name is required. Pass `ariaLabel` to the control."
    );
  }

  for (const item of props.items) {
    if (!item.label && !item.ariaLabel) {
      console.warn(
        "[VSegmentedControl] An icon-only item must have an accessible name. Pass `ariaLabel` on the item."
      );
    }
  }
}
</script>
<template>
  <RadioGroupRoot
    as-child
    :disabled="disabled"
    :model-value="modelValue"
    :orientation="direction === 'column' ? 'vertical' : 'horizontal'"
    @update:model-value="handleUpdate"
  >
    <div
      ref="rootRef"
      :aria-label="ariaLabel"
      :class="classes"
      class="segmented-control"
      @keydown.capture="handleArrowKeydown"
    >
      <span
        :style="thumbStyle"
        aria-hidden="true"
        class="segmented-control-thumb"
      ></span>
      <RadioGroupItem
        v-for="item in items"
        :key="item.value"
        :aria-label="item.label ? undefined : item.ariaLabel"
        :disabled="item.disabled"
        :title="item.title"
        :value="item.value"
        class="segmented-control-item"
        :class="{ 'is-icon-only': isIconOnly(item) }"
      >
        <component :is="item.icon" v-if="item.icon" aria-hidden="true" />
        <span v-if="item.label" class="segmented-control-item-label">
          {{ item.label }}
        </span>
      </RadioGroupItem>
    </div>
  </RadioGroupRoot>
</template>
<style lang="scss">
.segmented-control {
  --segmented-padding: 2px;
  --segmented-item-height: 32px;
  --segmented-item-padding: 1rem;
  --segmented-font-size: 0.875rem;
  --segmented-icon-size: 1rem;
  --segmented-icon-item-width: calc(var(--segmented-item-height) * 1.5);

  @apply relative isolate inline-flex max-w-full items-center gap-0.5 overflow-x-auto rounded-base bg-gray-100;

  padding: var(--segmented-padding);
  scrollbar-width: none;

  &::-webkit-scrollbar {
    display: none;
  }

  &.segmented-control-sm {
    --segmented-item-height: 28px;
    --segmented-item-padding: 0.75rem;
    --segmented-font-size: 0.75rem;
    --segmented-icon-size: 0.875rem;
  }

  &.segmented-control-stretch {
    @apply flex w-full;

    &.segmented-control-direction-row {
      .segmented-control-item {
        flex: 1 1 0;
        min-width: 0;
      }
    }
  }

  &.segmented-control-direction-column {
    @apply inline-flex flex-col items-stretch overflow-y-auto overflow-x-hidden;

    .segmented-control-item {
      width: 100%;
      min-width: 0;
    }
  }

  .segmented-control-thumb {
    @apply pointer-events-none absolute left-0 top-0 z-0 rounded-sm bg-white shadow-sm;

    transition:
      transform 180ms cubic-bezier(0.2, 0.8, 0.2, 1),
      width 180ms cubic-bezier(0.2, 0.8, 0.2, 1),
      height 180ms cubic-bezier(0.2, 0.8, 0.2, 1),
      opacity 120ms ease;
  }

  .segmented-control-item {
    @apply relative z-[1] inline-flex min-w-8 cursor-pointer items-center justify-center gap-1.5 whitespace-nowrap rounded-sm font-medium text-gray-600 transition-colors duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-primary;

    height: var(--segmented-item-height);
    padding: 0 var(--segmented-item-padding);
    font-size: var(--segmented-font-size);

    &[data-state="checked"] {
      @apply text-secondary;
    }

    &:not(:disabled):hover {
      @apply text-gray-900;
    }

    &:disabled {
      @apply cursor-not-allowed text-gray-400;
    }

    &.is-icon-only {
      padding: 0;
    }

    > svg {
      @apply shrink-0;

      width: var(--segmented-icon-size);
      height: var(--segmented-icon-size);
    }
  }

  .segmented-control-item-label {
    @apply min-w-0 truncate;
  }

  &.segmented-control-direction-row .segmented-control-item.is-icon-only {
    width: var(--segmented-icon-item-width);
    min-width: var(--segmented-icon-item-width);
  }
}

@media (pointer: coarse) {
  .segmented-control,
  .segmented-control.segmented-control-sm {
    --segmented-padding: 4px;
    --segmented-item-height: 36px;
    --segmented-icon-size: 1rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .segmented-control .segmented-control-thumb {
    transition: none;
  }
}
</style>
