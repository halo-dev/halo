import MdiDeleteForeverOutline from "@/components/icon/MdiDeleteForeverOutline.vue";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import {
  EditorState,
  findParentNode,
  isActive,
  PluginKey,
  posToDOMRect,
  type Editor,
  type Range,
} from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import { deleteNode } from "@/utils";
import TiptapDetails, {
  DetailsContent,
  DetailsSummary,
  type DetailsOptions,
} from "@tiptap/extension-details";
import { markRaw } from "vue";
import MdiExpandHorizontal from "~icons/mdi/expand-horizontal";

export const DETAILS_BUBBLE_MENU_KEY = new PluginKey("detailsBubbleMenu");

export type ExtensionDetailsOptions = Partial<DetailsOptions> &
  ExtensionOptions;

export const ExtensionDetails = TiptapDetails.extend<ExtensionDetailsOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      HTMLAttributes: {
        class: "details",
      },
      getCommandMenuItems() {
        return {
          priority: 160,
          icon: markRaw(MdiExpandHorizontal),
          title: "editor.extensions.details.command_item",
          keywords: ["details"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .setDetails()
              .updateAttributes("details", { open: true })
              .run();
          },
        };
      },
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 95,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("details"),
            icon: markRaw(MdiExpandHorizontal),
            title: i18n.global.t("editor.extensions.details.command_item"),
            action: () => {
              if (editor.isActive("details")) {
                editor.chain().focus().unsetDetails().run();
              } else {
                editor
                  .chain()
                  .focus()
                  .setDetails()
                  .updateAttributes("details", { open: true })
                  .run();
              }
            },
          },
        };
      },
      getBubbleMenu() {
        return {
          pluginKey: DETAILS_BUBBLE_MENU_KEY,
          shouldShow: ({ state }: { state: EditorState }): boolean => {
            return isActive(state, ExtensionDetails.name);
          },
          options: {
            placement: "top-start",
          },
          getReferencedVirtualElement() {
            const editor = this.editor;
            if (!editor) {
              return null;
            }
            const parentNode = findParentNode(
              (node) => node.type.name === ExtensionDetails.name
            )(editor.state.selection);
            if (parentNode) {
              const domRect = posToDOMRect(
                editor.view,
                parentNode.pos,
                parentNode.pos + parentNode.node.nodeSize
              );
              return {
                getBoundingClientRect: () => domRect,
                getClientRects: () => [domRect],
              };
            }
            return null;
          },
          items: [
            {
              priority: 10,
              props: {
                icon: markRaw(MdiDeleteForeverOutline),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }: { editor: Editor }): boolean =>
                  deleteNode(ExtensionDetails.name, editor),
              },
            },
          ],
        };
      },
    };
  },
  addExtensions() {
    return [DetailsSummary, DetailsContent];
  },
}).configure({
  persist: true,
});
