<script lang="ts" setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import type { ArrowShow, Direction, Type } from "./interface";
import type { ComputedRef } from "vue";
import { useElementSize, useThrottleFn } from "@vueuse/core";
import { IconArrowLeft, IconArrowRight } from "../../icons/icons";

const props = withDefaults(
  defineProps<{
    activeId?: number | string;
    items?: Array<Record<string, string>>;
    type?: Type;
    direction?: Direction;
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
  handleClickTabItem(index);
  emit("update:activeId", id);
  emit("change", id);
};

const tabbarItemsRef = ref<HTMLElement | undefined>();
const tabItemRefs = ref<HTMLElement[] | undefined>();
const itemWidthArr = ref<number[]>([]);
const indicatorRef = ref<HTMLElement | undefined>();
const arrowFlag = ref(false);
const { width: tabbarWidth } = useElementSize(tabbarItemsRef);

const arrowShow: ComputedRef<ArrowShow> = computed(() => {
  const show: ArrowShow = { left: false, right: false };
  if (!tabbarItemsRef.value) return show;
  void arrowFlag.value;
  void tabbarWidth.value;
  const { scrollWidth, scrollLeft, clientWidth } = tabbarItemsRef.value;
  if (scrollWidth > clientWidth) {
    if (scrollLeft < scrollWidth - clientWidth) {
      show.right = true;
    }
    if (scrollLeft > 20) {
      show.left = true;
    }
  }
  return show;
});

function handleHorizontalWheel(event: WheelEvent) {
  if (!tabbarItemsRef.value) {
    return;
  }
  const { scrollLeft, scrollWidth, clientWidth } = tabbarItemsRef.value;
  const toLeft = event.deltaY < 0 && scrollLeft > 0;
  const toRight = event.deltaY > 0 && scrollLeft < scrollWidth - clientWidth;

  if (toLeft || toRight) {
    event.preventDefault();
    event.stopPropagation();
    tabbarItemsRef.value.scrollBy({ left: event.deltaY });
  }
}

function saveItemsWidth() {
  if (!tabbarItemsRef.value || !tabItemRefs.value) return;
  itemWidthArr.value = [];
  for (const item of tabItemRefs.value) {
    itemWidthArr.value.push(item.offsetWidth);
  }
  arrowFlag.value = !arrowFlag.value;
}

function handleClickTabItem(index: number) {
  if (!tabbarItemsRef.value || !indicatorRef.value) return;
  const { scrollWidth, clientWidth } = tabbarItemsRef.value;
  if (scrollWidth <= clientWidth) return;
  if (index === 0) {
    tabbarItemsRef.value.scrollTo({ left: 0, behavior: "smooth" });
    return;
  }
  if (index === itemWidthArr.value.length - 1) {
    tabbarItemsRef.value.scrollTo({
      left: scrollWidth - clientWidth,
      behavior: "smooth",
    });
    return;
  }
}

function handleClickArrow(prev: boolean) {
  if (!tabbarItemsRef.value || !indicatorRef.value || !tabItemRefs.value)
    return;
  const { scrollWidth, scrollLeft, clientWidth } = tabbarItemsRef.value;
  if (scrollWidth <= clientWidth) return;
  if (!itemWidthArr.value[0]) {
    itemWidthArr.value = [];
    for (const item of tabItemRefs.value) {
      itemWidthArr.value.push(item.offsetWidth);
    }
  }
  let hiddenNum = 0;
  let totalWith = 0;
  let scrollByX = 0;
  const lastItemWidth = itemWidthArr.value[itemWidthArr.value.length - 1];
  if (prev) {
    for (let i = 0; i < itemWidthArr.value.length; i++) {
      const w = itemWidthArr.value[i];
      totalWith += w;
      if (totalWith >= scrollLeft) {
        hiddenNum = i;
        break;
      }
    }
    if (hiddenNum === 0) {
      scrollByX = -itemWidthArr.value[0];
    } else {
      scrollByX = -(
        itemWidthArr.value[hiddenNum] -
        totalWith +
        scrollLeft +
        itemWidthArr.value[hiddenNum - 1]
      );
    }
  } else {
    const overWidth = scrollWidth - scrollLeft - clientWidth;
    for (let i = itemWidthArr.value.length - 1; i >= 0; i--) {
      const w = itemWidthArr.value[i];
      totalWith += w;
      if (totalWith >= overWidth) {
        hiddenNum = i;
        break;
      }
    }

    if (hiddenNum === itemWidthArr.value.length - 1) {
      scrollByX =
        lastItemWidth + itemWidthArr.value[itemWidthArr.value.length - 1];
    } else {
      scrollByX =
        itemWidthArr.value[hiddenNum] -
        (totalWith - overWidth) +
        itemWidthArr.value[hiddenNum + 1];
    }
  }
  tabbarItemsRef.value.scrollBy({
    left: scrollByX,
    behavior: "smooth",
  });
}

const handleScroll = useThrottleFn(
  () => {
    arrowFlag.value = !arrowFlag.value;
  },
  100,
  true
);

watch(() => tabItemRefs.value?.length, saveItemsWidth);

onMounted(() => {
  tabbarItemsRef.value?.addEventListener("wheel", handleHorizontalWheel);
  tabbarItemsRef.value?.addEventListener("scroll", handleScroll);
});

onUnmounted(() => {
  tabbarItemsRef.value?.removeEventListener("wheel", handleHorizontalWheel);
  tabbarItemsRef.value?.removeEventListener("scroll", handleScroll);
});
</script>
<template>
  <div :class="classes" class="tabbar-wrapper">
    <div
      ref="indicatorRef"
      :class="['indicator', 'left', arrowShow.left ? 'visible' : 'invisible']"
    >
      <div title="向前" class="arrow-left" @click="handleClickArrow(true)">
        <IconArrowLeft />
      </div>
    </div>
    <div
      :class="['indicator', 'right', arrowShow.right ? 'visible' : 'invisible']"
    >
      <div title="向后" class="arrow-right" @click="handleClickArrow(false)">
        <IconArrowRight />
      </div>
    </div>
    <div ref="tabbarItemsRef" class="tabbar-items">
      <div
        v-for="(item, index) in items"
        :key="index"
        ref="tabItemRefs"
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
</template>
<style lang="scss">
.tabbar-wrapper {
  @apply relative;
  .indicator {
    @apply absolute
    top-0
    z-10
    w-20
    h-full
    flex
    items-center
    from-transparent
    from-10%
    via-white/80
    via-30%
    to-white
    to-70%
    pt-1
    pointer-events-none
    pb-1.5;

    &.left {
      @apply left-0
      justify-start
      bg-gradient-to-l;
    }
    &.right {
      @apply right-0
      justify-end
      bg-gradient-to-r;
    }
    .arrow-left,
    .arrow-right {
      @apply w-10
      h-9
      flex
      justify-center
      items-center
      pointer-events-auto
      cursor-pointer
      select-none;
      svg {
        font-size: 1.5em;
      }
    }
  }

  .tabbar-items {
    @apply flex
    items-center
    flex-row
    overflow-x-auto
    py-0.5;

    &::-webkit-scrollbar-track-piece {
      background-color: #f8f8f8;
      -webkit-border-radius: 2em;
      -moz-border-radius: 2em;
      border-radius: 2em;
    }

    &::-webkit-scrollbar {
      width: 4px;
      height: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background-color: #ddd;
      background-clip: padding-box;
      -webkit-border-radius: 2em;
      -moz-border-radius: 2em;
      border-radius: 2em;
    }

    &::-webkit-scrollbar-thumb:hover {
      background-color: #bbb;
    }
  }

  .tabbar-item {
    @apply inline-flex
    cursor-pointer
    self-center
    transition-all
    text-sm
    justify-center
    gap-2
    h-9
    whitespace-nowrap;

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
      justify-content: flex-start;
    }

    .tabbar-item {
      @apply px-5
      py-1
      border-b-gray-100;

      border-bottom-width: 2px;

      &.tabbar-item-active {
        @apply text-secondary
        border-b-secondary;
      }
    }
  }

  &.tabbar-pills {
    .tabbar-items {
      @apply gap-1;
      justify-content: flex-start;
    }

    .tabbar-item {
      @apply px-6
      py-1
      opacity-70
      rounded-base;

      &.tabbar-item-active {
        @apply bg-gray-100
        opacity-100;
      }

      &:hover {
        @apply bg-gray-100;
      }
    }
  }

  &.tabbar-outline {
    @apply px-1
    py-0.5
    bg-gray-100
    rounded-base;

    .tabbar-items {
      @apply gap-1
      justify-start;
    }

    .tabbar-item {
      @apply px-6
      py-1
      opacity-70
      rounded-sm;

      &.tabbar-item-active {
        @apply bg-white
        opacity-100
        shadow-sm;
      }

      &:hover {
        @apply bg-white;
      }
    }
  }

  &.tabbar-direction-row {
    .tabbar-items {
      @apply flex-row;
    }
  }

  &.tabbar-direction-column {
    .tabbar-items {
      @apply flex-col;
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
}
</style>
