<script lang="ts" setup>
import {
  ScrollAreaCorner,
  ScrollAreaRoot,
  ScrollAreaScrollbar,
  ScrollAreaThumb,
  ScrollAreaViewport,
} from "reka-ui";
import { useTemplateRef } from "vue";
import type {
  VScrollAreaClassValue,
  VScrollAreaOrientation,
  VScrollAreaType,
} from "./types";

defineOptions({
  inheritAttrs: false,
});

withDefaults(
  defineProps<{
    /**
     * Scrollbar visibility, mirroring the macOS scrollbar preference.
     * `scroll` shows the scrollbar while scrolling and fades it out after
     * `scrollHideDelay`, which matches the previous OverlayScrollbars setup.
     */
    type?: VScrollAreaType;
    /**
     * Delay in milliseconds before a `scroll` or `hover` scrollbar hides.
     */
    scrollHideDelay?: number;
    /**
     * Scrollable axes. Reka UI hides the axis that has no scrollbar, so
     * horizontal overflow is only possible with `horizontal` or `both`.
     */
    orientation?: VScrollAreaOrientation;
    /**
     * Prevents the scroll from chaining into the parent container.
     */
    overscrollContain?: boolean;
    /**
     * Classes for the scroll viewport, for example a `max-h-*` constraint.
     */
    viewportClass?: VScrollAreaClassValue;
    /**
     * Classes for the content wrapper, for example padding.
     */
    contentClass?: VScrollAreaClassValue;
    /**
     * Reading direction used to place the vertical scrollbar.
     */
    dir?: "ltr" | "rtl";
  }>(),
  {
    type: "scroll",
    scrollHideDelay: 600,
    orientation: "vertical",
    overscrollContain: true,
    viewportClass: undefined,
    contentClass: undefined,
    dir: undefined,
  }
);

const emit = defineEmits<{
  (event: "scroll", value: Event): void;
}>();

interface ScrollAreaViewportExpose {
  viewportElement?: HTMLElement;
}

const viewportRef = useTemplateRef<ScrollAreaViewportExpose>("viewportRef");

function scrollTo(options: ScrollToOptions) {
  viewportRef.value?.viewportElement?.scrollTo(options);
}

defineExpose({
  get viewport() {
    return viewportRef.value?.viewportElement;
  },
  scrollTo,
});
</script>

<template>
  <ScrollAreaRoot
    v-bind="$attrs"
    :type="type"
    :dir="dir"
    :scroll-hide-delay="scrollHideDelay"
    class="v-scroll-area"
    :class="{
      'v-scroll-area--horizontal': orientation !== 'vertical',
    }"
  >
    <ScrollAreaViewport
      ref="viewportRef"
      as-child
      class="v-scroll-area__viewport"
      :class="[viewportClass, { 'overscroll-contain': overscrollContain }]"
      @scroll="emit('scroll', $event)"
    >
      <div class="v-scroll-area__content" :class="contentClass">
        <slot />
      </div>
    </ScrollAreaViewport>

    <ScrollAreaScrollbar
      v-if="orientation !== 'horizontal'"
      orientation="vertical"
      class="v-scroll-area__scrollbar"
    >
      <ScrollAreaThumb class="v-scroll-area__thumb" />
    </ScrollAreaScrollbar>

    <ScrollAreaScrollbar
      v-if="orientation !== 'vertical'"
      orientation="horizontal"
      class="v-scroll-area__scrollbar"
    >
      <ScrollAreaThumb class="v-scroll-area__thumb" />
    </ScrollAreaScrollbar>

    <ScrollAreaCorner
      v-if="orientation === 'both'"
      class="v-scroll-area__corner"
    />
  </ScrollAreaRoot>
</template>

<style lang="scss">
.v-scroll-area {
  --v-scroll-area-size: 10px;

  @apply relative overflow-hidden;

  &__viewport {
    @apply h-full w-full;
  }

  &__content {
    box-sizing: border-box;
    min-width: 0;
  }

  &--horizontal &__content {
    width: max-content;
    min-width: 100%;
  }

  &__scrollbar {
    @apply flex touch-none select-none p-0.5 transition-opacity duration-150;
    z-index: 1;

    &[data-orientation="vertical"] {
      width: var(--v-scroll-area-size);
    }

    &[data-orientation="horizontal"] {
      height: var(--v-scroll-area-size);
      flex-direction: column;
    }

    &[data-state="hidden"] {
      @apply pointer-events-none opacity-0;
    }
  }

  &__thumb {
    @apply relative flex-1 rounded-full bg-gray-400/60 transition-colors;

    &:hover {
      @apply bg-gray-500/70;
    }
  }

  &__corner {
    @apply absolute bottom-0 right-0;
  }
}
</style>
