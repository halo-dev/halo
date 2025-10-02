import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor, Range } from "@/tiptap/vue-3";
import type { ExtensionOptions } from "@/types";
import TiptapDetails, { type DetailsOptions } from "@tiptap/extension-details";
import TiptapDetailsContent from "@tiptap/extension-details-content";
import TiptapDetailsSummary from "@tiptap/extension-details-summary";
import { markRaw } from "vue";
import MdiExpandHorizontal from "~icons/mdi/expand-horizontal";

const getRenderContainer = (node: HTMLElement) => {
  let container = node;
  if (container.nodeName === "#text") {
    container = node.parentElement as HTMLElement;
  }

  while (container && container.dataset.type !== "details") {
    container = container.parentElement as HTMLElement;
  }
  return container;
};

const Details = TiptapDetails.extend<ExtensionOptions & DetailsOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      HTMLAttributes: {
        class: "details",
      },
      getCommandMenuItems() {
        return {
          priority: 160,
          icon: markRaw(MdiExpandHorizontal),
          title: "editor.extensions.details.command_item",
          keywords: ["details"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .setDetails()
              .updateAttributes("details", { open: true })
              .run();
          },
        };
      },
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 95,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("details"),
            icon: markRaw(MdiExpandHorizontal),
            title: i18n.global.t("editor.extensions.details.command_item"),
            action: () => {
              if (editor.isActive("details")) {
                editor.chain().focus().unsetDetails().run();
              } else {
                editor
                  .chain()
                  .focus()
                  .setDetails()
                  .updateAttributes("details", { open: true })
                  .run();
              }
            },
          },
        };
      },
      getDraggable() {
        return {
          getRenderContainer({ dom }: { dom: HTMLElement }) {
            return {
              el: getRenderContainer(dom),
            };
          },
        };
      },
    };
  },
  addExtensions() {
    return [TiptapDetailsSummary, TiptapDetailsContent];
  },
});

export default Details;
