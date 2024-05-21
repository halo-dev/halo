<script lang="ts" setup>
import { Dropdown as VDropdown, Menu as VMenu } from "floating-vue";
import { type AnyExtension, Editor } from "@/tiptap/vue-3";
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
    class="editor-header py-1 space-x-1 px-1 overflow-auto border-b drop-shadow-sm bg-white text-center"
  >
    <div class="h-full inline-flex items-center">
      <VMenu>
        <button class="p-1.5 rounded-md hover:bg-gray-100" tabindex="-1">
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
              tabindex="-1"
            />
          </div>
        </template>
      </VMenu>
      <div class="h-5 bg-gray-100 w-[1px] !mx-1"></div>
      <div
        v-for="(item, index) in getToolbarItemsFromExtensions()"
        :key="index"
      >
        <component
          :is="item.component"
          v-if="!item.children?.length"
          v-bind="item.props"
          tabindex="-1"
        />
        <template v-else>
          <VDropdown
            class="inline-flex"
            tabindex="-1"
            :triggers="['click']"
            :popper-triggers="['click']"
          >
            <component
              :is="item.component"
              v-bind="item.props"
              :children="item.children"
              tabindex="-1"
            />
            <template #popper>
              <div
                class="relative rounded-md bg-white overflow-hidden drop-shadow w-56 p-1 max-h-96 overflow-y-auto space-y-1.5"
              >
                <component
                  v-bind="child.props"
                  :is="child.component"
                  v-for="(child, childIndex) in item.children"
                  :key="childIndex"
                  tabindex="-1"
                />
              </div>
            </template>
          </VDropdown>
        </template>
      </div>
    </div>
  </div>
</template>
