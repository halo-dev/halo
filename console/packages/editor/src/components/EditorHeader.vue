<script lang="ts" setup>
import { Menu as VMenu } from "floating-vue";
import { Editor, type AnyExtension } from "@/tiptap/vue-3";
import MdiPlusCircle from "~icons/mdi/plus-circle";
import type { ToolbarItem, ToolboxItem } from "@/types";

const props = defineProps({
  editor: {
    type: Editor,
    required: true,
  },
});

function getToolbarItemsFromExtensions() {
  const extensionManager = props.editor?.extensionManager;
  return extensionManager.extensions
    .reduce((acc: ToolbarItem[], extension: AnyExtension) => {
      const { getToolbarItems } = extension.options;

      if (!getToolbarItems) {
        return acc;
      }

      const items = getToolbarItems({
        editor: props.editor,
      });

      if (Array.isArray(items)) {
        return [...acc, ...items];
      }

      return [...acc, items];
    }, [])
    .sort((a, b) => a.priority - b.priority);
}

function getToolboxItemsFromExtensions() {
  const extensionManager = props.editor?.extensionManager;
  return extensionManager.extensions
    .reduce((acc: ToolboxItem[], extension: AnyExtension) => {
      const { getToolboxItems } = extension.options;

      if (!getToolboxItems) {
        return acc;
      }

      const items = getToolboxItems({
        editor: props.editor,
      });

      if (Array.isArray(items)) {
        return [...acc, ...items];
      }

      return [...acc, items];
    }, [])
    .sort((a, b) => a.priority - b.priority);
}
</script>
<template>
  <div
    class="editor-header flex items-center py-1 space-x-0.5 justify-start px-1 overflow-auto sm:!justify-center border-b drop-shadow-sm bg-white"
  >
    <div class="inline-flex items-center justify-center">
      <VMenu>
        <button class="p-1 rounded-sm hover:bg-gray-100">
          <MdiPlusCircle class="text-[#4CCBA0]" />
        </button>
        <template #popper>
          <div
            class="relative rounded-md bg-white overflow-hidden drop-shadow w-56 p-1 max-h-96 overflow-y-auto space-y-1.5"
          >
            <component
              :is="toolboxItem.component"
              v-for="(toolboxItem, index) in getToolboxItemsFromExtensions()"
              v-bind="toolboxItem.props"
              :key="index"
            />
          </div>
        </template>
      </VMenu>
    </div>
    <div class="h-5 bg-gray-100 w-[1px] !mx-1"></div>
    <div
      v-for="(item, index) in getToolbarItemsFromExtensions()"
      :key="index"
      class="inline-flex items-center justify-center"
    >
      <component
        :is="item.component"
        v-if="!item.children?.length"
        v-bind="item.props"
      />
      <VMenu v-else class="inline-flex">
        <component :is="item.component" v-bind="item.props" />
        <template #popper>
          <div
            class="relative rounded-md bg-white overflow-hidden drop-shadow w-48 p-1 max-h-72 overflow-y-auto"
          >
            <component
              v-bind="child.props"
              :is="child.component"
              v-for="(child, childIndex) in item.children"
              :key="childIndex"
            />
          </div>
        </template>
      </VMenu>
    </div>
  </div>
</template>
