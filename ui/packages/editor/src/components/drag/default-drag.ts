import { i18n } from "@/locales";
import type { DragButtonType } from "@/types";
import { copySelectionToClipboard, deleteNode } from "@/utils";

import { isActive } from "@tiptap/core";
import { markRaw } from "vue";
import MdiContentCut from "~icons/mdi/content-cut";
import MdiDeleteForeverOutline from "~icons/mdi/delete-forever-outline";
import RiFileCopyLine from "~icons/ri/file-copy-line";
import RiLoopLeftFill from "~icons/ri/loop-left-fill";
import BlockActionHorizontalSeparator from "../block/BlockActionHorizontalSeparator.vue";

export const CONVERT_TO_KEY = "convert-to";
export const DELETE_KEY = "delete";
export const COPY_KEY = "copy";
export const CUT_KEY = "cut";
export const INSERT_BELOW_KEY = "insert-below";

const defaultDragItems: DragButtonType[] = [
  {
    priority: 10,
    title: () => i18n.global.t("editor.drag.menu.convert_to"),
    key: CONVERT_TO_KEY,
    icon: markRaw(RiLoopLeftFill),
    children: {
      items: [
        // Internal sub-nodes are implemented by various extensions, such as the Header extension
      ],
    },
  },
  {
    priority: 700,
    component: markRaw(BlockActionHorizontalSeparator),
    visible: ({ editor }) => {
      if (isActive(editor.state, "table")) {
        return false;
      }
      return true;
    },
  },
  {
    priority: 710,
    title: () => i18n.global.t("editor.drag.menu.copy"),
    key: COPY_KEY,
    icon: markRaw(RiFileCopyLine),
    keyboard: "Mod-C",
    action: async ({ editor, pos, node }) => {
      if (node) {
        const nodeSize = node.nodeSize;
        const from = pos;
        const to = pos + nodeSize;
        editor.commands.setTextSelection({ from, to });

        await copySelectionToClipboard(editor);
      }
    },
  },
  {
    priority: 720,
    title: () => i18n.global.t("editor.drag.menu.cut"),
    key: CUT_KEY,
    icon: markRaw(MdiContentCut),
    keyboard: "Mod-X",
    action: async ({ editor, pos, node }) => {
      if (node) {
        const nodeSize = node.nodeSize;
        const from = pos;
        const to = pos + nodeSize;
        editor.commands.setTextSelection({ from, to });
        const success = await copySelectionToClipboard(editor);
        if (success) {
          editor.chain().focus().deleteRange({ from, to }).run();
        }
      }
    },
  },
  {
    priority: 800,
    component: markRaw(BlockActionHorizontalSeparator),
  },
  {
    priority: 900,
    title: () => i18n.global.t("editor.drag.menu.delete"),
    key: DELETE_KEY,
    icon: markRaw(MdiDeleteForeverOutline),
    keyboard: "Delete",
    action: ({ editor, node }) => {
      if (node) {
        deleteNode(node?.type.name, editor);
      }
    },
  },
];

export default defaultDragItems;
