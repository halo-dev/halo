import { BlockActionSeparator } from "@/components";
import MdiDeleteForeverOutline from "@/components/icon/MdiDeleteForeverOutline.vue";
import ToolboxItem from "@/components/toolbox/ToolboxItem.vue";
import { i18n } from "@/locales";
import type { EditorState } from "@/tiptap/pm";
import {
  isActive,
  mergeAttributes,
  VueNodeViewRenderer,
  type Editor,
} from "@/tiptap/vue-3";
import type { ExtensionOptions, NodeBubbleMenu } from "@/types";
import { deleteNode } from "@/utils";
import type { ImageOptions } from "@tiptap/extension-image";
import TiptapImage from "@tiptap/extension-image";
import { markRaw } from "vue";
import MdiFileImageBox from "~icons/mdi/file-image-box";
import MdiFormatAlignCenter from "~icons/mdi/format-align-center";
import MdiFormatAlignJustify from "~icons/mdi/format-align-justify";
import MdiFormatAlignLeft from "~icons/mdi/format-align-left";
import MdiFormatAlignRight from "~icons/mdi/format-align-right";
import MdiLink from "~icons/mdi/link";
import MdiLinkVariant from "~icons/mdi/link-variant";
import MdiShare from "~icons/mdi/share";
import MdiTextBoxEditOutline from "~icons/mdi/text-box-edit-outline";
import BubbleItemImageAlt from "./BubbleItemImageAlt.vue";
import BubbleItemImageHref from "./BubbleItemImageHref.vue";
import BubbleItemVideoLink from "./BubbleItemImageLink.vue";
import BubbleItemImageSize from "./BubbleItemImageSize.vue";
import ImageView from "./ImageView.vue";

const Image = TiptapImage.extend<ExtensionOptions & ImageOptions>({
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
      width: {
        default: undefined,
        parseHTML: (element) => {
          const width =
            element.getAttribute("width") || element.style.width || null;
          return width;
        },
        renderHTML: (attributes) => {
          return {
            width: attributes.width,
          };
        },
      },
      height: {
        default: undefined,
        parseHTML: (element) => {
          const height =
            element.getAttribute("height") || element.style.height || null;
          return height;
        },
        renderHTML: (attributes) => {
          return {
            height: attributes.height,
          };
        },
      },
      href: {
        default: null,
        parseHTML: (element) => {
          const href = element.getAttribute("href") || null;
          return href;
        },
        renderHTML: (attributes) => {
          return {
            href: attributes.href,
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

  addNodeView() {
    return VueNodeViewRenderer(ImageView);
  },

  parseHTML() {
    return [
      {
        tag: this.options.allowBase64
          ? "img[src]"
          : 'img[src]:not([src^="data:"])',
      },
    ];
  },
  addOptions() {
    return {
      ...this.parent?.(),
      getToolboxItems({ editor }: { editor: Editor }) {
        return [
          {
            priority: 10,
            component: markRaw(ToolboxItem),
            props: {
              editor,
              icon: markRaw(MdiFileImageBox),
              title: i18n.global.t("editor.common.image"),
              action: () => {
                editor
                  .chain()
                  .focus()
                  .insertContent([{ type: "image", attrs: { src: "" } }])
                  .run();
              },
            },
          },
        ];
      },
      getBubbleMenu({ editor }: { editor: Editor }): NodeBubbleMenu {
        return {
          pluginKey: "imageBubbleMenu",
          shouldShow: ({ state }: { state: EditorState }): boolean => {
            return isActive(state, Image.name);
          },
          defaultAnimation: false,
          items: [
            {
              priority: 10,
              component: markRaw(BubbleItemImageSize),
            },
            {
              priority: 20,
              props: {
                isActive: () => editor.isActive({ textAlign: "left" }),
                icon: markRaw(MdiFormatAlignLeft),
                action: () => handleSetTextAlign(editor, "left"),
              },
            },
            {
              priority: 30,
              props: {
                isActive: () => editor.isActive({ textAlign: "center" }),
                icon: markRaw(MdiFormatAlignCenter),
                action: () => handleSetTextAlign(editor, "center"),
              },
            },
            {
              priority: 40,
              props: {
                isActive: () => editor.isActive({ textAlign: "right" }),
                icon: markRaw(MdiFormatAlignRight),
                action: () => handleSetTextAlign(editor, "right"),
              },
            },
            {
              priority: 50,
              props: {
                isActive: () => editor.isActive({ textAlign: "justify" }),
                icon: markRaw(MdiFormatAlignJustify),
                action: () => handleSetTextAlign(editor, "justify"),
              },
            },
            {
              priority: 60,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 70,
              props: {
                icon: markRaw(MdiLinkVariant),
                title: i18n.global.t("editor.common.button.edit_link"),
                action: () => {
                  return markRaw(BubbleItemVideoLink);
                },
              },
            },
            {
              priority: 80,
              props: {
                icon: markRaw(MdiShare),
                title: i18n.global.t("editor.common.tooltip.open_link"),
                action: () => {
                  window.open(editor.getAttributes(Image.name).src, "_blank");
                },
              },
            },
            {
              priority: 90,
              props: {
                icon: markRaw(MdiTextBoxEditOutline),
                title: i18n.global.t("editor.extensions.image.edit_alt"),
                action: () => {
                  return markRaw(BubbleItemImageAlt);
                },
              },
            },
            {
              priority: 100,
              props: {
                icon: markRaw(MdiLink),
                title: i18n.global.t("editor.extensions.image.edit_href"),
                action: () => {
                  return markRaw(BubbleItemImageHref);
                },
              },
            },
            {
              priority: 110,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 120,
              props: {
                icon: markRaw(MdiDeleteForeverOutline),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }) => {
                  deleteNode(Image.name, editor);
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
              dragDomOffset: {
                y: -5,
              },
            };
          },
        };
      },
    };
  },
  renderHTML({ HTMLAttributes }) {
    if (HTMLAttributes.href) {
      return [
        "a",
        { href: HTMLAttributes.href },
        ["img", mergeAttributes(HTMLAttributes)],
      ];
    }
    return ["img", mergeAttributes(HTMLAttributes)];
  },
});

const handleSetTextAlign = (
  editor: Editor,
  align: "left" | "center" | "right" | "justify"
) => {
  editor.chain().focus().setTextAlign(align).run();
};

export default Image;
