import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import ToolbarSubItem from "@/components/toolbar/ToolbarSubItem.vue";
import { i18n } from "@/locales";
import type { ExtensionOptions, ToolbarItemType } from "@/types";
import { Editor, Extension } from "@tiptap/core";
import { markRaw } from "vue";
import MdiFormatListBulleted from "~icons/mdi/format-list-bulleted";
import MdiFormatListCheckbox from "~icons/mdi/format-list-checkbox";
import MdiFormatListNumbered from "~icons/mdi/format-list-numbered";
import { ExtensionBulletList } from "../bullet-list";
import { ExtensionOrderedList } from "../ordered-list";
import { ExtensionTaskList } from "../task-list";

const listExtensionNames = [
  ExtensionBulletList.name,
  ExtensionOrderedList.name,
  ExtensionTaskList.name,
];

/**
 * Add toolbar items for list, include bullet list, ordered list and task list.
 */
export const ExtensionListExtra = Extension.create<ExtensionOptions>({
  name: "list-extra",
  addOptions() {
    return {
      getToolbarItems({ editor }) {
        const isListExtensionLoaded = editor.extensionManager.extensions.some(
          (extension) => listExtensionNames.includes(extension.name)
        );
        if (!isListExtensionLoaded) {
          return [];
        }

        return {
          priority: 130,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: isListActive(editor),
            icon: markRaw(getListIcon(editor)),
            title: i18n.global.t("editor.common.list"),
            action() {
              if (isListActive(editor)) {
                return;
              }
              editor.chain().focus().toggleBulletList().run();
            },
          },
          children: getListItems(editor),
        };
      },
    };
  },
});

function isListActive(editor: Editor) {
  return (
    editor.isActive(ExtensionBulletList.name) ||
    editor.isActive(ExtensionOrderedList.name) ||
    editor.isActive(ExtensionTaskList.name)
  );
}

function getListIcon(editor: Editor) {
  if (editor.isActive(ExtensionBulletList.name)) {
    return MdiFormatListBulleted;
  }
  if (editor.isActive(ExtensionOrderedList.name)) {
    return MdiFormatListNumbered;
  }
  if (editor.isActive(ExtensionTaskList.name)) {
    return MdiFormatListCheckbox;
  }
  return MdiFormatListBulleted;
}

function isListExtensionLoaded(editor: Editor, extensionName: string) {
  return editor.extensionManager.extensions.some(
    (extension) => extension.name === extensionName
  );
}

function getListItems(editor: Editor) {
  const items: ToolbarItemType[] = [];
  if (isListExtensionLoaded(editor, ExtensionBulletList.name)) {
    items.push({
      priority: 10,
      component: markRaw(ToolbarSubItem),
      props: {
        editor,
        isActive: editor.isActive(ExtensionBulletList.name),
        icon: markRaw(MdiFormatListBulleted),
        title: i18n.global.t("editor.common.bullet_list"),
        action: () => editor.chain().focus().toggleBulletList().run(),
      },
    });
  }

  if (isListExtensionLoaded(editor, ExtensionOrderedList.name)) {
    items.push({
      priority: 20,
      component: markRaw(ToolbarSubItem),
      props: {
        editor,
        isActive: editor.isActive(ExtensionOrderedList.name),
        icon: markRaw(MdiFormatListNumbered),
        title: i18n.global.t("editor.common.ordered_list"),
        action: () => editor.chain().focus().toggleOrderedList().run(),
      },
    });
  }

  if (isListExtensionLoaded(editor, ExtensionTaskList.name)) {
    items.push({
      priority: 30,
      component: markRaw(ToolbarSubItem),
      props: {
        editor,
        isActive: editor.isActive(ExtensionTaskList.name),
        icon: markRaw(MdiFormatListCheckbox),
        title: i18n.global.t("editor.common.task_list"),
        action: () => editor.chain().focus().toggleTaskList().run(),
      },
    });
  }

  return items;
}
