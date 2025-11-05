<script lang="ts" setup>
import { i18n } from "@/locales";
import {
  Editor,
  NodeSelection,
  PMNode,
  TextSelection,
  VueEditor,
} from "@/tiptap";
import type {
  DragButtonItemProps,
  DragButtonType,
  ExtensionOptions,
} from "@/types";
import { isBlockEmpty } from "@/utils";
import { offset } from "@floating-ui/dom";
import { DragHandle } from "@tiptap/extension-drag-handle-vue-3";
import { sortBy } from "es-toolkit";
import { Dropdown as VDropdown, vTooltip } from "floating-vue";
import { computed, ref, shallowRef, type PropType } from "vue";
import MaterialSymbolsAddRounded from "~icons/material-symbols/add-rounded";
import MaterialSymbolsDragIndicator from "~icons/material-symbols/drag-indicator";
import EditorDragMenu from "./EditorDragMenu.vue";
import defaultDragItems from "./default-drag";

const { editor } = defineProps({
  editor: {
    type: Object as PropType<VueEditor>,
    required: true,
  },
});

const currentNode = shallowRef<PMNode | null>(null);
const currentPos = shallowRef<number>(0);
const showMenu = ref(false);

const isEmptyNode = computed(() => {
  if (!currentNode.value) {
    return false;
  }
  return isBlockEmpty(currentNode.value);
});

const handleInsertBlock = () => {
  if (isEmptyNode.value) {
    editor.chain().insertContent("/").focus().run();
  } else {
    const insertPos = currentPos.value + (currentNode.value?.nodeSize ?? 0);
    editor.commands.insertContentAt(
      insertPos,
      [{ type: "paragraph", content: [{ type: "text", text: "/" }] }],
      {
        updateSelection: true,
      }
    );
    editor.commands.focus(insertPos + 2, {
      scrollIntoView: true,
    });
  }
};

const handleNodeChange = ({
  node,
  pos,
}: {
  node: PMNode | null;
  pos: number;
}) => {
  currentNode.value = node;
  currentPos.value = pos;
};

const handleMenuShow = () => {
  const { tr } = editor.state;
  tr.setMeta("lockDragHandle", true);
  if (currentPos.value !== undefined) {
    const $pos = tr.doc.resolve(currentPos.value);
    tr.setSelection(new NodeSelection($pos));
  }
  editor.view.dispatch(tr);
};

const handleMenuHide = () => {
  const { tr } = editor.state;
  tr.setMeta("lockDragHandle", false);
  const { selection } = editor.state;
  if (!selection.empty) {
    const $pos = tr.doc.resolve(selection.to);
    tr.setSelection(TextSelection.create(tr.doc, $pos.pos));
  }
  editor.view.dispatch(tr);
};

const dragMenuItems = computed(() => {
  const extensionManager = editor?.extensionManager;
  const rootDragButtonItems: DragButtonType[] = [...defaultDragItems];
  const extendsDragButtonItems: DragButtonType[] = [];
  for (const extension of extensionManager.extensions) {
    const { getDraggableMenuItems } = extension.options as ExtensionOptions;
    if (!getDraggableMenuItems) {
      continue;
    }

    const dragButtonItems = getDraggableMenuItems({ editor }) as
      | DragButtonType
      | DragButtonType[];

    for (const item of Array.isArray(dragButtonItems)
      ? dragButtonItems
      : [dragButtonItems]) {
      if (item.extendsKey && item.extendsKey.trim() !== "") {
        extendsDragButtonItems.push(item);
        continue;
      }
      rootDragButtonItems.push(item);
    }
  }

  const mergedDragButtonItems = mergeDragButtonItems(
    rootDragButtonItems,
    extendsDragButtonItems
  );

  return sortDragButtonItems(mergedDragButtonItems);
});

/**
 * Merges root and extension drag button items into a unified array.
 *
 * Extension items can extend root items by matching their `extendsKey` property to a root item's `key`.
 * When merging, the following rules apply:
 * 1. Extension items with matching `extendsKey` are merged into their parent root item's children
 * 2. If an extension child has a `key` matching an existing child, it replaces that child
 * 3. If an extension child has a unique `key`, it's appended to the parent's children
 * 4. Extension children without a `key` are always appended
 *
 * @param rootDragButtonItems - Base drag button items from default configuration
 * @param extendsDragButtonItems - Extension drag button items from plugins
 * @returns Merged and deduplicated drag button items
 */
const mergeDragButtonItems = (
  rootDragButtonItems: DragButtonType[],
  extendsDragButtonItems: DragButtonType[]
): DragButtonType[] => {
  const mergedDragButtonItems: DragButtonType[] = [];
  const extendsDragButtonItemsMap: Map<string, DragButtonItemProps[]> =
    new Map();
  for (const extendsDragButton of extendsDragButtonItems) {
    if (extendsDragButton.extendsKey) {
      if (!extendsDragButtonItemsMap.has(extendsDragButton.extendsKey)) {
        extendsDragButtonItemsMap.set(extendsDragButton.extendsKey, []);
      }
      const originalItems: DragButtonItemProps[] =
        extendsDragButtonItemsMap.get(extendsDragButton.extendsKey) ?? [];
      extendsDragButtonItemsMap.set(extendsDragButton.extendsKey, [
        ...originalItems,
        extendsDragButton,
      ]);
    }
  }

  for (const rootDragButtonItem of rootDragButtonItems) {
    const rootKey = rootDragButtonItem.key;
    if (rootKey) {
      const extendsDragButtonItems =
        extendsDragButtonItemsMap.get(rootKey) ?? [];
      for (const extendsDragButton of extendsDragButtonItems) {
        mergeRootDragButtonItemsProps(rootDragButtonItem, extendsDragButton);
      }
    }

    mergedDragButtonItems.push(rootDragButtonItem);
  }

  return mergedDragButtonItems;
};

/**
 * Merges properties from an extension drag button into a root drag button item.
 *
 * Property merge behavior:
 * - `visible`: AND logic - item visible only if all functions return true (default: true)
 * - `isActive`: OR logic - item active if any function returns true (default: false)
 * - `disabled`: OR logic - item disabled if any function returns true (default: false)
 * - `action`: Extension action executes first, falls back to root action if undefined
 * - `children`: Recursively merged with deduplication by `key`
 *
 * @param rootDragButtonItem - Root drag button item to be extended
 * @param extendsDragButton - Extension drag button providing additional behavior
 */
const mergeRootDragButtonItemsProps = (
  rootDragButtonItem: DragButtonType,
  extendsDragButton: DragButtonType
) => {
  mergeRootDragButtonVisibleProps(rootDragButtonItem, extendsDragButton);
  mergeRootDragButtonIsActiveProps(rootDragButtonItem, extendsDragButton);
  mergeRootDragButtonDisabledProps(rootDragButtonItem, extendsDragButton);
  mergeRootDragButtonActionProps(rootDragButtonItem, extendsDragButton);
  mergeRootDragButtonItemsChildrenProps(rootDragButtonItem, extendsDragButton);
  return rootDragButtonItem;
};

/**
 * Recursively merges children items from extension into root drag button.
 * Performs deduplication by `key` and recursively merges nested extensions.
 *
 * @param rootDragButtonItem - Root drag button item to modify
 * @param extendsDragButtonItem - Extension providing additional children
 */
const mergeRootDragButtonItemsChildrenProps = (
  rootDragButtonItem: DragButtonType,
  extendsDragButtonItem: DragButtonType
) => {
  const extendsChildrenItems = extendsDragButtonItem.children?.items ?? [];
  if (extendsChildrenItems.length === 0) {
    return;
  }

  const items = [
    ...(rootDragButtonItem.children?.items ?? []),
    ...extendsChildrenItems,
  ].filter((item, index, self) => {
    if (item.key) {
      return self.findIndex((t) => t.key === item.key) === index;
    }
    return true;
  });

  const originalItems = items.filter((item) => {
    return item.extendsKey === undefined;
  });
  const extendsItems = items.filter((item) => {
    return item.extendsKey && item.extendsKey.trim() !== "";
  });

  const mergedItems = mergeDragButtonItems(originalItems, extendsItems);

  rootDragButtonItem.children = {
    ...rootDragButtonItem.children,
    items: sortDragButtonItems(mergedItems),
  };
};

/**
 * Merges `disabled` property handlers with OR logic.
 * The merged item is disabled if either extension or root handler returns true.
 *
 * @param rootDragButtonItem - Root drag button item to modify
 * @param extendsDragButton - Extension providing additional disabled conditions
 */
const mergeRootDragButtonDisabledProps = (
  rootDragButtonItem: DragButtonType,
  extendsDragButton: DragButtonType
) => {
  const { disabled: extendsDisabled } = extendsDragButton;
  const { disabled: rootDisabled } = rootDragButtonItem;
  rootDragButtonItem.disabled = ({
    editor,
    node,
    pos,
  }: {
    editor: Editor;
    node: PMNode | null;
    pos: number;
  }) => {
    if (extendsDisabled) {
      const extendsDisabledResult = extendsDisabled({ editor, node, pos });
      if (extendsDisabledResult === true) {
        return true;
      }
    }
    if (rootDisabled) {
      return rootDisabled({ editor, node, pos });
    }
    return false;
  };
};

/**
 * Merges `isActive` property handlers with OR logic.
 * The merged item is active if either extension or root handler returns true.
 *
 * @param rootDragButtonItem - Root drag button item to modify
 * @param extendsDragButton - Extension providing additional active state conditions
 */
const mergeRootDragButtonIsActiveProps = (
  rootDragButtonItem: DragButtonType,
  extendsDragButton: DragButtonType
) => {
  const { isActive: extendsIsActive } = extendsDragButton;
  const { isActive: rootIsActive } = rootDragButtonItem;
  rootDragButtonItem.isActive = ({
    editor,
    node,
    pos,
  }: {
    editor: Editor;
    node: PMNode | null;
    pos: number;
  }) => {
    if (extendsIsActive) {
      const extendsIsActiveResult = extendsIsActive({ editor, node, pos });
      if (extendsIsActiveResult === true) {
        return true;
      }
    }
    if (rootIsActive) {
      return rootIsActive({ editor, node, pos });
    }
    return false;
  };
};

/**
 * Merges `action` property handlers with fallback behavior.
 * Extension action executes first; if it returns undefined, falls back to root action.
 *
 * @param rootDragButtonItem - Root drag button item to modify
 * @param extendsDragButton - Extension providing action override
 */
const mergeRootDragButtonActionProps = (
  rootDragButtonItem: DragButtonType,
  extendsDragButton: DragButtonType
) => {
  const { action: extendsAction } = extendsDragButton;
  const { action: rootAction } = rootDragButtonItem;
  if (!extendsAction && !rootAction) {
    return;
  }
  rootDragButtonItem.action = async ({
    editor,
    node,
    pos,
    close,
  }: {
    editor: Editor;
    node: PMNode | null;
    pos: number;
    close: () => void;
  }) => {
    if (extendsAction) {
      const extendsActionResult = await extendsAction({
        editor,
        node,
        pos,
        close,
      });

      if (extendsActionResult !== undefined) {
        return extendsActionResult;
      }
    }
    if (rootAction) {
      return rootAction({ editor, node, pos, close });
    }
    return undefined;
  };
};

/**
 * Merges `visible` property handlers with AND logic.
 * The merged item is visible only when both extension and root handlers return true.
 *
 * @param rootDragButtonItem - Root drag button item to modify
 * @param extendsDragButton - Extension providing additional visibility conditions
 */
const mergeRootDragButtonVisibleProps = (
  rootDragButtonItem: DragButtonType,
  extendsDragButton: DragButtonType
) => {
  const { visible: extendsVisible } = extendsDragButton;
  const { visible: rootVisible } = rootDragButtonItem;
  rootDragButtonItem.visible = ({
    editor,
    node,
    pos,
  }: {
    editor: Editor;
    node: PMNode | null;
    pos: number;
  }) => {
    if (extendsVisible) {
      const extendsVisibleResult = extendsVisible({ editor, node, pos });
      if (extendsVisibleResult === false) {
        return false;
      }
    }
    if (rootVisible) {
      return rootVisible({ editor, node, pos });
    }
    return true;
  };
};

/**
 * Sorts drag button items by priority in ascending order.
 *
 * @param items - Drag button items to sort
 * @returns Sorted items by priority (lower priority values appear first)
 */
const sortDragButtonItems = (items: DragButtonType[]): DragButtonType[] => {
  return sortBy(items, ["priority"]);
};
</script>

<template>
  <!-- @vue-ignore-->
  <DragHandle
    :editor="editor"
    :compute-position-config="{
      middleware: [offset(5)],
    }"
    @node-change="handleNodeChange"
  >
    <div class="flex items-center justify-center gap-0.5">
      <button
        v-tooltip="{
          content: i18n.global.t('editor.drag.button.insert_block'),
          distance: 8,
          delay: {
            show: 500,
          },
        }"
        class="flex p-0.5 hover:bg-gray-100"
        @click="handleInsertBlock"
      >
        <MaterialSymbolsAddRounded class="h-5 w-5" />
      </button>

      <VDropdown
        class="flex p-0.5 hover:bg-gray-100"
        :triggers="['click']"
        :shown="showMenu"
        :distance="8"
        placement="right"
        @show="handleMenuShow"
        @hide="handleMenuHide"
      >
        <button
          v-tooltip="{
            content: i18n.global.t('editor.drag.button.drag_handle'),
            distance: 8,
            delay: {
              show: 500,
            },
          }"
          @click="showMenu = true"
        >
          <MaterialSymbolsDragIndicator class="h-5 w-5" />
        </button>

        <template #popper>
          <EditorDragMenu
            :editor="editor"
            :node="currentNode"
            :pos="currentPos"
            :items="dragMenuItems"
            @close="showMenu = false"
          />
        </template>
      </VDropdown>
    </div>
  </DragHandle>
</template>
