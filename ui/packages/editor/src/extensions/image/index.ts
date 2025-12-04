import { BlockActionSeparator } from "@/components";
import MingcuteDelete2Line from "@/components/icon/MingcuteDelete2Line.vue";
import ToolboxItem from "@/components/toolbox/ToolboxItem.vue";
import { i18n } from "@/locales";
import {
  findChildren,
  findParentNode,
  isActive,
  mergeAttributes,
  Plugin,
  PluginKey,
  TextSelection,
  VueNodeViewRenderer,
  type Editor,
  type EditorState,
  type Range,
} from "@/tiptap";
import type { ExtensionOptions, NodeBubbleMenuType } from "@/types";
import { deleteNode } from "@/utils";
import type { Attachment } from "@halo-dev/api-client";
import type { ImageOptions } from "@tiptap/extension-image";
import TiptapImage from "@tiptap/extension-image";
import type { AxiosRequestConfig } from "axios";
import { isEmpty } from "es-toolkit/compat";
import { markRaw } from "vue";
import MingcuteBookmarkEditLine from "~icons/mingcute/bookmark-edit-line";
import MingcuteEdit4Line from "~icons/mingcute/edit-4-line";
import MingcuteLink2Line from "~icons/mingcute/link-2-line";
import MingcuteLinkLine from "~icons/mingcute/link-line";
import MingcutePicLine from "~icons/mingcute/pic-line";
import MingcuteShare3Line from "~icons/mingcute/share-3-line";
import { ExtensionFigure } from "../figure";
import { ExtensionFigureCaption } from "../figure/figure-caption";
import { ExtensionParagraph } from "../paragraph";
import BubbleItemImageAlt from "./BubbleItemImageAlt.vue";
import BubbleItemImageHref from "./BubbleItemImageHref.vue";
import BubbleItemImageLink from "./BubbleItemImageLink.vue";
import BubbleItemImagePosition from "./BubbleItemImagePosition.vue";
import BubbleItemImageSize from "./BubbleItemImageSize.vue";
import ImageView from "./ImageView.vue";

export const IMAGE_BUBBLE_MENU_KEY = new PluginKey("imageBubbleMenu");

export type ExtensionImageOptions = ExtensionOptions &
  Partial<ImageOptions> & {
    uploadImage?: (
      file: File,
      options?: AxiosRequestConfig
    ) => Promise<Attachment>;
  };

export const ExtensionImage = TiptapImage.extend<ExtensionImageOptions>({
  fakeSelection: true,

  inline: false,

  group: "block",

  defining: false,

  addAttributes() {
    return {
      ...this.parent?.(),
      src: {
        default: null,
        parseHTML: (element) => {
          return element.getAttribute("src");
        },
      },
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
      position: {
        default: "left",
        parseHTML: (element) => {
          return (
            element.getAttribute("data-position") ||
            element.getAttribute("text-align")
          );
        },
        renderHTML: (attributes) => {
          return {
            "data-position": attributes.position,
          };
        },
      },
      file: {
        default: null,
        renderHTML() {
          return {};
        },
        parseHTML() {
          return null;
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
        tag: this.options.allowBase64 ? "img" : 'img:not([src^="data:"])',
      },
    ];
  },

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey("imageLegacyFormat"),
        appendTransaction: (transactions, _oldState, newState) => {
          const docChanged = transactions.some((tr) => tr.docChanged);
          if (!docChanged) {
            return null;
          }

          const tr = newState.tr;
          let modified = false;

          newState.doc.descendants((node, pos) => {
            if (node.type.name !== ExtensionImage.name) {
              return;
            }

            const $pos = newState.doc.resolve(pos);
            if ($pos.parent.type.name === ExtensionFigure.name) {
              return;
            }

            let position = "left";
            let deletePreviousNode = false;
            let previousNodePos = -1;
            let previousNodeSize = 0;

            const previousNode = $pos.nodeBefore;
            if (
              previousNode &&
              previousNode.type.name === ExtensionParagraph.name
            ) {
              if (previousNode.attrs.textAlign) {
                const positionMap: Record<string, string> = {
                  left: "left",
                  center: "center",
                  right: "right",
                  justify: "center",
                };
                position = positionMap[previousNode.attrs.textAlign] || "left";
              }
              if (previousNode.textContent?.trim().length === 0) {
                deletePreviousNode = true;
                previousNodePos = pos - previousNode.nodeSize;
                previousNodeSize = previousNode.nodeSize;
              }
            }

            const figureNode = newState.schema.nodes.figure.create(
              {
                contentType: "image",
                position,
              },
              [node]
            );

            if (deletePreviousNode) {
              tr.delete(previousNodePos, previousNodePos + previousNodeSize);
              tr.replaceRangeWith(
                pos - previousNodeSize,
                pos - previousNodeSize + node.nodeSize,
                figureNode
              );
            } else {
              tr.replaceRangeWith(pos, pos + node.nodeSize, figureNode);
            }

            modified = true;
          });

          return modified ? tr : null;
        },
      }),
    ];
  },

  addOptions() {
    return {
      ...this.parent?.(),
      uploadImage: undefined,
      getToolboxItems({ editor }: { editor: Editor }) {
        return [
          {
            priority: 10,
            component: markRaw(ToolboxItem),
            props: {
              editor,
              icon: markRaw(MingcutePicLine),
              title: i18n.global.t("editor.common.image"),
              action: () => {
                editor
                  .chain()
                  .focus()
                  .insertContent([
                    {
                      type: "figure",
                      attrs: { contentType: "image" },
                      content: [{ type: "image" }],
                    },
                  ])
                  .run();
              },
            },
          },
        ];
      },
      getCommandMenuItems() {
        return {
          priority: 95,
          icon: markRaw(MingcutePicLine),
          title: "editor.extensions.commands_menu.image",
          keywords: ["image", "tupian"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .insertContent([
                {
                  type: "figure",
                  attrs: { contentType: "image" },
                  content: [{ type: "image" }],
                },
              ])
              .run();
          },
        };
      },
      getBubbleMenu({ editor }: { editor: Editor }): NodeBubbleMenuType {
        return {
          pluginKey: IMAGE_BUBBLE_MENU_KEY,
          shouldShow: ({ state }: { state: EditorState }): boolean => {
            return isActive(state, ExtensionImage.name);
          },
          options: {
            placement: "top-start",
          },
          items: [
            {
              priority: 10,
              component: markRaw(BubbleItemImageSize),
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionImage.name).src
                  );
                },
              },
            },
            {
              priority: 20,
              component: markRaw(BubbleItemImagePosition),
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionImage.name).src
                  );
                },
              },
            },
            {
              priority: 30,
              component: markRaw(BlockActionSeparator),
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionImage.name).src
                  );
                },
              },
            },
            {
              priority: 40,
              props: {
                icon: markRaw(MingcuteLinkLine),
                title: i18n.global.t("editor.common.button.edit_link"),
                action: () => {
                  return markRaw(BubbleItemImageLink);
                },
              },
            },
            {
              priority: 50,
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionImage.name).src
                  );
                },
                icon: markRaw(MingcuteShare3Line),
                title: i18n.global.t("editor.common.tooltip.open_link"),
                action: () => {
                  window.open(
                    editor.getAttributes(ExtensionImage.name).src,
                    "_blank"
                  );
                },
              },
            },
            {
              priority: 60,
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionImage.name).src
                  );
                },
                icon: markRaw(MingcuteEdit4Line),
                title: i18n.global.t("editor.extensions.image.edit_alt"),
                action: () => {
                  return markRaw(BubbleItemImageAlt);
                },
              },
            },
            {
              priority: 70,
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionImage.name).src
                  );
                },
                icon: markRaw(MingcuteLink2Line),
                title: i18n.global.t("editor.extensions.image.edit_href"),
                action: () => {
                  return markRaw(BubbleItemImageHref);
                },
              },
            },
            {
              priority: 80,
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionImage.name).src
                  );
                },
                icon: markRaw(MingcuteBookmarkEditLine),
                title: i18n.global.t("editor.extensions.image.edit_caption"),
                action: ({ editor }) => {
                  const figureParent = findParentNode(
                    (node) => node.type.name === ExtensionFigure.name
                  )(editor.state.selection);

                  if (!figureParent) {
                    return;
                  }

                  const { node, pos } = figureParent;
                  let captionPos = -1;
                  node.forEach((child, offset) => {
                    if (child.type.name === ExtensionFigureCaption.name) {
                      captionPos = pos + offset + 1;
                    }
                  });
                  if (captionPos !== -1) {
                    editor.chain().focus().setTextSelection(captionPos).run();
                    return;
                  }
                  const imageNodePos = findChildren(
                    editor.state.selection.$from.node(),
                    (node) => node.type.name === ExtensionImage.name
                  )[0];
                  const figureCaptionNode =
                    editor.schema.nodes.figureCaption.create({
                      width: imageNodePos.node.attrs.width,
                    });
                  editor
                    .chain()
                    .focus()
                    .command(({ tr }) => {
                      const insertPos = pos + node.nodeSize - 1;
                      tr.insert(insertPos, figureCaptionNode);
                      tr.setSelection(
                        TextSelection.near(tr.doc.resolve(insertPos + 1))
                      );
                      return true;
                    })
                    .run();
                },
              },
            },
            {
              priority: 90,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 100,
              props: {
                icon: markRaw(MingcuteDelete2Line),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }) => {
                  const figureParent = findParentNode(
                    (node) => node.type.name === ExtensionFigure.name
                  )(editor.state.selection);

                  deleteNode(
                    figureParent ? ExtensionFigure.name : ExtensionImage.name,
                    editor
                  );
                },
              },
            },
          ],
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
}).configure({
  inline: true,
  allowBase64: false,
  HTMLAttributes: {
    loading: "lazy",
  },
});
