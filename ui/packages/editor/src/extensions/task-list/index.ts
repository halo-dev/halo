import {
  TaskItem as TiptapTaskItem,
  TaskList as TiptapTaskList,
  type TaskListOptions,
} from "@tiptap/extension-list";
import { markRaw } from "vue";
import MingcuteListCheck3Line from "~icons/mingcute/list-check-3-line";
import type { Editor, Range } from "@/tiptap";
import type { ExtensionOptions } from "@/types";

export type ExtensionTaskListOptions = Partial<TaskListOptions> &
  ExtensionOptions;

const ExtensionTaskItem = TiptapTaskItem.extend({
  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "A checkable task inside a task list. It is not a top-level content block.",
        exposure: "recommended",
        useWhen: ["Adding a completed or incomplete action to a task list."],
        avoidWhen: ["There is no containing task list."],
        attributeGuidance: {
          checked: {
            description: "Whether this task is complete.",
            allowedValues: [true, false],
          },
        },
        generation: {
          mode: "direct-html",
        },
        examples: [
          '<ul data-type="taskList"><li data-type="taskItem" data-checked="false"><label><input type="checkbox"><span></span></label><div><p>Review the draft</p></div></li></ul>',
          '<ul data-type="taskList"><li data-type="taskItem" data-checked="true"><label><input type="checkbox" checked><span></span></label><div><p>Publish the article</p></div></li></ul>',
        ],
      },
      structure: {
        allowedParents: ["taskList"],
        minPerParent: 1,
        description:
          "taskItem may appear only inside taskList, and each task list contains at least one task.",
      },
    };
  },
});

export const ExtensionTaskList =
  TiptapTaskList.extend<ExtensionTaskListOptions>({
    addHaloEditorMetadata() {
      return {
        ai: {
          description: "A checklist of actionable tasks.",
          aliases: ["checklist"],
          exposure: "recommended",
          useWhen: ["Presenting actions with explicit completion state."],
          avoidWhen: ["Items are informational rather than actionable."],
          generation: {
            mode: "direct-html",
          },
          examples: [
            '<ul data-type="taskList"><li data-type="taskItem" data-checked="true"><label><input type="checkbox" checked><span></span></label><div><p>Draft the article</p></div></li><li data-type="taskItem" data-checked="false"><label><input type="checkbox"><span></span></label><div><p>Review the draft</p></div></li></ul>',
          ],
        },
      };
    },

    addOptions() {
      return {
        ...this.parent?.(),
        getCommandMenuItems() {
          return {
            priority: 150,
            icon: markRaw(MingcuteListCheck3Line),
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
      return [ExtensionTaskItem];
    },
  });
