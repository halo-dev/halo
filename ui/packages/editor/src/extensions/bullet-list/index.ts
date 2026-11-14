import {
  BulletList as TiptapBulletList,
  type BulletListOptions,
} from "@tiptap/extension-list";
import { markRaw } from "vue";
import MingcuteListCheckLine from "~icons/mingcute/list-check-line";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import type { Editor, Range } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import { ExtensionListItem } from "../list-item";

export type ExtensionBulletListOptions = Partial<BulletListOptions> &
  ExtensionOptions;

export const ExtensionBulletList = TiptapBulletList.extend<ExtensionOptions>({
  addKeyboardShortcuts() {
    return defineHaloKeyboardShortcuts(this, [
      {
        id: "editor.structure.bulletList",
        keys: ["Mod-Shift-8"],
        label: () => i18n.global.t("editor.shortcuts.commands.bullet_list"),
        category: "structure",
        priority: 90,
      },
    ]);
  },

  addHaloEditorMetadata() {
    return {
      ai: {
        description: "An unordered list of related items.",
        aliases: ["unordered list"],
        exposure: "recommended",
        useWhen: ["Item order is not significant."],
        avoidWhen: ["The sequence or ranking of items matters."],
        generation: {
          mode: "direct-html",
        },
        examples: [
          "<ul><li><p>First point</p></li><li><p>Second point</p></li></ul>",
          "<ul><li><p>Parent point</p><ul><li><p>Nested point</p></li></ul></li></ul>",
        ],
      },
    };
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getCommandMenuItems() {
        return {
          priority: 130,
          icon: markRaw(MingcuteListCheckLine),
          title: "editor.common.bullet_list",
          keywords: ["bulletlist", "wuxuliebiao"],
          shortcutId: "editor.structure.bulletList",
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor.chain().focus().deleteRange(range).toggleBulletList().run();
          },
        };
      },
    };
  },
  addExtensions() {
    return [ExtensionListItem];
  },
});
