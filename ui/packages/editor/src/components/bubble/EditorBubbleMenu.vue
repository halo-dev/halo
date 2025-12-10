<script lang="ts" setup>
import {
  Editor,
  EditorState,
  EditorView,
  PluginKey,
  VueEditor,
} from "@/tiptap";
import type { BubbleItemType, NodeBubbleMenuType } from "@/types";
import { BubbleMenu } from "@tiptap/vue-3/menus";
import { type PropType } from "vue";
import BubbleItem from "./BubbleItem.vue";

const props = defineProps({
  editor: {
    type: Object as PropType<VueEditor>,
    required: true,
  },
});

const getBubbleMenuFromExtensions = (): NodeBubbleMenuType[] => {
  const extensionManager = props.editor?.extensionManager;
  const extendsBubbleMap: Map<string | PluginKey, NodeBubbleMenuType[]> =
    new Map();
  const bubbleMenus: NodeBubbleMenuType[] = [];
  for (const extension of extensionManager.extensions) {
    const { getBubbleMenu } = extension.options;

    if (!getBubbleMenu) {
      continue;
    }

    const nodeBubbleMenu = getBubbleMenu({
      editor: props.editor,
    }) as NodeBubbleMenuType;

    if (nodeBubbleMenu.extendsKey) {
      if (!extendsBubbleMap.has(nodeBubbleMenu.extendsKey)) {
        extendsBubbleMap.set(nodeBubbleMenu.extendsKey, []);
      }
      extendsBubbleMap.get(nodeBubbleMenu.extendsKey)?.push(nodeBubbleMenu);
      continue;
    }

    bubbleMenus.push(nodeBubbleMenu);
  }

  return bubbleMenus.map<NodeBubbleMenuType>((bubbleMenu) => {
    if (!bubbleMenu.pluginKey) {
      bubbleMenu.items = sortBubbleMenuItems(bubbleMenu.items);
      return bubbleMenu;
    }

    if (!extendsBubbleMap.has(bubbleMenu.pluginKey)) {
      bubbleMenu.items = sortBubbleMenuItems(bubbleMenu.items);
      return bubbleMenu;
    }

    const extendsBubbleMenus = extendsBubbleMap.get(bubbleMenu.pluginKey) ?? [];
    return mergeBubbleMenu(bubbleMenu, extendsBubbleMenus);
  });
};

/**
 * Merge bubble menu
 *
 * If the item has a key, it will be overwritten if it exists in the extendsBubbleMenus.
 * If the item does not have a key, it will be appended to the end of the items.
 * If the extendsBubbleMenus has a key, but it is not found in the items, it will be appended to the end of the items.
 *
 * For shouldShow: all shouldShow functions from the original and extended bubble menus will be merged.
 * The merged bubble menu will only be shown if all shouldShow functions return true.
 * If a shouldShow is not defined, it defaults to true.
 *
 * @param bubbleMenu - The bubble menu to merge.
 * @param extendsBubbleMenus - The extends bubble menus to merge.
 * @returns The merged bubble menu.
 */
const mergeBubbleMenu = (
  bubbleMenu: NodeBubbleMenuType,
  extendsBubbleMenus: NodeBubbleMenuType[]
): NodeBubbleMenuType => {
  const items = bubbleMenu.items ?? [];
  const extendsItems =
    extendsBubbleMenus
      .map((extendsBubbleMenu) => extendsBubbleMenu.items)
      .filter((item) => item !== undefined)
      .flat() ?? [];

  const keyedItems = new Map<string, BubbleItemType>();
  const nonKeyedItems: BubbleItemType[] = [];

  items.forEach((item) => {
    if (item.key) {
      keyedItems.set(item.key, item);
    } else {
      nonKeyedItems.push(item);
    }
  });

  extendsItems.forEach((item) => {
    if (item.key) {
      keyedItems.set(item.key, item);
    } else {
      nonKeyedItems.push(item);
    }
  });

  const mergedItems = [...Array.from(keyedItems.values()), ...nonKeyedItems];

  const shouldShowFunctions = [
    bubbleMenu.shouldShow,
    ...extendsBubbleMenus.map((menu) => menu.shouldShow),
  ].filter((fn) => fn !== undefined);

  const mergedShouldShow =
    shouldShowFunctions.length > 0
      ? (
          props: Parameters<NonNullable<NodeBubbleMenuType["shouldShow"]>>[0]
        ) => {
          return shouldShowFunctions.every((fn) => (fn ? fn(props) : true));
        }
      : undefined;

  return {
    ...bubbleMenu,
    items: sortBubbleMenuItems(mergedItems),
    shouldShow: mergedShouldShow,
  };
};

const sortBubbleMenuItems = (items: BubbleItemType[] | undefined) => {
  return items?.sort((a, b) => a.priority - b.priority);
};

const shouldShow = (
  props: {
    editor: Editor;
    element: HTMLElement;
    view: EditorView;
    state: EditorState;
    oldState?: EditorState;
    from: number;
    to: number;
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
    :should-show="(prop) => shouldShow(prop, bubbleMenu) ?? false"
    :editor="editor"
    :options="{
      ...(bubbleMenu.options as any),
    }"
    :update-delay="0"
    :get-referenced-virtual-element="bubbleMenu.getReferencedVirtualElement"
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
