import type { Editor } from "@/tiptap/vue-3";
import TiptapCode from "@tiptap/extension-code";
import type { CodeOptions } from "@tiptap/extension-code";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import MdiCodeTags from "~icons/mdi/code-tags";
import { markRaw } from "vue";
import { i18n } from "@/locales";
import type { ExtensionOptions } from "@/types";

const Code = TiptapCode.extend<ExtensionOptions & CodeOptions>({
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

export default Code;
