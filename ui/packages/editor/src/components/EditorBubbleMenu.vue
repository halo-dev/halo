<script lang="ts" setup>
import BubbleItem from "@/components/bubble/BubbleItem.vue";
import BubbleMenu from "@/components/bubble/BubbleMenu.vue";
import type { EditorState, EditorView } from "@/tiptap/pm";
import type { AnyExtension, Editor } from "@/tiptap/vue-3";
import type { NodeBubbleMenuType } from "@/types";
import type { PropType } from "vue";

const props = defineProps({
  editor: {
    type: Object as PropType<Editor>,
    required: true,
  },
});

const getBubbleMenuFromExtensions = () => {
  const extensionManager = props.editor?.extensionManager;
  return extensionManager.extensions
    .map((extension: AnyExtension) => {
      const { getBubbleMenu } = extension.options;

      if (!getBubbleMenu) {
        return null;
      }

      const nodeBubbleMenu = getBubbleMenu({
        editor: props.editor,
      }) as NodeBubbleMenuType;

      if (nodeBubbleMenu.items) {
        nodeBubbleMenu.items = nodeBubbleMenu.items.sort(
          (a, b) => a.priority - b.priority
        );
      }

      return nodeBubbleMenu;
    })
    .filter(Boolean) as NodeBubbleMenuType[];
};

const shouldShow = (
  props: {
    editor: Editor;
    state: EditorState;
    node?: HTMLElement;
    view?: EditorView;
    oldState?: EditorState;
    from?: number;
    to?: number;
  },
  bubbleMenu: NodeBubbleMenuType
) => {
  if (!props.editor.isEditable) {
    return false;
  }
  return bubbleMenu.shouldShow?.(props);
};
</script>
<template>
  <bubble-menu
    v-for="(bubbleMenu, index) in getBubbleMenuFromExtensions()"
    :key="index"
    :plugin-key="bubbleMenu?.pluginKey"
    :should-show="(prop) => shouldShow(prop, bubbleMenu)"
    :editor="editor"
    :tippy-options="{
      maxWidth: '100%',
      ...bubbleMenu.tippyOptions,
    }"
    :get-render-container="bubbleMenu.getRenderContainer"
    :default-animation="bubbleMenu.defaultAnimation"
  >
    <div
      class="bubble-menu flex items-center space-x-1 rounded-md border bg-white p-1 shadow"
    >
      <template v-if="bubbleMenu.items">
        <template
          v-for="(item, itemIndex) in bubbleMenu.items"
          :key="itemIndex"
        >
          <template v-if="item.component">
            <component
              :is="item.component"
              v-bind="item.props"
              :editor="editor"
            />
          </template>
          <bubble-item v-else :editor="editor" v-bind="item.props" />
        </template>
      </template>
      <template v-else-if="bubbleMenu.component">
        <component :is="bubbleMenu?.component" :editor="editor" />
      </template>
    </div>
  </bubble-menu>
</template>
<style scoped>
.bubble-menu {
  max-width: calc(100vw - 30px);
  overflow-x: auto;
}
</style>
