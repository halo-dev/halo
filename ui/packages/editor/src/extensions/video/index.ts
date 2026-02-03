import { BlockActionSeparator } from "@/components";
import MingcuteDelete2Line from "@/components/icon/MingcuteDelete2Line.vue";
import ToolboxItem from "@/components/toolbox/ToolboxItem.vue";
import { i18n } from "@/locales";
import {
  Editor,
  findChildren,
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
import type { Attachment } from "@halo-dev/api-client";
import type { AxiosRequestConfig } from "axios";
import { isEmpty } from "es-toolkit/compat";
import { markRaw } from "vue";
import LucideCaptions from "~icons/lucide/captions";
import MdiCogPlay from "~icons/mdi/cog-play";
import MdiCogPlayOutline from "~icons/mdi/cog-play-outline";
import MdiMotionPlay from "~icons/mdi/motion-play";
import MdiMotionPlayOutline from "~icons/mdi/motion-play-outline";
import MdiPlayCircle from "~icons/mdi/play-circle";
import MdiPlayCircleOutline from "~icons/mdi/play-circle-outline";
import MingcuteLinkLine from "~icons/mingcute/link-line";
import MingcuteShare3Line from "~icons/mingcute/share-3-line";
import MingcuteVideoLine from "~icons/mingcute/video-line";
import { ExtensionFigure } from "../figure";
import { ExtensionFigureCaption } from "../figure/figure-caption";
import { ExtensionParagraph } from "../paragraph";
import BubbleItemVideoLink from "./BubbleItemVideoLink.vue";
import BubbleItemVideoPosition from "./BubbleItemVideoPosition.vue";
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

export type ExtensionVideoOptions = ExtensionOptions & {
  uploadVideo?: (
    file: File,
    options?: AxiosRequestConfig
  ) => Promise<Attachment>;
  uploadFromUrl?: (url: string) => Promise<Attachment>;
};

export const ExtensionVideo = Node.create<ExtensionVideoOptions>({
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
        default: "100%",
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
        default: "auto",
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
            if (node.type.name !== ExtensionVideo.name) {
              return;
            }

            const $pos = newState.doc.resolve(pos);
            if ($pos.parent.type.name === ExtensionFigure.name) {
              return;
            }

            let blockPosition = "start";
            let deletePreviousNode = false;
            let previousNodePos = -1;
            let previousNodeSize = 0;

            const previousNode = $pos.nodeBefore;
            if (
              previousNode &&
              previousNode.type.name === ExtensionParagraph.name
            ) {
              if (previousNode.attrs.textAlign) {
                const textAlignToBlockPositionMap: Record<string, string> = {
                  left: "start",
                  center: "center",
                  right: "end",
                  justify: "center",
                };
                blockPosition =
                  textAlignToBlockPositionMap[previousNode.attrs.textAlign] ??
                  "start";
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
                alignItems: blockPosition,
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
      uploadVideo: undefined,
      getCommandMenuItems() {
        return {
          priority: 100,
          icon: markRaw(MingcuteVideoLine),
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
              icon: markRaw(MingcuteVideoLine),
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
            return isActive(state, ExtensionVideo.name);
          },
          options: {
            placement: "top-start",
          },
          items: [
            {
              priority: 10,
              props: {
                isActive: () =>
                  editor.getAttributes(ExtensionVideo.name).controls,
                icon: markRaw(
                  editor.getAttributes(ExtensionVideo.name).controls
                    ? MdiCogPlay
                    : MdiCogPlayOutline
                ),
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionVideo.name).src
                  );
                },
                action: () => {
                  return editor
                    .chain()
                    .updateAttributes(ExtensionVideo.name, {
                      controls: editor.getAttributes(ExtensionVideo.name)
                        .controls
                        ? null
                        : true,
                    })
                    .setNodeSelection(editor.state.selection.from)
                    .focus()
                    .run();
                },
                title: editor.getAttributes(ExtensionVideo.name).controls
                  ? i18n.global.t("editor.extensions.video.disable_controls")
                  : i18n.global.t("editor.extensions.video.enable_controls"),
              },
            },
            {
              priority: 20,
              props: {
                isActive: () => {
                  return editor.getAttributes(ExtensionVideo.name).autoplay;
                },
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionVideo.name).src
                  );
                },
                icon: markRaw(
                  editor.getAttributes(ExtensionVideo.name).autoplay
                    ? MdiPlayCircle
                    : MdiPlayCircleOutline
                ),
                action: () => {
                  return editor
                    .chain()
                    .updateAttributes(ExtensionVideo.name, {
                      autoplay: editor.getAttributes(ExtensionVideo.name)
                        .autoplay
                        ? null
                        : true,
                    })
                    .setNodeSelection(editor.state.selection.from)
                    .focus()
                    .run();
                },
                title: editor.getAttributes(ExtensionVideo.name).autoplay
                  ? i18n.global.t("editor.extensions.video.disable_autoplay")
                  : i18n.global.t("editor.extensions.video.enable_autoplay"),
              },
            },
            {
              priority: 30,
              props: {
                isActive: () => {
                  return editor.getAttributes(ExtensionVideo.name).loop;
                },
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionVideo.name).src
                  );
                },
                icon: markRaw(
                  editor.getAttributes(ExtensionVideo.name).loop
                    ? MdiMotionPlay
                    : MdiMotionPlayOutline
                ),
                action: () => {
                  editor
                    .chain()
                    .updateAttributes(ExtensionVideo.name, {
                      loop: editor.getAttributes(ExtensionVideo.name).loop
                        ? null
                        : true,
                    })
                    .setNodeSelection(editor.state.selection.from)
                    .focus()
                    .run();
                },
                title: editor.getAttributes(ExtensionVideo.name).loop
                  ? i18n.global.t("editor.extensions.video.disable_loop")
                  : i18n.global.t("editor.extensions.video.enable_loop"),
              },
            },
            {
              priority: 40,
              component: markRaw(BlockActionSeparator),
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionVideo.name).src
                  );
                },
              },
            },
            {
              priority: 50,
              component: markRaw(BubbleItemVideoSize),
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionVideo.name).src
                  );
                },
              },
            },
            {
              priority: 60,
              component: markRaw(BubbleItemVideoPosition),
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionVideo.name).src
                  );
                },
              },
            },
            {
              priority: 70,
              component: markRaw(BlockActionSeparator),
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionVideo.name).src
                  );
                },
              },
            },
            {
              priority: 80,
              props: {
                icon: markRaw(MingcuteLinkLine),
                title: i18n.global.t("editor.common.button.edit_link"),
                action: () => {
                  return markRaw(BubbleItemVideoLink);
                },
              },
            },
            {
              priority: 90,
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionVideo.name).src
                  );
                },
                icon: markRaw(MingcuteShare3Line),
                title: i18n.global.t("editor.common.tooltip.open_link"),
                action: () => {
                  window.open(
                    editor.getAttributes(ExtensionVideo.name).src,
                    "_blank"
                  );
                },
              },
            },
            {
              priority: 100,
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionVideo.name).src
                  );
                },
                icon: markRaw(LucideCaptions),
                title: i18n.global.t("editor.extensions.video.edit_caption"),
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
                    (node) => node.type.name === ExtensionVideo.name
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
              priority: 110,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 120,
              props: {
                icon: markRaw(MingcuteDelete2Line),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }) => {
                  const figureParent = findParentNode(
                    (node) => node.type.name === "figure"
                  )(editor.state.selection);

                  deleteNode(
                    figureParent ? "figure" : ExtensionVideo.name,
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

export const handleSetSize = (
  editor: Editor,
  size: { width?: string; height?: string }
) => {
  editor
    .chain()
    .updateAttributes(ExtensionVideo.name, size)
    .setNodeSelection(editor.state.selection.from)
    .focus()
    .run();
};
