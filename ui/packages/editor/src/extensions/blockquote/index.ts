import type { BlockquoteOptions } from "@tiptap/extension-blockquote";
import TiptapBlockquote from "@tiptap/extension-blockquote";
import { markRaw } from "vue";
import MingcuteBlockquoteLine from "~icons/mingcute/blockquote-line";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";

export type ExtensionBlockquoteOptions = Partial<BlockquoteOptions> &
  ExtensionOptions;

export const ExtensionBlockquote =
  TiptapBlockquote.extend<ExtensionBlockquoteOptions>({
    // Keep the shifted Yuque-style quote shortcut ahead of underline's Mod-U
    // keymap, which otherwise accepts the same event through ProseMirror's
    // shifted-character fallback.
    priority: 101,

    addKeyboardShortcuts() {
      return defineHaloKeyboardShortcuts(this, [
        {
          id: "editor.structure.blockquote",
          keys: ["Mod-Shift-u"],
          label: () => i18n.global.t("editor.common.quote"),
          category: "structure",
          priority: 110,
          command: () => this.editor.commands.toggleBlockquote(),
        },
      ]);
    },

    addHaloEditorMetadata() {
      return {
        ai: {
          description:
            "A block quotation containing one or more block-level elements.",
          exposure: "recommended",
          useWhen: ["Quoting a source or visually separating quoted material."],
          avoidWhen: ["Applying indentation to non-quoted prose."],
          generation: {
            mode: "direct-html",
          },
          examples: [
            "<blockquote><p>Quoted material with its original meaning preserved.</p></blockquote>",
            "<blockquote><p>Quoted introduction.</p><ul><li><p>A quoted list item</p></li></ul></blockquote>",
          ],
        },
      };
    },

    addOptions() {
      return {
        ...this.parent?.(),
        getToolbarItems({ editor }: { editor: Editor }) {
          return {
            priority: 90,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: editor.isActive(TiptapBlockquote.name),
              icon: markRaw(MingcuteBlockquoteLine),
              title: i18n.global.t("editor.common.quote"),
              shortcutId: "editor.structure.blockquote",
              action: () => {
                editor.commands.toggleBlockquote();
              },
            },
          };
        },
      };
    },
  });
