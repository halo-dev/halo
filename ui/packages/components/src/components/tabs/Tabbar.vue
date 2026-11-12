<script lang="ts" setup>
import { useElementSize, useEventListener } from "@vueuse/core";
import { computed, nextTick, shallowRef, useTemplateRef, watch } from "vue";
import { IconArrowLeft, IconArrowRight } from "../../icons/icons";
import type { TabsDirection, TabsType } from "./types";

const props = withDefaults(
  defineProps<{
    activeId?: number | string;
    items?: Array<Record<string, string>>;
    type?: TabsType;
    direction?: TabsDirection;
    idKey?: string;
    labelKey?: string;
  }>(),
  {
    activeId: undefined,
    items: undefined,
    type: "default",
    direction: "row",
    idKey: "id",
    labelKey: "label",
  }
);

const emit = defineEmits<{
  (event: "update:activeId", value: number | string): void;
  (event: "change", value: number | string): void;
}>();

const classes = computed(() => {
  return [`tabbar-${props.type}`, `tabbar-direction-${props.direction}`];
});

const handleChange = (id: number | string, index: number) => {
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
const { width: tabbarWidth } = useElementSize(tabbarItemsRef);
const { width: tabbarContentWidth } = useElementSize(tabbarTrackRef);

const maxScrollLeft = computed(() => {
  if (props.direction !== "row") return 0;
  return Math.max(0, tabbarContentWidth.value - tabbarWidth.value);
});
const canScrollLeft = computed(() => {
  return maxScrollLeft.value > 0 && scrollLeft.value > 1;
});
const canScrollRight = computed(() => {
  return maxScrollLeft.value - scrollLeft.value > 1;
});

const activeIndex = computed(() => {
  return (
    props.items?.findIndex((item) => item[props.idKey] === props.activeId) ?? -1
  );
});

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
  if (props.direction !== "row" || index < 0 || index !== activeIndex.value) {
    return;
  }

  const container = tabbarItemsRef.value;
  const item = tabbarTrackRef.value?.children[index] as HTMLElement | undefined;
  if (!container || !item) return;

  const containerRect = container.getBoundingClientRect();
  const itemRect = item.getBoundingClientRect();
  const arrowOffset = 40;
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

function handleHorizontalWheel(event: WheelEvent) {
  const container = tabbarItemsRef.value;
  if (!container || props.direction !== "row") return;

  const { scrollLeft, scrollWidth, clientWidth } = container;
  const maxScrollLeft = Math.max(0, scrollWidth - clientWidth);
  const toLeft = event.deltaY < 0 && scrollLeft > 1;
  const toRight = event.deltaY > 0 && maxScrollLeft - scrollLeft > 1;

  if (toLeft || toRight) {
    event.preventDefault();
    event.stopPropagation();
    container.scrollBy({ left: event.deltaY });
  }
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
  [tabbarWidth, tabbarContentWidth],
  () => {
    updateScrollLeft();
    void scrollTabIntoView(activeIndex.value, "auto");
  },
  { flush: "post", immediate: true }
);

useEventListener(tabbarItemsRef, "scroll", updateScrollLeft, {
  passive: true,
});
useEventListener(tabbarItemsRef, "wheel", handleHorizontalWheel, {
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
        :disabled="!canScrollLeft"
        aria-label="向左滚动标签"
        class="tabbar-arrow"
        title="向左滚动标签"
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
        :disabled="!canScrollRight"
        aria-label="向右滚动标签"
        class="tabbar-arrow"
        title="向右滚动标签"
        type="button"
        @click="handleClickArrow(false)"
      >
        <IconArrowRight />
      </button>
    </div>
    <div
      ref="tabbarItemsRef"
      :tabindex="maxScrollLeft > 0 ? 0 : undefined"
      aria-label="标签列表"
      class="tabbar-items"
      role="group"
    >
      <div ref="tabbarTrackRef" class="tabbar-items-track">
        <div
          v-for="(item, index) in items"
          :key="item[idKey]"
          :class="{ 'tabbar-item-active': item[idKey] === activeId }"
          class="tabbar-item"
          @click="handleChange(item[idKey], index)"
        >
          <div v-if="item.icon" class="tabbar-item-icon">
            <component :is="item.icon" />
          </div>
          <div v-if="item[labelKey]" class="tabbar-item-label">
            {{ item[labelKey] }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<style lang="scss">
.tabbar-wrapper {
  --tabbar-fade-color: theme("colors.white");

  @apply relative;

  .indicator {
    @apply pointer-events-none absolute top-0 z-10 flex h-full w-16 items-center transition-opacity duration-150 ease-out;

    &.left {
      @apply left-0 justify-start;
      background: linear-gradient(
        to left,
        transparent,
        var(--tabbar-fade-color) 70%
      );
    }

    &.right {
      @apply right-0 justify-end;
      background: linear-gradient(
        to right,
        transparent,
        var(--tabbar-fade-color) 70%
      );
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
    @apply overflow-x-auto py-0.5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-1;
    scrollbar-width: none;

    &::-webkit-scrollbar {
      display: none;
    }
  }

  .tabbar-items-track {
    @apply flex items-center;
  }

  .tabbar-item {
    @apply inline-flex h-9 cursor-pointer justify-center gap-2 self-center whitespace-nowrap text-sm transition-all;

    .tabbar-item-label,
    .tabbar-item-icon {
      @apply self-center;
    }
  }

  &.tabbar-default {
    border-bottom-width: 2px;
    @apply border-b-gray-100;

    .tabbar-items {
      margin-bottom: -4px;
    }

    .tabbar-items-track {
      justify-content: flex-start;
    }

    .tabbar-item {
      @apply border-b-gray-100 px-5 py-1;

      border-bottom-width: 2px;

      &.tabbar-item-active {
        @apply border-b-secondary text-secondary;
      }
    }
  }

  &.tabbar-pills {
    .tabbar-items-track {
      @apply justify-start gap-1;
    }

    .tabbar-item {
      @apply rounded-base px-6 py-1 opacity-70;

      &.tabbar-item-active {
        @apply bg-gray-100 opacity-100;
      }

      &:hover {
        @apply bg-gray-100;
      }
    }
  }

  &.tabbar-outline {
    --tabbar-fade-color: theme("colors.gray.100");

    @apply rounded-base bg-gray-100 px-1 py-0.5;

    .tabbar-items-track {
      @apply justify-start gap-1;
    }

    .tabbar-item {
      @apply rounded-sm px-6 py-1 opacity-70;

      &.tabbar-item-active {
        @apply bg-white opacity-100 shadow-sm;
      }

      &:hover {
        @apply bg-white;
      }
    }
  }

  &.tabbar-direction-row {
    .tabbar-items-track {
      @apply w-max min-w-full flex-row;
    }
  }

  &.tabbar-direction-column {
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
    .tabbar-arrow {
      @apply transition-none;
    }
  }
}
</style>
