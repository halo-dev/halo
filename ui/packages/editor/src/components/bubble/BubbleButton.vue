<script lang="ts" setup>
import { computed } from "vue";
import MingcuteDownSmallFill from "~icons/mingcute/down-small-fill";

const props = withDefaults(
  defineProps<{
    title?: string;
    text?: string;
    isActive?: boolean;
    showMoreIndicator?: boolean;
    customTooltip?: boolean;
  }>(),
  {
    showMoreIndicator: false,
    isActive: false,
    title: undefined,
    text: undefined,
    customTooltip: false,
  }
);

const onlyIcon = computed(() => {
  return !props.text && !props.showMoreIndicator;
});
</script>
<template>
  <button
    v-tooltip="{
      content: customTooltip ? undefined : title,
      distance: 8,
      delay: {
        show: 0,
      },
    }"
    :class="[
      { 'bg-gray-200 !text-black': isActive },
      { 'size-8': onlyIcon },
      { 'h-8 gap-1 px-1': !onlyIcon },
    ]"
    :aria-label="title"
    class="text inline-flex items-center justify-center rounded-md text-gray-600 hover:bg-gray-100 active:!bg-gray-200 [&>svg]:size-5"
  >
    <slot name="icon" />
    <span v-if="text">{{ text }}</span>
    <MingcuteDownSmallFill v-if="showMoreIndicator" />
  </button>
</template>
