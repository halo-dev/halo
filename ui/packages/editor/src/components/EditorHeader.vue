<script lang="ts" setup>
import { Editor, type AnyExtension } from "@/tiptap/vue-3";
import type { ToolbarItemType, ToolboxItemType } from "@/types";
import { Dropdown as VDropdown } from "floating-vue";
import MdiPlusCircle from "~icons/mdi/plus-circle";

const props = defineProps({
  editor: {
    type: Editor,
    required: true,
  },
});

function getToolbarItemsFromExtensions() {
  const extensionManager = props.editor?.extensionManager;
  return extensionManager.extensions
    .reduce((acc: ToolbarItemType[], extension: AnyExtension) => {
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
    .reduce((acc: ToolboxItemType[], extension: AnyExtension) => {
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
    class="editor-header space-x-1 overflow-auto border-b bg-white px-1 py-1 text-center shadow-sm"
  >
    <div class="inline-flex h-full items-center">
      <VDropdown :triggers="['click']" :popper-triggers="['click']">
        <template #default="{ shown }">
          <button
            class="rounded-md p-1.5 hover:bg-gray-100 active:bg-gray-200"
            :class="{ 'bg-gray-200': shown }"
            tabindex="-1"
          >
            <MdiPlusCircle class="text-[#4CCBA0]" />
          </button>
        </template>
        <template #popper>
          <div
            class="relative max-h-96 w-56 space-y-1.5 overflow-hidden overflow-y-auto rounded-md bg-white p-1 shadow"
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
      </VDropdown>
      <div class="!mx-1 h-5 w-[1px] bg-gray-100"></div>
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
            <template #default="{ shown }">
              <component
                :is="item.component"
                v-bind="item.props"
                :children="item.children"
                tabindex="-1"
                :class="{ 'bg-gray-200': shown }"
              />
            </template>
            <template #popper>
              <div
                class="relative max-h-96 w-56 space-y-1.5 overflow-hidden overflow-y-auto rounded-md bg-white p-1 shadow"
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
