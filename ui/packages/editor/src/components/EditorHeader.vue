<script lang="ts" setup>
import { VDropdown } from "@halo-dev/components";
import { nextTick, onMounted, onUpdated, useTemplateRef } from "vue";
import MingcuteAddCircleFill from "~icons/mingcute/add-circle-fill";
import { i18n } from "@/locales";
import { type AnyExtension, VueEditor } from "@/tiptap";
import type { ToolbarItemType, ToolboxItemType } from "@/types";

const props = defineProps({
  editor: {
    type: VueEditor,
    required: true,
  },
});

const toolbarRef = useTemplateRef<HTMLElement>("toolbar");
let pendingMenuFocus:
  | {
      menuId: string;
      last: boolean;
    }
  | undefined;

const toolboxMenuId = () => `editor-toolbox-${props.editor.instanceId}`;
const toolbarMenuId = (index: number) =>
  `editor-toolbar-menu-${props.editor.instanceId}-${index}`;

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

function getToolbarButtons() {
  return Array.from(
    toolbarRef.value?.querySelectorAll<HTMLButtonElement>(
      "button[data-editor-toolbar-control]:not([disabled])"
    ) ?? []
  );
}

function setToolbarTabStop(button: HTMLButtonElement) {
  getToolbarButtons().forEach((item) => {
    item.tabIndex = item === button ? 0 : -1;
  });
}

function ensureToolbarTabStop() {
  const buttons = getToolbarButtons();
  const current =
    buttons.find((button) => button === document.activeElement) ??
    buttons.find((button) => button.tabIndex === 0) ??
    buttons[0];
  if (current) {
    setToolbarTabStop(current);
  }
}

function focusToolbarButton(button: HTMLButtonElement) {
  setToolbarTabStop(button);
  button.focus();
}

function getMenuItems(menu: HTMLElement) {
  return Array.from(
    menu.querySelectorAll<HTMLElement>(
      '[role="menuitem"]:not([disabled]):not([aria-disabled="true"])'
    )
  );
}

function focusMenuItem(menuId: string, last = false) {
  void nextTick(() => {
    const menu = document.getElementById(menuId);
    if (!menu) {
      return;
    }
    const items = getMenuItems(menu);
    if (items.includes(document.activeElement as HTMLElement)) {
      return;
    }
    const item = last ? items.at(-1) : items[0];
    item?.focus();
  });
}

function handleMenuShow(menuId: string) {
  if (pendingMenuFocus?.menuId !== menuId) {
    return;
  }
  const { last } = pendingMenuFocus;
  pendingMenuFocus = undefined;

  // Floating Vue focuses its popper after two animation frames. A third frame
  // puts keyboard focus on the requested item without racing that step.
  window.requestAnimationFrame(() => {
    window.requestAnimationFrame(() => {
      window.requestAnimationFrame(() => focusMenuItem(menuId, last));
    });
  });
}

function handleToolbarFocusIn(event: FocusEvent) {
  const button = (event.target as Element | null)?.closest<HTMLButtonElement>(
    "button[data-editor-toolbar-control]"
  );
  if (button) {
    setToolbarTabStop(button);
  }
}

function handleToolbarKeydown(event: KeyboardEvent) {
  const button = (event.target as Element | null)?.closest<HTMLButtonElement>(
    "button[data-editor-toolbar-control]"
  );
  if (!button || !toolbarRef.value?.contains(button)) {
    return;
  }

  const buttons = getToolbarButtons();
  const currentIndex = buttons.indexOf(button);
  if (currentIndex < 0) {
    return;
  }

  if (event.key === "Escape") {
    event.preventDefault();
    props.editor.commands.focus();
    return;
  }

  const menuId = button.getAttribute("aria-controls");
  if (menuId && ["ArrowDown", "ArrowUp", "Enter", " "].includes(event.key)) {
    event.preventDefault();
    const last = event.key === "ArrowUp";
    if (button.getAttribute("aria-expanded") !== "true") {
      pendingMenuFocus = { menuId, last };
      button.click();
    } else {
      focusMenuItem(menuId, last);
    }
    return;
  }

  let target: HTMLButtonElement | undefined;
  if (event.key === "ArrowRight") {
    target = buttons[(currentIndex + 1) % buttons.length];
  } else if (event.key === "ArrowLeft") {
    target = buttons[(currentIndex - 1 + buttons.length) % buttons.length];
  } else if (event.key === "Home") {
    target = buttons[0];
  } else if (event.key === "End") {
    target = buttons.at(-1);
  }

  if (target) {
    event.preventDefault();
    focusToolbarButton(target);
  }
}

function handleMenuKeydown(event: KeyboardEvent) {
  const menu = event.currentTarget as HTMLElement;
  const items = getMenuItems(menu);
  const item = (event.target as Element | null)?.closest<HTMLElement>(
    '[role="menuitem"]'
  );
  const currentIndex = item ? items.indexOf(item) : -1;

  if (event.key === "Escape") {
    event.preventDefault();
    const trigger = document.querySelector<HTMLButtonElement>(
      `[aria-controls="${menu.id}"]`
    );
    trigger?.click();
    trigger?.focus();
    return;
  }

  if (!items.length) {
    return;
  }

  let target: HTMLElement | undefined;
  if (event.key === "ArrowDown") {
    target = items[(currentIndex + 1 + items.length) % items.length];
  } else if (event.key === "ArrowUp") {
    target = items[(currentIndex - 1 + items.length) % items.length];
  } else if (event.key === "Home") {
    target = items[0];
  } else if (event.key === "End") {
    target = items.at(-1);
  }

  if (target) {
    event.preventDefault();
    target.focus();
  }
}

onMounted(() => void nextTick(ensureToolbarTabStop));
onUpdated(ensureToolbarTabStop);
</script>
<template>
  <div
    class="editor-header space-x-1 overflow-auto border-b bg-white px-1 py-1 text-center shadow-sm"
  >
    <div
      ref="toolbar"
      class="inline-flex h-full items-center gap-1"
      role="toolbar"
      :aria-label="i18n.global.t('editor.components.editor_header.toolbar')"
      @focusin="handleToolbarFocusIn"
      @keydown="handleToolbarKeydown"
    >
      <VDropdown
        :triggers="['click']"
        :popper-triggers="['click']"
        @apply-show="handleMenuShow(toolboxMenuId())"
      >
        <template #default="{ shown }">
          <button
            type="button"
            class="inline-flex size-8 items-center justify-center rounded-md p-1 transition-colors hover:bg-gray-100 active:!bg-gray-200"
            :class="{ 'bg-gray-200': shown }"
            tabindex="-1"
            data-editor-toolbar-control
            aria-haspopup="menu"
            :aria-controls="toolboxMenuId()"
            :aria-expanded="shown"
            :aria-label="
              i18n.global.t('editor.components.editor_header.insert_content')
            "
            :title="
              i18n.global.t('editor.components.editor_header.insert_content')
            "
          >
            <MingcuteAddCircleFill class="text-primary" />
          </button>
        </template>
        <template #popper>
          <div
            :id="toolboxMenuId()"
            class="relative max-h-96 w-56 overflow-hidden overflow-y-auto"
            role="menu"
            :aria-label="
              i18n.global.t('editor.components.editor_header.insert_content')
            "
            @keydown="handleMenuKeydown"
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
      <div class="mx-1 h-5 w-[1px] bg-gray-100"></div>
      <div
        v-for="(item, index) in getToolbarItemsFromExtensions()"
        :key="index"
      >
        <component
          :is="item.component"
          v-if="!item.children?.length"
          v-bind="item.props"
          tabindex="-1"
          data-editor-toolbar-control
        />
        <template v-else>
          <VDropdown
            class="inline-flex"
            tabindex="-1"
            :triggers="['click']"
            :popper-triggers="['click']"
            @apply-show="handleMenuShow(toolbarMenuId(index))"
          >
            <template #default="{ shown }">
              <component
                :is="item.component"
                v-bind="item.props"
                :children="item.children"
                tabindex="-1"
                data-editor-toolbar-control
                aria-haspopup="menu"
                :aria-controls="toolbarMenuId(index)"
                :aria-expanded="shown"
                :class="{ 'bg-gray-200': shown }"
              />
            </template>
            <template #popper>
              <div
                :id="toolbarMenuId(index)"
                class="relative max-h-96 w-56 overflow-hidden overflow-y-auto"
                role="menu"
                :aria-label="item.props.title"
                @keydown="handleMenuKeydown"
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
