<script lang="ts" setup>
import { i18n } from "@/locales";
import { NodeSelection, PMNode, TextSelection, VueEditor } from "@/tiptap";
import type {
  DragButtonItemProps,
  DragButtonType,
  ExtensionOptions,
} from "@/types";
import { isEmpty } from "@/utils";
import { offset } from "@floating-ui/dom";
import { DragHandle } from "@tiptap/extension-drag-handle-vue-3";
import { Dropdown as VDropdown, vTooltip } from "floating-vue";
import { sortBy } from "lodash-es";
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
  return isEmpty(currentNode.value);
});

const handleInsertBlock = () => {
  if (isEmptyNode.value) {
    editor.chain().insertContent("/").focus().run();
  } else {
    editor
      .chain()
      .insertContentAt(
        currentPos.value + (currentNode.value?.nodeSize ?? 0),
        "/"
      )
      .focus()
      .run();
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
  const subDragButtonItems: DragButtonType[] = [];
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
      if (item.parentKey && item.parentKey.trim() !== "") {
        subDragButtonItems.push(item);
        continue;
      }
      rootDragButtonItems.push(item);
    }
  }

  const mergedDragButtonItems = mergeDragButtonItems(
    rootDragButtonItems,
    subDragButtonItems
  );
  return sortDragButtonItems(mergedDragButtonItems);
});

/**
 * Merge rootDragButtonItems and subDragButtonItems, returning an array containing all drag button items.
 *
 * Sub drag button items may be used as submenus of root drag button items.
 * When the parentKey in a sub drag button matches the key in root drag button items,
 * the sub drag button will be used as a submenu of the root drag button item.
 * However, root drag button items may already have submenus. In this case, merge based on the key
 * of the corresponding submenu in the sub drag button:
 * 1. If it has a key and there is a corresponding submenu in root drag button items,
 *    replace the corresponding submenu item in root drag button items
 * 2. If it has a key but there is no corresponding submenu in root drag button items,
 *    append it to the submenu of root drag button items
 * 3. If it has a key but root drag button items currently have no submenu,
 *    append it to root drag button items
 * 4. If it has no key, directly append it to the corresponding submenu item in root drag button items
 *
 * @param rootDragButtonItems - The root drag button items.
 * @param subDragButtonItems - The sub drag button items.
 * @returns The merged drag button items.
 */
const mergeDragButtonItems = (
  rootDragButtonItems: DragButtonType[],
  subDragButtonItems: DragButtonType[]
): DragButtonType[] => {
  const mergedDragButtonItems: DragButtonType[] = [];
  const subDragButtonItemsMap: Map<string, DragButtonItemProps[]> = new Map();
  for (const subDragButtonItem of subDragButtonItems) {
    if (subDragButtonItem.parentKey) {
      if (!subDragButtonItemsMap.has(subDragButtonItem.parentKey)) {
        subDragButtonItemsMap.set(subDragButtonItem.parentKey, []);
      }
      const items = subDragButtonItem.children?.items ?? [];
      const mapItems: DragButtonItemProps[] =
        subDragButtonItemsMap.get(subDragButtonItem.parentKey) ?? [];
      subDragButtonItemsMap.set(subDragButtonItem.parentKey, [
        ...mapItems,
        ...items,
      ]);
    }
  }

  for (const rootDragButtonItem of rootDragButtonItems) {
    const rootKey = rootDragButtonItem.key;
    if (!rootKey) {
      mergedDragButtonItems.push(rootDragButtonItem);
      continue;
    }

    if (subDragButtonItemsMap.has(rootKey)) {
      const subDragButtonItems = subDragButtonItemsMap.get(rootKey);
      if (subDragButtonItems && subDragButtonItems.length > 0) {
        const items = [
          ...(rootDragButtonItem.children?.items ?? []),
          ...subDragButtonItems,
        ].filter((item, index, self) => {
          if (item.key) {
            return self.findIndex((t) => t.key === item.key) === index;
          }
          return true;
        });
        mergedDragButtonItems.push({
          ...rootDragButtonItem,
          children: {
            ...rootDragButtonItem.children,
            items: sortDragButtonItems(items),
          },
        });
        continue;
      }
    }

    mergedDragButtonItems.push(rootDragButtonItem);
  }

  return mergedDragButtonItems;
};

const sortDragButtonItems = (items: DragButtonType[]): DragButtonType[] => {
  return sortBy(items, "priority");
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
