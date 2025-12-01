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
