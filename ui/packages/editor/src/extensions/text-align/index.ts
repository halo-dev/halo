import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import ToolbarSubItem from "@/components/toolbar/ToolbarSubItem.vue";
import { i18n } from "@/locales";
import { type Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import TiptapTextAlign, {
  type TextAlignOptions,
} from "@tiptap/extension-text-align";
import { markRaw } from "vue";
import MingcuteAlignCenterLine from "~icons/mingcute/align-center-line";
import MingcuteAlignJustifyLine from "~icons/mingcute/align-justify-line";
import MingcuteAlignLeftLine from "~icons/mingcute/align-left-line";
import MingcuteAlignRightLine from "~icons/mingcute/align-right-line";

const iconComponent = {
  left: MingcuteAlignLeftLine,
  center: MingcuteAlignCenterLine,
  right: MingcuteAlignRightLine,
  justify: MingcuteAlignJustifyLine,
};

const getIcon = (editor: Editor) => {
  let icon = MingcuteAlignLeftLine;
  Object.entries(iconComponent).forEach(([key, value]) => {
    if (editor.isActive({ textAlign: key })) {
      icon = value;
      return;
    }
  });
  return icon;
};

export type ExtensionTextAlignOptions = ExtensionOptions &
  Partial<TextAlignOptions>;

export const ExtensionTextAlign =
  TiptapTextAlign.extend<ExtensionTextAlignOptions>({
    addOptions() {
      return {
        ...this.parent?.(),
        getToolbarItems({ editor }: { editor: Editor }) {
          return {
            priority: 180,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: false,
              icon: markRaw(getIcon(editor)),
              title: i18n.global.t("editor.common.align_method"),
            },
            children: [
              {
                priority: 0,
                component: markRaw(ToolbarSubItem),
                props: {
                  editor,
                  isActive: editor.isActive({ textAlign: "left" }),
                  icon: markRaw(MingcuteAlignLeftLine),
                  title: i18n.global.t("editor.common.align_left"),
                  action: () =>
                    editor.chain().focus().setTextAlign("left").run(),
                },
              },
              {
                priority: 10,
                component: markRaw(ToolbarSubItem),
                props: {
                  editor,
                  isActive: editor.isActive({ textAlign: "center" }),
                  icon: markRaw(MingcuteAlignCenterLine),
                  title: i18n.global.t("editor.common.align_center"),
                  action: () =>
                    editor.chain().focus().setTextAlign("center").run(),
                },
              },
              {
                priority: 20,
                component: markRaw(ToolbarSubItem),
                props: {
                  editor,
                  isActive: editor.isActive({ textAlign: "right" }),
                  icon: markRaw(MingcuteAlignRightLine),
                  title: i18n.global.t("editor.common.align_right"),
                  action: () =>
                    editor.chain().focus().setTextAlign("right").run(),
                },
              },
              {
                priority: 30,
                component: markRaw(ToolbarSubItem),
                props: {
                  editor,
                  isActive: editor.isActive({ textAlign: "justify" }),
                  icon: markRaw(MingcuteAlignJustifyLine),
                  title: i18n.global.t("editor.common.align_justify"),
                  action: () =>
                    editor.chain().focus().setTextAlign("justify").run(),
                },
              },
            ],
          };
        },
      };
    },
  }).configure({
    types: ["heading", "paragraph"],
  });
