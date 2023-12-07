<script lang="ts" setup>
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import type { ArrowShow, Direction, Type } from "./interface";
import { useElementSize } from "@vueuse/core";
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
  handleClickArrow(index);
  emit("update:activeId", id);
  emit("change", id);
};

const tabbarItemsRef = ref<HTMLElement | undefined>();
const tabItemRefs = ref<HTMLElement[] | undefined>();
const itemWidthArr = ref<number[]>([]);
const indicatorRef = ref<HTMLElement | undefined>();
const arrowShow = ref<ArrowShow>({ left: false, right: false });
const { width: tabbarWidth } = useElementSize(tabbarItemsRef);

const scrollX = ref(0);

function handleHorizontalWheel(event: WheelEvent) {
  if (!tabbarItemsRef.value) {
    return;
  }
  const { scrollLeft, scrollWidth } = tabbarItemsRef.value;

  if (scrollX.value + event.deltaY < 0) {
    scrollX.value = 0;
  } else if (scrollX.value + event.deltaY >= scrollWidth - tabbarWidth.value) {
    scrollX.value = scrollWidth - tabbarWidth.value;
  } else {
    scrollX.value += event.deltaY;
  }

  const toLeft = event.deltaY < 0 && scrollLeft > 0;
  const toRight =
    event.deltaY > 0 && scrollLeft < scrollWidth - tabbarWidth.value;

  handleListenArrow();

  if (toLeft || toRight) {
    event.preventDefault();
    event.stopPropagation();
    tabbarItemsRef.value.scrollBy({ left: event.deltaY });
  }
}

// 保存每项 tab 宽度
function calculateItemWidth(n: Record<string, string>[] | undefined) {
  if (!tabbarItemsRef.value) return;
  if (tabItemRefs.value && tabItemRefs.value.length === n?.length) {
    for (const item of tabItemRefs.value) {
      itemWidthArr.value.push(item.offsetWidth);
    }
  }
  const { scrollWidth } = tabbarItemsRef.value;
  if (tabbarWidth.value < scrollWidth) {
    arrowShow.value.right = true;
  }
}

// 以单标签距离滚动
function handleClickArrow(
  index: number | undefined,
  prev: boolean | undefined = undefined
) {
  if (!tabbarItemsRef.value || !indicatorRef.value) return;
  if (tabbarItemsRef.value.scrollWidth <= tabbarItemsRef.value.clientWidth)
    return;
  const { scrollWidth, scrollLeft, clientWidth } = tabbarItemsRef.value;
  if (index === 0) {
    tabbarItemsRef.value.scrollTo({ left: 0, behavior: "smooth" });
    scrollX.value = 0;
    arrowShow.value.left = false;
    return;
  }
  if (index === itemWidthArr.value.length - 1) {
    tabbarItemsRef.value.scrollTo({
      left: scrollWidth - clientWidth,
      behavior: "smooth",
    });
    scrollX.value = scrollWidth - clientWidth;
    arrowShow.value.right = false;
    return;
  }
  let hiddenNum = 0;
  let totalWith = 0;
  let overWidth = 0;
  let scrollByX = 0;
  const lastItemWidth = itemWidthArr.value[itemWidthArr.value.length - 1];
  const firstItemWidth = itemWidthArr.value[0];
  if (prev) {
    if (!arrowShow.value.right) arrowShow.value.right = true;
    overWidth = scrollLeft;
    // 仅剩前两项待展现时，点击后直接滚动到第一项
    if (scrollX.value - firstItemWidth - itemWidthArr.value[1] <= 0) {
      arrowShow.value.left = false;
      tabbarItemsRef.value.scrollTo({
        left: 0,
        behavior: "smooth",
      });
      scrollX.value = 0;
      return;
    }
    for (let i = 0; i < itemWidthArr.value.length; i++) {
      const w = itemWidthArr.value[i];
      totalWith += w;
      if (totalWith >= overWidth) {
        hiddenNum = i;
        break;
      }
    }
    if (hiddenNum === 0) {
      scrollByX = -itemWidthArr.value[0];
      scrollX.value = 0;
      arrowShow.value.left = false;
    } else {
      scrollByX = -(
        itemWidthArr.value[hiddenNum] -
        totalWith +
        overWidth +
        itemWidthArr.value[hiddenNum - 1]
      );
      // listen: wheel-scroll arrowshow
      scrollX.value += scrollByX;
    }
  } else if (prev !== undefined && !prev) {
    if (!arrowShow.value.left) arrowShow.value.left = true;
    overWidth = scrollWidth - scrollLeft - clientWidth;
    // 仅剩最后两项待展现时，点击后直接滚动到最后一项
    if (
      scrollX.value +
        lastItemWidth +
        itemWidthArr.value[itemWidthArr.value.length - 2] >=
      scrollWidth - clientWidth
    ) {
      arrowShow.value.right = false;
      tabbarItemsRef.value.scrollBy({
        left: lastItemWidth + itemWidthArr.value[itemWidthArr.value.length - 2],
        behavior: "smooth",
      });
      scrollX.value = scrollWidth - clientWidth;
      return;
    }
    for (let i = itemWidthArr.value.length - 1; i >= 0; i--) {
      const w = itemWidthArr.value[i];
      totalWith += w;
      if (totalWith >= overWidth) {
        hiddenNum = i;
        break;
      }
    }

    if (hiddenNum === itemWidthArr.value.length - 1) {
      scrollByX = lastItemWidth;
      scrollX.value = scrollWidth - clientWidth;
      arrowShow.value.right = false;
    } else {
      scrollByX =
        itemWidthArr.value[hiddenNum] -
        (totalWith - overWidth) +
        itemWidthArr.value[hiddenNum + 1];
      scrollX.value += scrollByX;
    }
  }
  tabbarItemsRef.value.scrollBy({
    left: scrollByX,
    behavior: "smooth",
  });
}

function handleListenArrow() {
  if (!tabbarItemsRef.value) return;
  const { scrollWidth } = tabbarItemsRef.value;
  const firstItemWidth = itemWidthArr.value[0];
  const lastItemWidth = itemWidthArr.value[itemWidthArr.value.length - 1];

  if (scrollX.value >= scrollWidth - tabbarWidth.value - lastItemWidth / 2) {
    if (arrowShow.value) arrowShow.value.right = false;
  } else if (!arrowShow.value.right) {
    arrowShow.value.right = true;
  }

  if (scrollX.value > firstItemWidth / 2) {
    if (!arrowShow.value.left) arrowShow.value.left = true;
  } else if (arrowShow.value.left) {
    arrowShow.value.left = false;
  }
}

// tabbar 宽度变化时，滚动指示器的显示与隐藏
watch(tabbarWidth, () => {
  if (!tabbarItemsRef.value) return;
  if (tabbarItemsRef.value.scrollWidth > tabbarWidth.value) {
    handleListenArrow();
  } else {
    arrowShow.value = {
      left: false,
      right: false,
    };
    scrollX.value = 0;
  }
});

watch(() => props.items, calculateItemWidth);

onMounted(() => {
  tabbarItemsRef.value?.addEventListener("wheel", handleHorizontalWheel);
});

onUnmounted(() => {
  tabbarItemsRef.value?.removeEventListener("wheel", handleHorizontalWheel);
});
</script>
<template>
  <div :class="classes" class="tabbar-wrapper">
    <div
      ref="indicatorRef"
      :class="['indicator', 'left', arrowShow.left ? 'visible' : 'invisible']"
    >
      <div
        title="向前"
        class="arrow-left"
        @click="handleClickArrow(undefined, true)"
      >
        <IconArrowLeft />
      </div>
    </div>
    <div
      :class="['indicator', 'right', arrowShow.right ? 'visible' : 'invisible']"
    >
      <div
        title="向后"
        class="arrow-right"
        @click="handleClickArrow(undefined, false)"
      >
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
