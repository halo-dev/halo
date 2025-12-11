import { BlockActionSeparator } from "@/components";
import MingcuteDelete2Line from "@/components/icon/MingcuteDelete2Line.vue";
import ToolboxItem from "@/components/toolbox/ToolboxItem.vue";
import { i18n } from "@/locales";
import {
  Editor,
  Node,
  PluginKey,
  VueNodeViewRenderer,
  isActive,
  mergeAttributes,
  nodeInputRule,
  nodePasteRule,
  type EditorState,
  type Range,
} from "@/tiptap";
import type { ExtensionOptions, NodeBubbleMenuType } from "@/types";
import { deleteNode } from "@/utils";
import { isAllowedUri } from "@/utils/is-allowed-uri";
import { markRaw } from "vue";
import MdiBorderAllVariant from "~icons/mdi/border-all-variant";
import MdiBorderNoneVariant from "~icons/mdi/border-none-variant";
import MdiWeb from "~icons/mdi/web";
import MdiWebSync from "~icons/mdi/web-sync";
import MingcuteLinkLine from "~icons/mingcute/link-line";
import MingcuteShare3Line from "~icons/mingcute/share-3-line";
import BubbleItemIframeAlign from "./BubbleItemIframeAlign.vue";
import BubbleIframeLink from "./BubbleItemIframeLink.vue";
import BubbleIframeSize from "./BubbleItemIframeSize.vue";
import IframeView from "./IframeView.vue";

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    iframe: {
      setIframe: (options: { src: string }) => ReturnType;
    };
  }
}

export const IFRAME_BUBBLE_MENU_KEY = new PluginKey("iframeBubbleMenu");

export const ExtensionIframe = Node.create<ExtensionOptions>({
  name: "iframe",
  fakeSelection: true,

  inline() {
    return true;
  },

  group() {
    return "inline";
  },

  addAttributes() {
    return {
      ...this.parent?.(),
      src: {
        default: null,
        parseHTML: (element) => {
          const src = element.getAttribute("src");
          return src;
        },
      },
      width: {
        default: "100%",
        parseHTML: (element) => {
          return element.getAttribute("width");
        },
        renderHTML(attributes) {
          return {
            width: attributes.width,
          };
        },
      },
      height: {
        default: "300px",
        parseHTML: (element) => {
          const height = element.getAttribute("height");
          return height;
        },
        renderHTML: (attributes) => {
          return {
            height: attributes.height,
          };
        },
      },
      scrolling: {
        default: null,
        parseHTML: (element) => {
          return element.getAttribute("scrolling");
        },
        renderHTML: (attributes) => {
          return {
            scrolling: attributes.scrolling,
          };
        },
      },
      frameborder: {
        default: "0",
        parseHTML: (element) => {
          return element.getAttribute("frameborder");
        },
        renderHTML: (attributes) => {
          return {
            frameborder: attributes.frameborder,
          };
        },
      },
      allowfullscreen: {
        default: true,
        parseHTML: (element) => {
          return element.getAttribute("allowfullscreen");
        },
        renderHTML: (attributes) => {
          return {
            allowfullscreen: attributes.allowfullscreen,
          };
        },
      },
      framespacing: {
        default: 0,
        parseHTML: (element) => {
          const framespacing = element.getAttribute("framespacing");
          return framespacing ? parseInt(framespacing, 10) : null;
        },
        renderHTML: (attributes) => {
          return {
            framespacing: attributes.framespacing,
          };
        },
      },
      style: {
        renderHTML() {
          return {
            style: "display: inline-block",
          };
        },
      },
    };
  },

  parseHTML() {
    return [
      {
        tag: "iframe",
        getAttrs: (dom) => {
          const src = (dom as HTMLElement).getAttribute("src");

          // prevent XSS attacks
          if (!src || !isAllowedUri(src)) {
            return false;
          }
          return { src };
        },
      },
    ];
  },

  renderHTML({ HTMLAttributes }) {
    // prevent XSS attacks
    if (!isAllowedUri(HTMLAttributes.src)) {
      return ["iframe", mergeAttributes({ ...HTMLAttributes, src: "" })];
    }
    return ["iframe", mergeAttributes(HTMLAttributes)];
  },

  addCommands() {
    return {
      setIframe:
        (options) =>
        ({ commands }) => {
          return commands.insertContent({
            type: this.name,
            attrs: options,
          });
        },
    };
  },

  addInputRules() {
    return [
      nodeInputRule({
        find: /^\$iframe\$$/,
        type: this.type,
        getAttributes: () => {
          return { width: "100%" };
        },
      }),
    ];
  },

  addPasteRules() {
    return [
      nodePasteRule({
        find: /<iframe.*?src="(.*?)".*?<\/iframe>/g,
        type: this.type,
        getAttributes: (match) => {
          const parse = document
            .createRange()
            .createContextualFragment(match[0]);

          const iframe = parse.querySelector("iframe");

          if (!iframe) {
            return;
          }

          return {
            src: iframe.src,
            width: iframe.width || "100%",
            height: iframe.height || "300px",
          };
        },
      }),
    ];
  },

  addNodeView() {
    return VueNodeViewRenderer(IframeView);
  },

  addOptions() {
    return {
      getCommandMenuItems() {
        return {
          priority: 90,
          icon: markRaw(MdiWeb),
          title: "editor.extensions.commands_menu.iframe",
          keywords: ["iframe", "qianruwangye"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .insertContent([{ type: "iframe", attrs: { src: "" } }])
              .run();
          },
        };
      },
      getToolboxItems({ editor }: { editor: Editor }) {
        return [
          {
            priority: 50,
            component: markRaw(ToolboxItem),
            props: {
              editor,
              icon: markRaw(MdiWeb),
              title: i18n.global.t("editor.extensions.commands_menu.iframe"),
              action: () => {
                editor
                  .chain()
                  .focus()
                  .insertContent([{ type: "iframe", attrs: { src: "" } }])
                  .run();
              },
            },
          },
        ];
      },
      getBubbleMenu({ editor }: { editor: Editor }): NodeBubbleMenuType {
        return {
          pluginKey: IFRAME_BUBBLE_MENU_KEY,
          shouldShow: ({ state }: { state: EditorState }) => {
            return isActive(state, ExtensionIframe.name);
          },
          items: [
            {
              priority: 10,
              props: {
                isActive: () =>
                  editor.getAttributes(ExtensionIframe.name).frameborder ===
                  "1",
                icon: markRaw(
                  editor.getAttributes(ExtensionIframe.name).frameborder === "1"
                    ? MdiBorderAllVariant
                    : MdiBorderNoneVariant
                ),
                action: () => {
                  editor
                    .chain()
                    .updateAttributes(ExtensionIframe.name, {
                      frameborder:
                        editor.getAttributes(ExtensionIframe.name)
                          .frameborder === "1"
                          ? "0"
                          : "1",
                    })
                    .focus()
                    .setNodeSelection(editor.state.selection.from)
                    .run();
                },
                title:
                  editor.getAttributes(ExtensionIframe.name).frameborder === "1"
                    ? i18n.global.t(
                        "editor.extensions.iframe.disable_frameborder"
                      )
                    : i18n.global.t(
                        "editor.extensions.iframe.enable_frameborder"
                      ),
              },
            },
            {
              priority: 20,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 30,
              component: markRaw(BubbleIframeSize),
            },
            {
              priority: 40,
              component: markRaw(BubbleItemIframeAlign),
            },
            {
              priority: 50,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 60,
              props: {
                icon: markRaw(MdiWebSync),
                action: () => {
                  editor
                    .chain()
                    .updateAttributes(ExtensionIframe.name, {
                      src: editor.getAttributes(ExtensionIframe.name).src,
                    })
                    .run();
                },
              },
            },
            {
              priority: 70,
              props: {
                icon: markRaw(MingcuteLinkLine),
                title: i18n.global.t("editor.common.button.edit_link"),
                action: () => {
                  return markRaw(BubbleIframeLink);
                },
              },
            },
            {
              priority: 80,
              props: {
                icon: markRaw(MingcuteShare3Line),
                title: i18n.global.t("editor.common.tooltip.open_link"),
                action: () => {
                  window.open(
                    editor.getAttributes(ExtensionIframe.name).src,
                    "_blank"
                  );
                },
              },
            },
            {
              priority: 90,
              props: {
                icon: markRaw(MingcuteDelete2Line),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }) => {
                  deleteNode(ExtensionIframe.name, editor);
                },
              },
            },
          ],
        };
      },
    };
  },
});
