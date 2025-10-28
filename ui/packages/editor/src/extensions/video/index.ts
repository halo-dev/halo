import { BlockActionSeparator } from "@/components";
import MdiDeleteForeverOutline from "@/components/icon/MdiDeleteForeverOutline.vue";
import ToolboxItem from "@/components/toolbox/ToolboxItem.vue";
import { i18n } from "@/locales";
import {
  Editor,
  findParentNode,
  isActive,
  mergeAttributes,
  Node,
  nodeInputRule,
  Plugin,
  PluginKey,
  TextSelection,
  VueNodeViewRenderer,
  type EditorState,
  type Range,
} from "@/tiptap";
import type { ExtensionOptions, NodeBubbleMenuType } from "@/types";
import { deleteNode } from "@/utils";
import { isEmpty } from "lodash-es";
import { markRaw } from "vue";
import LucideCaptions from "~icons/lucide/captions";
import MdiCogPlay from "~icons/mdi/cog-play";
import MdiCogPlayOutline from "~icons/mdi/cog-play-outline";
import MdiFormatAlignCenter from "~icons/mdi/format-align-center";
import MdiFormatAlignJustify from "~icons/mdi/format-align-justify";
import MdiFormatAlignLeft from "~icons/mdi/format-align-left";
import MdiFormatAlignRight from "~icons/mdi/format-align-right";
import MdiImageSizeSelectActual from "~icons/mdi/image-size-select-actual";
import MdiImageSizeSelectLarge from "~icons/mdi/image-size-select-large";
import MdiImageSizeSelectSmall from "~icons/mdi/image-size-select-small";
import MdiLinkVariant from "~icons/mdi/link-variant";
import MdiMotionPlay from "~icons/mdi/motion-play";
import MdiMotionPlayOutline from "~icons/mdi/motion-play-outline";
import MdiPlayCircle from "~icons/mdi/play-circle";
import MdiPlayCircleOutline from "~icons/mdi/play-circle-outline";
import MdiShare from "~icons/mdi/share";
import MdiVideo from "~icons/mdi/video";
import Figure from "../figure";
import FigureCaption from "../figure/figure-caption";
import Paragraph from "../paragraph";
import BubbleItemVideoLink from "./BubbleItemVideoLink.vue";
import BubbleItemVideoSize from "./BubbleItemVideoSize.vue";
import VideoView from "./VideoView.vue";
declare module "@/tiptap" {
  interface Commands<ReturnType> {
    video: {
      setVideo: (options: { src: string }) => ReturnType;
    };
  }
}

export const VIDEO_BUBBLE_MENU_KEY = new PluginKey("videoBubbleMenu");

const Video = Node.create<ExtensionOptions>({
  name: "video",
  fakeSelection: true,

  inline: false,

  group: "block",

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
          return element.getAttribute("width") || element.style.width || null;
        },
        renderHTML(attributes) {
          return {
            width: attributes.width,
          };
        },
      },
      height: {
        default: undefined,
        parseHTML: (element) => {
          return element.getAttribute("height") || element.style.height || null;
        },
        renderHTML: (attributes) => {
          return {
            height: attributes.height,
          };
        },
      },
      autoplay: {
        default: null,
        parseHTML: (element) => {
          return element.getAttribute("autoplay");
        },
        renderHTML: (attributes) => {
          return {
            autoplay: attributes.autoplay,
          };
        },
      },
      controls: {
        default: true,
        parseHTML: (element) => {
          return element.getAttribute("controls");
        },
        renderHTML: (attributes) => {
          return {
            controls: attributes.controls,
          };
        },
      },
      loop: {
        default: null,
        parseHTML: (element) => {
          return element.getAttribute("loop");
        },
        renderHTML: (attributes) => {
          return {
            loop: attributes.loop,
          };
        },
      },
      textAlign: {
        default: null,
        parseHTML: (element) => {
          return element.getAttribute("text-align");
        },
        renderHTML: (attributes) => {
          return {
            "text-align": attributes.textAlign,
          };
        },
      },
    };
  },

  parseHTML() {
    return [
      {
        tag: "video",
      },
    ];
  },

  renderHTML({ HTMLAttributes }) {
    return ["video", mergeAttributes(HTMLAttributes)];
  },

  addCommands() {
    return {
      setVideo:
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
        find: /^\$video\$$/,
        type: this.type,
      }),
    ];
  },

  addNodeView() {
    return VueNodeViewRenderer(VideoView);
  },

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey("videoLegacyFormat"),
        appendTransaction: (transactions, _oldState, newState) => {
          const docChanged = transactions.some((tr) => tr.docChanged);
          if (!docChanged) {
            return null;
          }

          const tr = newState.tr;
          let modified = false;

          newState.doc.descendants((node, pos) => {
            if (node.type.name !== Video.name) {
              return;
            }

            const $pos = newState.doc.resolve(pos);
            if ($pos.parent.type.name === Figure.name) {
              return;
            }

            let alignItems = "center";
            let deletePreviousNode = false;
            let previousNodePos = -1;
            let previousNodeSize = 0;

            const previousNode = $pos.nodeBefore;
            if (previousNode && previousNode.type.name === Paragraph.name) {
              if (previousNode.attrs.textAlign) {
                const alignMap: Record<string, string> = {
                  left: "start",
                  center: "center",
                  right: "end",
                  justify: "stretch",
                };
                alignItems = alignMap[previousNode.attrs.textAlign] || "center";
              }
              if (previousNode.textContent?.trim().length === 0) {
                deletePreviousNode = true;
                previousNodePos = pos - previousNode.nodeSize;
                previousNodeSize = previousNode.nodeSize;
              }
            }

            const figureNode = newState.schema.nodes.figure.create(
              {
                contentType: "video",
                alignItems,
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
      getCommandMenuItems() {
        return {
          priority: 100,
          icon: markRaw(MdiVideo),
          title: "editor.extensions.commands_menu.video",
          keywords: ["video", "shipin"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .insertContent([
                {
                  type: "figure",
                  attrs: { contentType: "video" },
                  content: [{ type: "video", attrs: { src: "" } }],
                },
                { type: "paragraph", content: "" },
              ])
              .run();
          },
        };
      },
      getToolboxItems({ editor }: { editor: Editor }) {
        return [
          {
            priority: 20,
            component: markRaw(ToolboxItem),
            props: {
              editor,
              icon: markRaw(MdiVideo),
              title: i18n.global.t("editor.extensions.commands_menu.video"),
              action: () => {
                editor
                  .chain()
                  .focus()
                  .insertContent([
                    {
                      type: "figure",
                      attrs: { contentType: "video" },
                      content: [{ type: "video", attrs: { src: "" } }],
                    },
                  ])
                  .run();
              },
            },
          },
        ];
      },
      getBubbleMenu({ editor }: { editor: Editor }): NodeBubbleMenuType {
        return {
          pluginKey: VIDEO_BUBBLE_MENU_KEY,
          shouldShow: ({ state }: { state: EditorState }) => {
            return isActive(state, Video.name);
          },
          options: {
            placement: "top-start",
          },
          items: [
            {
              priority: 10,
              props: {
                isActive: () => editor.getAttributes(Video.name).controls,
                icon: markRaw(
                  editor.getAttributes(Video.name).controls
                    ? MdiCogPlay
                    : MdiCogPlayOutline
                ),
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
                action: () => {
                  return editor
                    .chain()
                    .updateAttributes(Video.name, {
                      controls: editor.getAttributes(Video.name).controls
                        ? null
                        : true,
                    })
                    .setNodeSelection(editor.state.selection.from)
                    .focus()
                    .run();
                },
                title: editor.getAttributes(Video.name).controls
                  ? i18n.global.t("editor.extensions.video.disable_controls")
                  : i18n.global.t("editor.extensions.video.enable_controls"),
              },
            },
            {
              priority: 20,
              props: {
                isActive: () => {
                  return editor.getAttributes(Video.name).autoplay;
                },
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
                icon: markRaw(
                  editor.getAttributes(Video.name).autoplay
                    ? MdiPlayCircle
                    : MdiPlayCircleOutline
                ),
                action: () => {
                  return editor
                    .chain()
                    .updateAttributes(Video.name, {
                      autoplay: editor.getAttributes(Video.name).autoplay
                        ? null
                        : true,
                    })
                    .setNodeSelection(editor.state.selection.from)
                    .focus()
                    .run();
                },
                title: editor.getAttributes(Video.name).autoplay
                  ? i18n.global.t("editor.extensions.video.disable_autoplay")
                  : i18n.global.t("editor.extensions.video.enable_autoplay"),
              },
            },
            {
              priority: 30,
              props: {
                isActive: () => {
                  return editor.getAttributes(Video.name).loop;
                },
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
                icon: markRaw(
                  editor.getAttributes(Video.name).loop
                    ? MdiMotionPlay
                    : MdiMotionPlayOutline
                ),
                action: () => {
                  editor
                    .chain()
                    .updateAttributes(Video.name, {
                      loop: editor.getAttributes(Video.name).loop ? null : true,
                    })
                    .setNodeSelection(editor.state.selection.from)
                    .focus()
                    .run();
                },
                title: editor.getAttributes(Video.name).loop
                  ? i18n.global.t("editor.extensions.video.disable_loop")
                  : i18n.global.t("editor.extensions.video.enable_loop"),
              },
            },
            {
              priority: 40,
              component: markRaw(BlockActionSeparator),
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
              },
            },
            {
              priority: 50,
              component: markRaw(BubbleItemVideoSize),
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
              },
            },
            {
              priority: 60,
              component: markRaw(BlockActionSeparator),
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
              },
            },
            {
              priority: 70,
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
                isActive: () => {
                  const size = getVideoSizePercentage(editor, 25);
                  return (
                    editor.getAttributes(Video.name).width ===
                    `${size?.width}px`
                  );
                },
                icon: markRaw(MdiImageSizeSelectSmall),
                action: () => handleSetSizePercentage(editor, 25),
                title: i18n.global.t("editor.extensions.video.small_size"),
              },
            },
            {
              priority: 80,
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
                isActive: ({ editor }: { editor: Editor }) => {
                  const size = getVideoSizePercentage(editor, 50);
                  return (
                    editor.getAttributes(Video.name).width ===
                    `${size?.width}px`
                  );
                },
                icon: markRaw(MdiImageSizeSelectLarge),
                action: () => handleSetSizePercentage(editor, 50),
                title: i18n.global.t("editor.extensions.video.medium_size"),
              },
            },
            {
              priority: 90,
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
                isActive: () => {
                  const size = getVideoSizePercentage(editor, 100);
                  return (
                    editor.getAttributes(Video.name).width ===
                    `${size?.width}px`
                  );
                },
                icon: markRaw(MdiImageSizeSelectActual),
                action: () => handleSetSizePercentage(editor, 100),
                title: i18n.global.t("editor.extensions.video.large_size"),
              },
            },
            {
              priority: 100,
              component: markRaw(BlockActionSeparator),
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
              },
            },
            {
              priority: 110,
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
                isActive: () => {
                  const figureParent = findParentNode(
                    (node) => node.type.name === Figure.name
                  )(editor.state.selection);
                  if (figureParent) {
                    return figureParent.node.attrs.alignItems === "start";
                  }
                  return editor.isActive({ textAlign: "left" });
                },
                icon: markRaw(MdiFormatAlignLeft),
                action: () => handleSetTextAlign(editor, "start"),
              },
            },
            {
              priority: 120,
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
                isActive: () => {
                  const figureParent = findParentNode(
                    (node) => node.type.name === Figure.name
                  )(editor.state.selection);
                  if (figureParent) {
                    return figureParent.node.attrs.alignItems === "center";
                  }
                  return editor.isActive({ textAlign: "center" });
                },
                icon: markRaw(MdiFormatAlignCenter),
                action: () => handleSetTextAlign(editor, "center"),
              },
            },
            {
              priority: 130,
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
                isActive: () => {
                  const figureParent = findParentNode(
                    (node) => node.type.name === Figure.name
                  )(editor.state.selection);
                  if (figureParent) {
                    return figureParent.node.attrs.alignItems === "end";
                  }
                  return editor.isActive({ textAlign: "right" });
                },
                icon: markRaw(MdiFormatAlignRight),
                action: () => handleSetTextAlign(editor, "end"),
              },
            },
            {
              priority: 140,
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
                isActive: () => {
                  const figureParent = findParentNode(
                    (node) => node.type.name === Figure.name
                  )(editor.state.selection);
                  if (figureParent) {
                    return figureParent.node.attrs.alignItems === "stretch";
                  }
                  return editor.isActive({ textAlign: "justify" });
                },
                icon: markRaw(MdiFormatAlignJustify),
                action: () => handleSetTextAlign(editor, "stretch"),
              },
            },
            {
              priority: 150,
              component: markRaw(BlockActionSeparator),
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
              },
            },
            {
              priority: 160,
              props: {
                icon: markRaw(MdiLinkVariant),
                title: i18n.global.t("editor.common.button.edit_link"),
                action: () => {
                  return markRaw(BubbleItemVideoLink);
                },
              },
            },
            {
              priority: 170,
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
                icon: markRaw(MdiShare),
                title: i18n.global.t("editor.common.tooltip.open_link"),
                action: () => {
                  window.open(editor.getAttributes(Video.name).src, "_blank");
                },
              },
            },
            {
              priority: 180,
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Video.name).src);
                },
                icon: markRaw(LucideCaptions),
                title: i18n.global.t("editor.extensions.video.edit_caption"),
                action: ({ editor }) => {
                  const figureParent = findParentNode(
                    (node) => node.type.name === Figure.name
                  )(editor.state.selection);

                  if (!figureParent) {
                    return;
                  }

                  const { node, pos } = figureParent;
                  let captionPos = -1;

                  node.forEach((child, offset) => {
                    if (child.type.name === FigureCaption.name) {
                      captionPos = pos + offset + 1;
                    }
                  });

                  if (captionPos !== -1) {
                    editor.chain().focus().setTextSelection(captionPos).run();
                    return;
                  }
                  const figureCaptionNode =
                    editor.schema.nodes.figureCaption.create();
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
              priority: 190,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 200,
              props: {
                icon: markRaw(MdiDeleteForeverOutline),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }) => {
                  const figureParent = findParentNode(
                    (node) => node.type.name === "figure"
                  )(editor.state.selection);

                  deleteNode(figureParent ? "figure" : Video.name, editor);
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
              el: container,
            };
          },
        };
      },
    };
  },
});

export const getVideoElement = (editor: Editor): HTMLVideoElement | null => {
  const { view, state } = editor;
  const { from } = state.selection;

  let domNode = view.nodeDOM(from);
  if (!domNode && from > 0) {
    const $pos = state.doc.resolve(from);
    if ($pos.parent) {
      domNode = view.domAtPos(from).node as HTMLElement;
    }
  }

  if (domNode instanceof HTMLElement) {
    let video = domNode.querySelector("video");
    if (video) {
      return video;
    }

    if (domNode.tagName === "VIDEO") {
      return domNode as HTMLVideoElement;
    }

    const parent = domNode.parentElement;
    if (parent) {
      video = parent.querySelector("video");
      if (video) {
        return video;
      }
    }
  }

  return null;
};

export const getVideoSizePercentage = (
  editor: Editor,
  percentage: number,
  videoElement?: HTMLVideoElement | null
): { width: number; height: number } | undefined => {
  const element = videoElement || getVideoElement(editor);
  if (!element || element.readyState < 1) {
    return undefined;
  }
  const videoWidth = element.videoWidth;
  const videoHeight = element.videoHeight;
  const aspectRatio = videoWidth / videoHeight;
  const newWidth = Math.round(videoWidth * (percentage / 100));
  const newHeight = Math.round(newWidth / aspectRatio);
  return { width: newWidth, height: newHeight };
};

export const handleSetSizePercentage = (
  editor: Editor,
  width: number,
  videoElement?: HTMLVideoElement | null
) => {
  const size = getVideoSizePercentage(editor, width, videoElement);
  if (!size) {
    return;
  }
  editor
    .chain()
    .updateAttributes(Video.name, {
      width: `${size.width}px`,
      height: `${size.height}px`,
    })
    .setNodeSelection(editor.state.selection.from)
    .focus()
    .run();
};

const handleSetTextAlign = (
  editor: Editor,
  align: "start" | "center" | "end" | "stretch"
) => {
  const figureParent = findParentNode((node) => node.type.name === Figure.name)(
    editor.state.selection
  );

  const alignMap: Record<string, string> = {
    start: "left",
    center: "center",
    end: "right",
    stretch: "justify",
  };

  if (!figureParent) {
    return editor.chain().focus().setTextAlign(alignMap[align]).run();
  }

  return editor
    .chain()
    .focus()
    .command(({ tr }) => {
      tr.setNodeMarkup(figureParent.pos, undefined, {
        ...figureParent.node.attrs,
        alignItems: align,
      });
      return true;
    })
    .run();
};

export default Video;
