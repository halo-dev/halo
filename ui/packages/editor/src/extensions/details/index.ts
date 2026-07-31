import TiptapDetails, {
  DetailsContent,
  DetailsSummary,
  type DetailsOptions,
} from "@tiptap/extension-details";
import { markRaw } from "vue";
import MingcuteFoldVerticalLine from "~icons/mingcute/fold-vertical-line";
import MingcuteDelete2Line from "@/components/icon/MingcuteDelete2Line.vue";
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

export const DETAILS_BUBBLE_MENU_KEY = new PluginKey("detailsBubbleMenu");

export type ExtensionDetailsOptions = Partial<DetailsOptions> &
  ExtensionOptions;

const ExtensionDetailsSummary = DetailsSummary.extend({
  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "The visible summary label for a details disclosure. It is not a top-level content block.",
        exposure: "recommended",
        useWhen: ["Labeling the content hidden or shown by a details element."],
        avoidWhen: ["There is no containing details element."],
        contentGuidelines: ["Keep the summary short and descriptive."],
        generation: {
          mode: "direct-html",
        },
        examples: [
          '<details><summary>More information</summary><div data-type="detailsContent"><p>Additional details.</p></div></details>',
        ],
      },
      structure: {
        allowedParents: ["details"],
        minPerParent: 1,
        maxPerParent: 1,
        description:
          "detailsSummary may appear only inside details, exactly once per details element.",
      },
    };
  },
});

const ExtensionDetailsContent = DetailsContent.extend({
  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "The expandable block content of a details disclosure. It is not a top-level content block.",
        exposure: "recommended",
        useWhen: ["Providing the body controlled by a details summary."],
        avoidWhen: ["There is no containing details element."],
        generation: {
          mode: "direct-html",
        },
        examples: [
          '<details><summary>More information</summary><div data-type="detailsContent"><p>Additional details.</p></div></details>',
        ],
      },
      structure: {
        allowedParents: ["details"],
        minPerParent: 1,
        maxPerParent: 1,
        description:
          "detailsContent may appear only inside details, exactly once per details element.",
      },
    };
  },
});

export const ExtensionDetails = TiptapDetails.extend<ExtensionDetailsOptions>({
  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "A collapsible disclosure with a summary and block-level details content.",
        exposure: "available",
        useWhen: ["Secondary information should be expandable on demand."],
        avoidWhen: [
          "The content is essential and should always remain visible.",
        ],
        attributeGuidance: {
          open: {
            description: "Whether the details content is initially expanded.",
            allowedValues: [true, false],
          },
        },
        generation: {
          mode: "direct-html",
        },
        examples: [
          '<details><summary>More information</summary><div data-type="detailsContent"><p>Initially collapsed details.</p></div></details>',
          '<details open><summary>More information</summary><div data-type="detailsContent"><p>Additional details.</p></div></details>',
        ],
      },
    };
  },

  addOptions() {
    return {
      ...this.parent?.(),
      HTMLAttributes: {
        class: "details",
      },
      getCommandMenuItems() {
        return {
          priority: 160,
          icon: markRaw(MingcuteFoldVerticalLine),
          title: "editor.extensions.details.command_item",
          keywords: ["details"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .setDetails()
              .updateAttributes(TiptapDetails.name, { open: true })
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
            isActive: editor.isActive(TiptapDetails.name),
            icon: markRaw(MingcuteFoldVerticalLine),
            title: i18n.global.t("editor.extensions.details.command_item"),
            action: () => {
              if (editor.isActive(TiptapDetails.name)) {
                editor.chain().focus().unsetDetails().run();
              } else {
                editor
                  .chain()
                  .focus()
                  .setDetails()
                  .updateAttributes(TiptapDetails.name, { open: true })
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
                icon: markRaw(MingcuteDelete2Line),
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
    return [ExtensionDetailsSummary, ExtensionDetailsContent];
  },
}).configure({
  persist: true,
});
