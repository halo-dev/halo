import TiptapBold, { type BoldOptions } from "@tiptap/extension-bold";
import { markRaw } from "vue";
import MingcuteBoldLine from "~icons/mingcute/bold-line";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";

export type ExtensionBoldOptions = Partial<BoldOptions> & ExtensionOptions;

export const ExtensionBold = TiptapBold.extend<ExtensionBoldOptions>({
  addHaloEditorMetadata() {
    return {
      ai: {
        description: "Strong emphasis for important inline text.",
        exposure: "recommended",
        useWhen: ["Emphasizing a key term or important phrase."],
        avoidWhen: ["Styling long passages or headings solely for appearance."],
        generation: {
          mode: "direct-html",
        },
        examples: ["<p>This is <strong>important</strong>.</p>"],
      },
    };
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 40,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive(TiptapBold.name),
            icon: markRaw(MingcuteBoldLine),
            title: i18n.global.t("editor.common.bold"),
            action: () => {
              editor.chain().focus().toggleBold().run();
            },
          },
        };
      },
    };
  },
});
