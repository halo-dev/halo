<script setup lang="ts" name="BubbleMenu">
import { ref, type PropType, onMounted, onBeforeUnmount } from "vue";
import {
  BubbleMenuPlugin,
  type BubbleMenuPluginProps,
} from "./BubbleMenuPlugin";

const props = defineProps({
  pluginKey: {
    type: [String, Object] as PropType<BubbleMenuPluginProps["pluginKey"]>,
    default: "bubbleMenu",
  },

  editor: {
    type: Object as PropType<BubbleMenuPluginProps["editor"]>,
    required: true,
  },

  tippyOptions: {
    type: Object as PropType<BubbleMenuPluginProps["tippyOptions"]>,
    default: () => ({}),
  },

  shouldShow: {
    type: Function as PropType<
      Exclude<Required<BubbleMenuPluginProps>["shouldShow"], null>
    >,
    default: null,
  },

  getRenderContainer: {
    type: Function as PropType<
      Exclude<Required<BubbleMenuPluginProps>["getRenderContainer"], null>
    >,
    default: null,
  },

  defaultAnimation: {
    type: Boolean as PropType<BubbleMenuPluginProps["defaultAnimation"]>,
    default: true,
  },
});

const root = ref<HTMLElement | null>(null);
onMounted(() => {
  const {
    editor,
    pluginKey,
    shouldShow,
    tippyOptions,
    getRenderContainer,
    defaultAnimation,
  } = props;

  editor.registerPlugin(
    BubbleMenuPlugin({
      editor,
      element: root.value as HTMLElement,
      pluginKey,
      shouldShow,
      tippyOptions,
      getRenderContainer,
      defaultAnimation,
    })
  );
});

onBeforeUnmount(() => {
  const { pluginKey, editor } = props;

  editor.unregisterPlugin(pluginKey);
});
</script>
<template>
  <div ref="root">
    <slot></slot>
  </div>
</template>
