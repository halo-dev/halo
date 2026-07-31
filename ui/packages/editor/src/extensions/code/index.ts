import type { CodeOptions } from "@tiptap/extension-code";
import TiptapCode from "@tiptap/extension-code";
import { markRaw } from "vue";
import MingcuteCodeLine from "~icons/mingcute/code-line";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";

export type ExtensionCodeOptions = Partial<CodeOptions> & ExtensionOptions;

export const ExtensionCode = TiptapCode.extend<ExtensionCodeOptions>({
  exitable: true,
  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "Inline code for identifiers, commands, short expressions, and file names.",
        exposure: "recommended",
        useWhen: ["Including a short code fragment within prose."],
        avoidWhen: ["Presenting a multi-line code sample."],
        generation: {
          mode: "direct-html",
        },
        examples: ["<p>Run <code>pnpm install</code> first.</p>"],
      },
    };
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 100,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive(TiptapCode.name),
            icon: markRaw(MingcuteCodeLine),
            title: i18n.global.t("editor.common.code"),
            action: () => editor.chain().focus().toggleCode().run(),
          },
        };
      },
    };
  },
});
