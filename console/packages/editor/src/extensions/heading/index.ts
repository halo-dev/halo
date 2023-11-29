import type { Editor, Range } from "@/tiptap/vue-3";
import TiptapParagraph from "@/extensions/paragraph";
import TiptapHeading from "@tiptap/extension-heading";
import type { HeadingOptions } from "@tiptap/extension-heading";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import ToolbarSubItem from "@/components/toolbar/ToolbarSubItem.vue";
import MdiFormatParagraph from "~icons/mdi/format-paragraph";
import MdiFormatHeaderPound from "~icons/mdi/format-header-pound";
import MdiFormatHeader1 from "~icons/mdi/format-header-1";
import MdiFormatHeader2 from "~icons/mdi/format-header-2";
import MdiFormatHeader3 from "~icons/mdi/format-header-3";
import MdiFormatHeader4 from "~icons/mdi/format-header-4";
import MdiFormatHeader5 from "~icons/mdi/format-header-5";
import MdiFormatHeader6 from "~icons/mdi/format-header-6";
import { markRaw } from "vue";
import { i18n } from "@/locales";
import type { ExtensionOptions } from "@/types";

const Blockquote = TiptapHeading.extend<ExtensionOptions & HeadingOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 30,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive:
              editor.isActive("paragraph") || editor.isActive("heading"),
            icon: markRaw(MdiFormatHeaderPound),
          },
          children: [
            {
              priority: 10,
              component: markRaw(ToolbarSubItem),
              props: {
                editor,
                isActive: editor.isActive("paragraph"),
                icon: markRaw(MdiFormatParagraph),
                title: i18n.global.t("editor.common.heading.paragraph"),
                action: () => editor.chain().focus().setParagraph().run(),
              },
            },
            {
              priority: 20,
              component: markRaw(ToolbarSubItem),
              props: {
                editor,
                isActive: editor.isActive("heading", { level: 1 }),
                icon: markRaw(MdiFormatHeader1),
                title: i18n.global.t("editor.common.heading.header1"),
                action: () =>
                  editor.chain().focus().toggleHeading({ level: 1 }).run(),
              },
            },
            {
              priority: 30,
              component: markRaw(ToolbarSubItem),
              props: {
                editor,
                isActive: editor.isActive("heading", { level: 2 }),
                icon: markRaw(MdiFormatHeader2),
                title: i18n.global.t("editor.common.heading.header2"),
                action: () =>
                  editor.chain().focus().toggleHeading({ level: 2 }).run(),
              },
            },
            {
              priority: 40,
              component: markRaw(ToolbarSubItem),
              props: {
                editor,
                isActive: editor.isActive("heading", { level: 3 }),
                icon: markRaw(MdiFormatHeader3),
                title: i18n.global.t("editor.common.heading.header3"),
                action: () =>
                  editor.chain().focus().toggleHeading({ level: 3 }).run(),
              },
            },
            {
              priority: 50,
              component: markRaw(ToolbarSubItem),
              props: {
                editor,
                isActive: editor.isActive("heading", { level: 4 }),
                icon: markRaw(MdiFormatHeader4),
                title: i18n.global.t("editor.common.heading.header4"),
                action: () =>
                  editor.chain().focus().toggleHeading({ level: 4 }).run(),
              },
            },
            {
              priority: 60,
              component: markRaw(ToolbarSubItem),
              props: {
                editor,
                isActive: editor.isActive("heading", { level: 5 }),
                icon: markRaw(MdiFormatHeader5),
                title: i18n.global.t("editor.common.heading.header5"),
                action: () =>
                  editor.chain().focus().toggleHeading({ level: 5 }).run(),
              },
            },
            {
              priority: 70,
              component: markRaw(ToolbarSubItem),
              props: {
                editor,
                isActive: editor.isActive("heading", { level: 6 }),
                icon: markRaw(MdiFormatHeader6),
                title: i18n.global.t("editor.common.heading.header6"),
                action: () =>
                  editor.chain().focus().toggleHeading({ level: 6 }).run(),
              },
            },
          ],
        };
      },
      getCommandMenuItems() {
        return [
          {
            priority: 10,
            icon: markRaw(MdiFormatParagraph),
            title: "editor.common.heading.paragraph",
            keywords: ["paragraph", "text", "putongwenben"],
            command: ({ editor, range }: { editor: Editor; range: Range }) => {
              editor.chain().focus().deleteRange(range).setParagraph().run();
            },
          },
          {
            priority: 20,
            icon: markRaw(MdiFormatHeader1),
            title: "editor.common.heading.header1",
            keywords: ["h1", "header1", "1", "yijibiaoti"],
            command: ({ editor, range }: { editor: Editor; range: Range }) => {
              editor
                .chain()
                .focus()
                .deleteRange(range)
                .setNode("heading", { level: 1 })
                .run();
            },
          },
          {
            priority: 30,
            icon: markRaw(MdiFormatHeader2),
            title: "editor.common.heading.header2",
            keywords: ["h2", "header2", "2", "erjibiaoti"],
            command: ({ editor, range }: { editor: Editor; range: Range }) => {
              editor
                .chain()
                .focus()
                .deleteRange(range)
                .setNode("heading", { level: 2 })
                .run();
            },
          },
          {
            priority: 40,
            icon: markRaw(MdiFormatHeader3),
            title: "editor.common.heading.header3",
            keywords: ["h3", "header3", "3", "sanjibiaoti"],
            command: ({ editor, range }: { editor: Editor; range: Range }) => {
              editor
                .chain()
                .focus()
                .deleteRange(range)
                .setNode("heading", { level: 3 })
                .run();
            },
          },
          {
            priority: 50,
            icon: markRaw(MdiFormatHeader4),
            title: "editor.common.heading.header4",
            keywords: ["h4", "header4", "4", "sijibiaoti"],
            command: ({ editor, range }: { editor: Editor; range: Range }) => {
              editor
                .chain()
                .focus()
                .deleteRange(range)
                .setNode("heading", { level: 4 })
                .run();
            },
          },
          {
            priority: 60,
            icon: markRaw(MdiFormatHeader5),
            title: "editor.common.heading.header5",
            keywords: ["h5", "header5", "5", "wujibiaoti"],
            command: ({ editor, range }: { editor: Editor; range: Range }) => {
              editor
                .chain()
                .focus()
                .deleteRange(range)
                .setNode("heading", { level: 5 })
                .run();
            },
          },
          {
            priority: 70,
            icon: markRaw(MdiFormatHeader6),
            title: "editor.common.heading.header6",
            keywords: ["h6", "header6", "6", "liujibiaoti"],
            command: ({ editor, range }: { editor: Editor; range: Range }) => {
              editor
                .chain()
                .focus()
                .deleteRange(range)
                .setNode("heading", { level: 6 })
                .run();
            },
          },
        ];
      },
      getDraggable() {
        return {
          getRenderContainer({ dom }: { dom: HTMLElement }) {
            const tagNames = ["H1", "H2", "H3", "H4", "H5", "H6"];
            let container = dom;
            while (container && !tagNames.includes(container.tagName)) {
              container = container.parentElement as HTMLElement;
            }
            if (!container) {
              return {
                el: dom,
              };
            }
            let y;
            switch (container?.tagName) {
              case "H1":
                y = 10;
                break;
              case "H2":
                y = 2;
                break;
              case "H3":
                y = 0;
                break;
              case "H4":
                y = -3;
                break;
              case "H5":
                y = -5;
                break;
              case "H6":
                y = -5;
                break;
              default:
                y = 0;
                break;
            }
            return {
              el: container,
              dragDomOffset: {
                y: y,
              },
            };
          },
        };
      },
    };
  },
  addExtensions() {
    return [TiptapParagraph];
  },
});

export default Blockquote;
