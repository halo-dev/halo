import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import type { CodeOptions } from "@tiptap/extension-code";
import TiptapCode from "@tiptap/extension-code";
import { markRaw } from "vue";
import MdiCodeTags from "~icons/mdi/code-tags";

export type ExtensionCodeOptions = Partial<CodeOptions> & ExtensionOptions;

export const ExtensionCode = TiptapCode.extend<ExtensionCodeOptions>({
  exitable: true,
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 100,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("code"),
            icon: markRaw(MdiCodeTags),
            title: i18n.global.t("editor.common.code"),
            action: () => editor.chain().focus().toggleCode().run(),
          },
        };
      },
    };
  },
});
