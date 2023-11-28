import type { Editor, Range } from "@/tiptap/vue-3";
import TiptapTaskList from "@tiptap/extension-task-list";
import type { TaskListOptions } from "@tiptap/extension-task-list";
import ExtensionTaskItem from "@tiptap/extension-task-item";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import MdiFormatListCheckbox from "~icons/mdi/format-list-checkbox";
import { markRaw } from "vue";
import { i18n } from "@/locales";
import type { ExtensionOptions } from "@/types";

const TaskList = TiptapTaskList.extend<ExtensionOptions & TaskListOptions>({
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
      getDraggable() {
        return {
          getRenderContainer({ dom }) {
            let container = dom;
            while (container && !(container.tagName === "LI")) {
              container = container.parentElement as HTMLElement;
            }
            return {
              el: container,
              dragDomOffset: {
                y: -1,
              },
            };
          },
        };
      },
    };
  },
  addExtensions() {
    return [ExtensionTaskItem];
  },
});

export default TaskList;
