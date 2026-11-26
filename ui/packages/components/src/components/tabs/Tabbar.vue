<script lang="ts" setup>
import { useElementSize, useEventListener } from "@vueuse/core";
import {
  computed,
  inject,
  nextTick,
  onMounted,
  shallowRef,
  useTemplateRef,
  watch,
} from "vue";
import { IconArrowLeft, IconArrowRight } from "../../icons/icons";
import { useComponentsI18n } from "../../locales";
import type { TabbarItem, TabsDirection, TabsType } from "./types";

// Matches the arrow button width (2rem) plus its horizontal margin (0.25rem × 2).
const ARROW_OFFSET = 40;

const props = withDefaults(
  defineProps<{
    activeId?: number | string;
    items?: TabbarItem[];
    type?: TabsType;
    direction?: TabsDirection;
    idKey?: string;
    labelKey?: string;
    flush?: boolean;
  }>(),
  {
    activeId: undefined,
    items: undefined,
    type: "default",
    direction: "row",
    idKey: "id",
    labelKey: "label",
    flush: false,
  }
);

const emit = defineEmits<{
  (event: "update:activeId", value: number | string): void;
  (event: "change", value: number | string): void;
}>();

const { t } = useComponentsI18n();

const classes = computed(() => {
  return [
    `tabbar-${props.type}`,
    `tabbar-direction-${props.direction}`,
    { "is-flush": props.flush },
  ];
});

const itemList = computed(() => props.items ?? []);

function getItemId(item: TabbarItem): string | number | undefined {
  return item[props.idKey] as string | number | undefined;
}

function getItemLabel(item: TabbarItem): string | undefined {
  const label = item[props.labelKey];
  return typeof label === "string" ? label : undefined;
}

function isItemDisabled(item: TabbarItem | undefined): boolean {
  return item?.disabled === true;
}

function hasBadge(item: TabbarItem): boolean {
  return item.badge !== undefined && item.badge !== null && item.badge !== "";
}

const selectedIndex = computed(() => {
  return itemList.value.findIndex(
    (item) => getItemId(item) === props.activeId && !isItemDisabled(item)
  );
});

const firstEnabledIndex = computed(() => {
  return itemList.value.findIndex((item) => !isItemDisabled(item));
});

const lastEnabledIndex = computed(() => {
  for (let index = itemList.value.length - 1; index >= 0; index--) {
    if (!isItemDisabled(itemList.value[index])) {
      return index;
    }
  }
  return -1;
});

const resolvedIndex = computed(() => {
  return selectedIndex.value >= 0
    ? selectedIndex.value
    : firstEnabledIndex.value;
});

const resolvedActiveId = computed(() => {
  const index = resolvedIndex.value;
  return index >= 0 ? getItemId(itemList.value[index]) : undefined;
});

function syncActiveId() {
  const id = resolvedActiveId.value;
  if (id !== undefined && id !== props.activeId) {
    emit("update:activeId", id);
  }
}

onMounted(syncActiveId);

watch(resolvedActiveId, syncActiveId, { flush: "post" });

function findEnabledIndex(from: number, step: 1 | -1): number {
  const length = itemList.value.length;

  for (let offset = 1; offset <= length; offset++) {
    const index = (from + step * offset + length * 2) % length;
    if (!isItemDisabled(itemList.value[index])) {
      return index;
    }
  }

  return -1;
}

const handleChange = (id: string | number | undefined, index: number) => {
  if (id === undefined || isItemDisabled(itemList.value[index])) {
    return;
  }

  if (id === props.activeId) {
    void scrollTabIntoView(index, getScrollBehavior());
  }
  emit("update:activeId", id);
  emit("change", id);
};

const tabbarItemsRef = useTemplateRef<HTMLElement>("tabbarItemsRef");
const tabbarTrackRef = useTemplateRef<HTMLElement>("tabbarTrackRef");
const leftArrowRef = useTemplateRef<HTMLButtonElement>("leftArrowRef");
const rightArrowRef = useTemplateRef<HTMLButtonElement>("rightArrowRef");
const scrollLeft = shallowRef(0);
const { width: tabbarWidth, height: tabbarHeight } =
  useElementSize(tabbarItemsRef);
const { width: tabbarContentWidth, height: tabbarContentHeight } =
  useElementSize(tabbarTrackRef);

const maxScrollLeft = computed(() => {
  if (props.direction !== "row") return 0;
  return Math.max(0, tabbarContentWidth.value - tabbarWidth.value);
});

const maxScrollTop = computed(() => {
  if (props.direction !== "column") return 0;
  return Math.max(0, tabbarContentHeight.value - tabbarHeight.value);
});

const isScrollable = computed(() => {
  return props.direction === "column"
    ? maxScrollTop.value > 1
    : maxScrollLeft.value > 1;
});

const canScrollLeft = computed(() => {
  return maxScrollLeft.value > 0 && scrollLeft.value > 1;
});
const canScrollRight = computed(() => {
  return maxScrollLeft.value - scrollLeft.value > 1;
});

const activeIndex = computed(() => resolvedIndex.value);

const tabsId = inject<string | undefined>("tabsId", undefined);

function getTabElementId(item: TabbarItem) {
  const id = getItemId(item);
  return tabsId && id !== undefined ? `${tabsId}-tab-${id}` : undefined;
}

function getPanelElementId(item: TabbarItem) {
  const id = getItemId(item);
  return tabsId && id !== undefined ? `${tabsId}-panel-${id}` : undefined;
}

const tabStopIndex = computed(() => resolvedIndex.value);

function handleKeydown(event: KeyboardEvent, index: number) {
  const items = itemList.value;
  if (!items.length) {
    return;
  }

  const isColumn = props.direction === "column";
  let targetIndex = -1;

  if (event.key === (isColumn ? "ArrowDown" : "ArrowRight")) {
    targetIndex = findEnabledIndex(index, 1);
  } else if (event.key === (isColumn ? "ArrowUp" : "ArrowLeft")) {
    targetIndex = findEnabledIndex(index, -1);
  } else if (event.key === "Home") {
    targetIndex = firstEnabledIndex.value;
  } else if (event.key === "End") {
    targetIndex = lastEnabledIndex.value;
  } else if (event.key === "Enter" || event.key === " ") {
    if (!isItemDisabled(items[index])) {
      event.preventDefault();
      handleChange(getItemId(items[index]), index);
    }
    return;
  } else {
    return;
  }

  event.preventDefault();

  if (targetIndex < 0) {
    return;
  }

  const targetItem = items[targetIndex];
  if (!targetItem) {
    return;
  }

  handleChange(getItemId(targetItem), targetIndex);
  (
    tabbarTrackRef.value?.children[targetIndex] as HTMLElement | undefined
  )?.focus();
}

function updateScrollLeft() {
  scrollLeft.value = tabbarItemsRef.value?.scrollLeft ?? 0;
}

function getScrollBehavior(): ScrollBehavior {
  if (
    typeof window !== "undefined" &&
    window.matchMedia?.("(prefers-reduced-motion: reduce)").matches
  ) {
    return "auto";
  }
  return "smooth";
}

async function scrollTabIntoView(index: number, behavior: ScrollBehavior) {
  await nextTick();
  if (index < 0 || index !== activeIndex.value) {
    return;
  }

  const container = tabbarItemsRef.value;
  const item = tabbarTrackRef.value?.children[index] as HTMLElement | undefined;
  if (!container || !item) return;

  const containerRect = container.getBoundingClientRect();
  const itemRect = item.getBoundingClientRect();

  if (props.direction === "column") {
    let top = 0;
    if (itemRect.top < containerRect.top) {
      top = itemRect.top - containerRect.top;
    } else if (itemRect.bottom > containerRect.bottom) {
      top = itemRect.bottom - containerRect.bottom;
    }

    if (Math.abs(top) < 1) return;
    container.scrollBy({ top, behavior });
    return;
  }

  const arrowOffset = ARROW_OFFSET;
  const visibleLeft =
    containerRect.left + (canScrollLeft.value ? arrowOffset : 0);
  const visibleRight =
    containerRect.right - (canScrollRight.value ? arrowOffset : 0);

  let left = 0;
  if (itemRect.left < visibleLeft) {
    left = itemRect.left - visibleLeft;
  } else if (itemRect.right > visibleRight) {
    left = itemRect.right - visibleRight;
  }

  if (Math.abs(left) < 1) return;
  container.scrollBy({ left, behavior });
}

function getWheelDelta(event: WheelEvent, container: HTMLElement): number {
  const horizontal = Math.abs(event.deltaX) > Math.abs(event.deltaY);
  const raw = horizontal ? event.deltaX : event.deltaY;
  const unit =
    event.deltaMode === 1
      ? 16
      : event.deltaMode === 2
        ? horizontal
          ? container.clientWidth
          : container.clientHeight
        : 1;

  return raw * unit;
}

function handleWheel(event: WheelEvent) {
  const container = tabbarItemsRef.value;
  if (!container) return;

  if (props.direction === "column") {
    return;
  }

  if (maxScrollLeft.value <= 1) return;

  // Only take over gestures that are meant to scroll horizontally. A plain
  // vertical wheel keeps scrolling the page (and lets native overflow handle
  // shift + wheel / trackpad swipes).
  if (!event.shiftKey && Math.abs(event.deltaX) <= Math.abs(event.deltaY)) {
    return;
  }

  const delta = getWheelDelta(event, container);
  const atStart = container.scrollLeft <= 1;
  const atEnd = maxScrollLeft.value - container.scrollLeft <= 1;

  if ((delta < 0 && atStart) || (delta > 0 && atEnd)) {
    return;
  }

  event.preventDefault();
  event.stopPropagation();
  container.scrollBy({ left: delta });
}

function handleClickArrow(prev: boolean) {
  const container = tabbarItemsRef.value;
  if (!container || props.direction !== "row") return;

  const distance = container.clientWidth * 0.8;
  if (distance <= 0) return;

  const left = prev ? -distance : distance;
  const maxScrollLeft = Math.max(
    0,
    container.scrollWidth - container.clientWidth
  );
  const targetScrollLeft = Math.min(
    Math.max(0, container.scrollLeft + left),
    maxScrollLeft
  );
  container.scrollBy({ left, behavior: getScrollBehavior() });

  const sourceArrow = prev ? leftArrowRef.value : rightArrowRef.value;
  const targetArrow = prev ? rightArrowRef.value : leftArrowRef.value;
  const reachesBoundary = prev
    ? targetScrollLeft <= 1
    : maxScrollLeft - targetScrollLeft <= 1;

  if (reachesBoundary && document.activeElement === sourceArrow) {
    const focusTarget =
      targetArrow && !targetArrow.disabled ? targetArrow : container;
    focusTarget.focus({ preventScroll: true });
  }
}

watch(
  activeIndex,
  (index) => {
    void scrollTabIntoView(index, getScrollBehavior());
  },
  { flush: "post" }
);

watch(
  [tabbarWidth, tabbarHeight, tabbarContentWidth, tabbarContentHeight],
  () => {
    updateScrollLeft();
    void scrollTabIntoView(activeIndex.value, "auto");
  },
  { flush: "post", immediate: true }
);

useEventListener(tabbarItemsRef, "scroll", updateScrollLeft, {
  passive: true,
});
useEventListener(tabbarItemsRef, "wheel", handleWheel, {
  passive: false,
});
</script>
<template>
  <div :class="classes" class="tabbar-wrapper">
    <div
      :aria-hidden="!canScrollLeft"
      :class="['indicator', 'left', canScrollLeft ? 'is-visible' : 'is-hidden']"
    >
      <button
        ref="leftArrowRef"
        :aria-label="t('tabs.scroll_left')"
        :disabled="!canScrollLeft"
        :title="t('tabs.scroll_left')"
        class="tabbar-arrow"
        type="button"
        @click="handleClickArrow(true)"
      >
        <IconArrowLeft />
      </button>
    </div>
    <div
      :aria-hidden="!canScrollRight"
      :class="[
        'indicator',
        'right',
        canScrollRight ? 'is-visible' : 'is-hidden',
      ]"
    >
      <button
        ref="rightArrowRef"
        :aria-label="t('tabs.scroll_right')"
        :disabled="!canScrollRight"
        :title="t('tabs.scroll_right')"
        class="tabbar-arrow"
        type="button"
        @click="handleClickArrow(false)"
      >
        <IconArrowRight />
      </button>
    </div>
    <div
      ref="tabbarItemsRef"
      :aria-label="isScrollable ? t('tabs.list') : undefined"
      :class="{
        'is-scrollable': isScrollable,
        'is-fade-left': direction === 'row' && canScrollLeft,
        'is-fade-right': direction === 'row' && canScrollRight,
      }"
      :role="isScrollable ? 'group' : undefined"
      :tabindex="isScrollable ? 0 : undefined"
      class="tabbar-items"
    >
      <div
        ref="tabbarTrackRef"
        :aria-label="t('tabs.list')"
        :aria-orientation="direction === 'column' ? 'vertical' : 'horizontal'"
        class="tabbar-items-track"
        role="tablist"
      >
        <div
          v-for="(item, index) in itemList"
          :id="getTabElementId(item)"
          :key="getItemId(item) ?? index"
          :aria-controls="getPanelElementId(item)"
          :aria-disabled="item.disabled ? 'true' : undefined"
          :aria-label="getItemLabel(item) ? undefined : item.ariaLabel"
          :aria-selected="index === resolvedIndex"
          :class="{
            'tabbar-item-active': index === resolvedIndex,
            'is-disabled': item.disabled,
          }"
          :tabindex="index === tabStopIndex && !item.disabled ? 0 : -1"
          class="tabbar-item"
          role="tab"
          @click="handleChange(getItemId(item), index)"
          @keydown="handleKeydown($event, index)"
        >
          <div v-if="item.icon" class="tabbar-item-icon">
            <component :is="item.icon" aria-hidden="true" />
          </div>
          <div v-if="getItemLabel(item)" class="tabbar-item-label">
            {{ getItemLabel(item) }}
          </div>
          <span v-if="hasBadge(item)" class="tabbar-item-badge">
            {{ item.badge }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
<style lang="scss">
.tabbar-wrapper {
  @apply relative;

  .indicator {
    @apply pointer-events-none absolute top-0 z-10 flex h-full w-16 items-center transition-opacity duration-150 ease-out;
    border-radius: inherit;

    &.left {
      @apply left-0 justify-start;
    }

    &.right {
      @apply right-0 justify-end;
    }

    &.is-hidden {
      @apply opacity-0;

      .tabbar-arrow {
        @apply pointer-events-none;
      }
    }

    .tabbar-arrow {
      @apply pointer-events-auto mx-1 flex h-8 w-8 shrink-0 cursor-pointer select-none items-center justify-center rounded-full border border-gray-200 bg-white text-gray-500 shadow-sm transition-[color,background-color,border-color,box-shadow] duration-150 hover:bg-gray-50 hover:text-gray-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-1 active:bg-gray-200;

      svg {
        @apply h-4 w-4;
      }
    }
  }

  .tabbar-items {
    @apply overflow-x-auto overscroll-x-contain py-0.5 focus-visible:outline-none;
    scrollbar-width: none;

    &::-webkit-scrollbar {
      display: none;
    }

    &.is-fade-left {
      -webkit-mask-image: linear-gradient(to right, transparent, #000 4rem);
      mask-image: linear-gradient(to right, transparent, #000 4rem);
    }

    &.is-fade-right {
      -webkit-mask-image: linear-gradient(to left, transparent, #000 4rem);
      mask-image: linear-gradient(to left, transparent, #000 4rem);
    }

    &.is-fade-left.is-fade-right {
      -webkit-mask-image: linear-gradient(
        to right,
        transparent,
        #000 4rem,
        #000 calc(100% - 4rem),
        transparent
      );
      mask-image: linear-gradient(
        to right,
        transparent,
        #000 4rem,
        #000 calc(100% - 4rem),
        transparent
      );
    }
  }

  // The edge fade is a mask on the scroller, which would fade its focus ring
  // too. Draw the ring on the wrapper so it stays whole while the list scrolls.
  &:has(> .tabbar-items:focus-visible) {
    outline: 2px solid theme("colors.primary");
    outline-offset: -2px;
  }

  .tabbar-items-track {
    @apply flex items-center;
  }

  .tabbar-item {
    @apply inline-flex h-9 cursor-pointer justify-center gap-2 self-center whitespace-nowrap text-sm transition-[color,background-color,border-color,opacity] duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-primary;

    .tabbar-item-label,
    .tabbar-item-icon {
      @apply self-center;
    }

    .tabbar-item-badge {
      @apply inline-flex h-4 min-w-4 items-center justify-center self-center rounded-full bg-gray-200 px-1 text-[11px] font-medium leading-none text-gray-600;
    }

    &.is-disabled {
      @apply cursor-not-allowed text-gray-400;

      .tabbar-item-badge {
        @apply bg-gray-100 text-gray-400;
      }
    }
  }

  &.tabbar-default {
    --tabbar-line-width: 2px;

    border-bottom-width: var(--tabbar-line-width);
    @apply border-b-gray-100;

    .tabbar-items {
      /* Align the active underline with the bar's bottom line. */
      margin-bottom: calc(-1 * (var(--tabbar-line-width) + 0.125rem));
    }

    .tabbar-items-track {
      justify-content: flex-start;
    }

    .tabbar-item {
      @apply border-b-gray-100 px-5 py-1;

      border-bottom-width: var(--tabbar-line-width);

      &.tabbar-item-active {
        @apply border-b-secondary text-secondary;
      }

      &.is-disabled.tabbar-item-active {
        @apply border-b-gray-300 text-gray-400;
      }
    }
  }

  &.tabbar-pills {
    .tabbar-items-track {
      @apply justify-start gap-1;
    }

    .tabbar-item {
      @apply rounded-base px-6 py-1 text-gray-600;

      &.tabbar-item-active {
        @apply bg-gray-100 text-gray-900;
      }

      &:not(.is-disabled):hover {
        @apply bg-gray-100;
      }

      &.is-disabled {
        @apply bg-transparent text-gray-400;
      }
    }
  }

  &.tabbar-outline {
    @apply rounded-base bg-gray-100 px-1 py-0.5;

    .tabbar-items-track {
      @apply justify-start gap-1;
    }

    .tabbar-item {
      @apply rounded-sm px-6 py-1 text-gray-600;

      &.tabbar-item-active {
        @apply bg-white text-gray-900 shadow-sm;
      }

      &:not(.is-disabled):hover {
        @apply bg-white;
      }

      &.is-disabled {
        @apply bg-transparent text-gray-400;
      }
    }
  }

  // Full-bleed form for card headers: the bar carries the top corners itself,
  // so callers no longer need `!rounded-none` together with an outer clipping
  // parent. The variant's horizontal padding stays: with no padding a tab at
  // either edge sits directly on the card border.
  &.is-flush {
    @apply rounded-t-base;
    border-bottom-right-radius: 0;
    border-bottom-left-radius: 0;
  }

  &.tabbar-direction-row {
    .tabbar-items-track {
      @apply w-max min-w-full flex-row;
    }
  }

  &.tabbar-direction-column {
    .tabbar-items {
      @apply overflow-y-auto overflow-x-hidden overscroll-y-contain;
    }

    .tabbar-items-track {
      @apply w-full flex-col;
    }

    .tabbar-item {
      width: 100%;
    }

    &.tabbar-default {
      border-bottom-width: 0;
      @apply border-b-0;
      border-right-width: 2px;
      @apply border-r-gray-100;

      .tabbar-items {
        margin-bottom: 0;
        margin-right: -2px;
      }

      .tabbar-item {
        border-bottom-width: 0;
        border-right-width: 2px;

        &.tabbar-item-active {
          @apply border-r-secondary;
        }
      }
    }
  }

  @media (prefers-reduced-motion: reduce) {
    .indicator,
    .tabbar-arrow,
    .tabbar-item {
      @apply transition-none;
    }
  }
}
</style>
