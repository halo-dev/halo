import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import ToolbarSubItem from "@/components/toolbar/ToolbarSubItem.vue";
import { i18n } from "@/locales";
import { Extension, type Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import { markRaw } from "vue";
import MingcuteAlignCenterLine from "~icons/mingcute/align-center-line";
import MingcuteAlignJustifyLine from "~icons/mingcute/align-justify-line";
import MingcuteAlignLeftLine from "~icons/mingcute/align-left-line";
import MingcuteAlignRightLine from "~icons/mingcute/align-right-line";

const inlineIconComponent = {
  left: MingcuteAlignLeftLine,
  center: MingcuteAlignCenterLine,
  right: MingcuteAlignRightLine,
  justify: MingcuteAlignJustifyLine,
};

const blockIconComponent = {
  start: MingcuteAlignLeftLine,
  center: MingcuteAlignCenterLine,
  end: MingcuteAlignRightLine,
};

const getIcon = (editor: Editor) => {
  let icon = MingcuteAlignLeftLine;
  Object.entries(inlineIconComponent).forEach(([key, value]) => {
    if (editor.isActive({ textAlign: key })) {
      icon = value;
      return;
    }
  });
  Object.entries(blockIconComponent).forEach(([key, value]) => {
    if (editor.isActive({ alignItems: key })) {
      icon = value;
      return;
    }
  });
  return icon;
};

/**
 * The extension for the align attribute. Including inline and block nodes.
 */
export const ExtensionAlign = Extension.create<ExtensionOptions>({
  name: "align",

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
                isActive:
                  editor.isActive({ textAlign: "left" }) ||
                  editor.isActive({ alignItems: "start" }),
                icon: markRaw(MingcuteAlignLeftLine),
                title: i18n.global.t("editor.common.align_left"),
                action: () => {
                  console.log(
                    editor.isActive({ textAlign: "left" }),
                    editor.isActive({ alignItems: "start" })
                  );
                  return editor
                    .chain()
                    .focus()
                    .setTextAlign("left")
                    .setBlockPosition("start")
                    .run();
                },
              },
            },
            {
              priority: 10,
              component: markRaw(ToolbarSubItem),
              props: {
                editor,
                isActive:
                  editor.isActive({ textAlign: "center" }) ||
                  editor.isActive({ alignItems: "center" }),
                icon: markRaw(MingcuteAlignCenterLine),
                title: i18n.global.t("editor.common.align_center"),
                action: () =>
                  editor
                    .chain()
                    .focus()
                    .setTextAlign("center")
                    .setBlockPosition("center")
                    .run(),
              },
            },
            {
              priority: 20,
              component: markRaw(ToolbarSubItem),
              props: {
                editor,
                isActive:
                  editor.isActive({ textAlign: "right" }) ||
                  editor.isActive({ alignItems: "end" }),
                icon: markRaw(MingcuteAlignRightLine),
                title: i18n.global.t("editor.common.align_right"),
                action: () =>
                  editor
                    .chain()
                    .focus()
                    .setTextAlign("right")
                    .setBlockPosition("end")
                    .run(),
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
});
