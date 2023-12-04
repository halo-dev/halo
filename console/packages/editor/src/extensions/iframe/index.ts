import type { ExtensionOptions, NodeBubbleMenu } from "@/types";
import {
  Editor,
  isActive,
  mergeAttributes,
  Node,
  nodeInputRule,
  nodePasteRule,
  type Range,
  VueNodeViewRenderer,
} from "@/tiptap/vue-3";
import type { EditorState } from "@/tiptap/pm";
import { markRaw } from "vue";
import IframeView from "./IframeView.vue";
import MdiWeb from "~icons/mdi/web";
import ToolboxItem from "@/components/toolbox/ToolboxItem.vue";
import { i18n } from "@/locales";
import { BlockActionSeparator } from "@/components";
import BubbleIframeSize from "./BubbleItemIframeSize.vue";
import BubbleIframeLink from "./BubbleItemIframeLink.vue";
import MdiBorderAllVariant from "~icons/mdi/border-all-variant";
import MdiBorderNoneVariant from "~icons/mdi/border-none-variant";
import MdiDesktopMac from "~icons/mdi/desktop-mac";
import MdiTabletIpad from "~icons/mdi/tablet-ipad";
import MdiCellphoneIphone from "~icons/mdi/cellphone-iphone";
import MdiFormatAlignLeft from "~icons/mdi/format-align-left";
import MdiFormatAlignCenter from "~icons/mdi/format-align-center";
import MdiFormatAlignRight from "~icons/mdi/format-align-right";
import MdiFormatAlignJustify from "~icons/mdi/format-align-justify";
import { deleteNode } from "@/utils";
import MdiDeleteForeverOutline from "@/components/icon/MdiDeleteForeverOutline.vue";
import MdiShare from "~icons/mdi/share";
import MdiLinkVariant from "~icons/mdi/link-variant";
import MdiWebSync from "~icons/mdi/web-sync";

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    iframe: {
      setIframe: (options: { src: string }) => ReturnType;
    };
  }
}

const Iframe = Node.create<ExtensionOptions>({
  name: "iframe",

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
      },
    ];
  },

  renderHTML({ HTMLAttributes }) {
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
            priority: 40,
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
      getBubbleMenu({ editor }: { editor: Editor }): NodeBubbleMenu {
        return {
          pluginKey: "iframeBubbleMenu",
          shouldShow: ({ state }: { state: EditorState }) => {
            return isActive(state, Iframe.name);
          },
          items: [
            {
              priority: 10,
              props: {
                isActive: () =>
                  editor.getAttributes(Iframe.name).frameborder === "1",
                icon: markRaw(
                  editor.getAttributes(Iframe.name).frameborder === "1"
                    ? MdiBorderAllVariant
                    : MdiBorderNoneVariant
                ),
                action: () => {
                  editor
                    .chain()
                    .updateAttributes(Iframe.name, {
                      frameborder:
                        editor.getAttributes(Iframe.name).frameborder === "1"
                          ? "0"
                          : "1",
                    })
                    .focus()
                    .setNodeSelection(editor.state.selection.from)
                    .run();
                },
                title:
                  editor.getAttributes(Iframe.name).frameborder === "1"
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
              props: {
                isActive: () => sizeMatch(editor, "390px", "844px"),
                icon: markRaw(MdiCellphoneIphone),
                action: () => {
                  handleSetSize(editor, "390px", "844px");
                },
                title: i18n.global.t("editor.extensions.iframe.phone_size"),
              },
            },
            {
              priority: 50,
              props: {
                isActive: () => sizeMatch(editor, "834px", "1194px"),
                icon: markRaw(MdiTabletIpad),
                action: () => {
                  handleSetSize(editor, "834px", "1194px");
                },
                title: i18n.global.t(
                  "editor.extensions.iframe.tablet_vertical_size"
                ),
              },
            },
            {
              priority: 60,
              props: {
                isActive: () => sizeMatch(editor, "1194px", "834px"),
                icon: markRaw(MdiTabletIpad),
                iconStyle: "transform: rotate(90deg)",
                action: () => {
                  handleSetSize(editor, "1194px", "834px");
                },
                title: i18n.global.t(
                  "editor.extensions.iframe.tablet_horizontal_size"
                ),
              },
            },
            {
              priority: 70,
              props: {
                isActive: () => sizeMatch(editor, "100%", "834px"),
                icon: markRaw(MdiDesktopMac),
                action: () => {
                  handleSetSize(editor, "100%", "834px");
                },
                title: i18n.global.t("editor.extensions.iframe.desktop_size"),
              },
            },
            {
              priority: 80,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 90,
              props: {
                isActive: () => editor.isActive({ textAlign: "left" }),
                icon: markRaw(MdiFormatAlignLeft),
                action: () => handleSetTextAlign(editor, "left"),
              },
            },
            {
              priority: 100,
              props: {
                isActive: () => editor.isActive({ textAlign: "center" }),
                icon: markRaw(MdiFormatAlignCenter),
                action: () => handleSetTextAlign(editor, "center"),
              },
            },
            {
              priority: 110,
              props: {
                isActive: () => editor.isActive({ textAlign: "right" }),
                icon: markRaw(MdiFormatAlignRight),
                action: () => handleSetTextAlign(editor, "right"),
              },
            },
            {
              priority: 120,
              props: {
                isActive: () => editor.isActive({ textAlign: "justify" }),
                icon: markRaw(MdiFormatAlignJustify),
                action: () => handleSetTextAlign(editor, "justify"),
              },
            },
            {
              priority: 130,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 140,
              props: {
                icon: markRaw(MdiWebSync),
                action: () => {
                  editor
                    .chain()
                    .updateAttributes(Iframe.name, {
                      src: editor.getAttributes(Iframe.name).src,
                    })
                    .run();
                },
              },
            },
            {
              priority: 150,
              props: {
                icon: markRaw(MdiLinkVariant),
                title: i18n.global.t("editor.common.button.edit_link"),
                action: () => {
                  return markRaw(BubbleIframeLink);
                },
              },
            },
            {
              priority: 160,
              props: {
                icon: markRaw(MdiShare),
                title: i18n.global.t("editor.common.tooltip.open_link"),
                action: () => {
                  window.open(editor.getAttributes(Iframe.name).src, "_blank");
                },
              },
            },
            {
              priority: 190,
              props: {
                icon: markRaw(MdiDeleteForeverOutline),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }) => {
                  deleteNode(Iframe.name, editor);
                },
              },
            },
          ],
        };
      },
      getDraggable() {
        return {
          getRenderContainer({ dom, view }) {
            let container = dom;
            while (container && container.tagName !== "P") {
              container = container.parentElement as HTMLElement;
            }
            if (container) {
              container = container.firstElementChild
                ?.firstElementChild as HTMLElement;
            }
            let node;
            if (container.firstElementChild) {
              const pos = view.posAtDOM(container.firstElementChild, 0);
              const $pos = view.state.doc.resolve(pos);
              node = $pos.node();
            }

            return {
              node: node,
              el: container as HTMLElement,
            };
          },
        };
      },
    };
  },
});

const sizeMatch = (editor: Editor, width: string, height: string) => {
  const attr = editor.getAttributes(Iframe.name);
  return width === attr.width && height === attr.height;
};

const handleSetSize = (editor: Editor, width: string, height: string) => {
  editor
    .chain()
    .updateAttributes(Iframe.name, { width, height })
    .focus()
    .setNodeSelection(editor.state.selection.from)
    .run();
};

const handleSetTextAlign = (
  editor: Editor,
  align: "left" | "center" | "right" | "justify"
) => {
  editor.chain().focus().setTextAlign(align).run();
};

export default Iframe;
