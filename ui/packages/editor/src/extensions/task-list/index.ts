import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor, Range } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import {
  TaskItem,
  TaskList as TiptapTaskList,
  type TaskListOptions,
} from "@tiptap/extension-list";
import { markRaw } from "vue";
import MdiFormatListCheckbox from "~icons/mdi/format-list-checkbox";

export type ExtensionTaskListOptions = Partial<TaskListOptions> &
  ExtensionOptions;

export const ExtensionTaskList =
  TiptapTaskList.extend<ExtensionTaskListOptions>({
    addOptions() {
      return {
        ...this.parent?.(),
        getToolbarItems({ editor }: { editor: Editor }) {
          return {
            priority: 150,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: editor.isActive("taskList"),
              icon: markRaw(MdiFormatListCheckbox),
              title: i18n.global.t("editor.common.task_list"),
              action: () => editor.chain().focus().toggleTaskList().run(),
            },
          };
        },
        getCommandMenuItems() {
          return {
            priority: 150,
            icon: markRaw(MdiFormatListCheckbox),
            title: "editor.common.task_list",
            keywords: ["tasklist", "renwuliebiao"],
            command: ({ editor, range }: { editor: Editor; range: Range }) => {
              editor.chain().focus().deleteRange(range).toggleTaskList().run();
            },
          };
        },
      };
    },
    addExtensions() {
      return [TaskItem];
    },
  });
