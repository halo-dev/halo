import type { ExtensionOptions, NodeBubbleMenu } from "@/types";
import {
  Editor,
  isActive,
  mergeAttributes,
  Node,
  nodeInputRule,
  type Range,
  VueNodeViewRenderer,
} from "@/tiptap/vue-3";
import type { EditorState } from "@/tiptap/pm";
import { markRaw } from "vue";
import VideoView from "./VideoView.vue";
import MdiVideo from "~icons/mdi/video";
import ToolboxItem from "@/components/toolbox/ToolboxItem.vue";
import { i18n } from "@/locales";
import { BlockActionSeparator } from "@/components";
import BubbleItemVideoSize from "./BubbleItemVideoSize.vue";
import BubbleItemVideoLink from "./BubbleItemVideoLink.vue";
import MdiImageSizeSelectActual from "~icons/mdi/image-size-select-actual";
import MdiImageSizeSelectSmall from "~icons/mdi/image-size-select-small";
import MdiImageSizeSelectLarge from "~icons/mdi/image-size-select-large";
import MdiFormatAlignLeft from "~icons/mdi/format-align-left";
import MdiFormatAlignCenter from "~icons/mdi/format-align-center";
import MdiFormatAlignRight from "~icons/mdi/format-align-right";
import MdiFormatAlignJustify from "~icons/mdi/format-align-justify";
import MdiCogPlay from "~icons/mdi/cog-play";
import MdiCogPlayOutline from "~icons/mdi/cog-play-outline";
import MdiPlayCircle from "~icons/mdi/play-circle";
import MdiPlayCircleOutline from "~icons/mdi/play-circle-outline";
import MdiMotionPlayOutline from "~icons/mdi/motion-play-outline";
import MdiMotionPlay from "~icons/mdi/motion-play";
import MdiLinkVariant from "~icons/mdi/link-variant";
import MdiShare from "~icons/mdi/share";
import { deleteNode } from "@/utils";
import MdiDeleteForeverOutline from "@/components/icon/MdiDeleteForeverOutline.vue";
declare module "@/tiptap" {
  interface Commands<ReturnType> {
    video: {
      setVideo: (options: { src: string }) => ReturnType;
    };
  }
}

const Video = Node.create<ExtensionOptions>({
  name: "video",

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
          return element.getAttribute("src");
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
        default: "auto",
        parseHTML: (element) => {
          return element.getAttribute("height");
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
        getAttributes: () => {
          return { width: "100%" };
        },
      }),
    ];
  },

  addNodeView() {
    return VueNodeViewRenderer(VideoView);
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
                { type: "video", attrs: { src: "" } },
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
                  .insertContent([{ type: "video", attrs: { src: "" } }])
                  .run();
              },
            },
          },
        ];
      },
      getBubbleMenu({ editor }: { editor: Editor }): NodeBubbleMenu {
        return {
          pluginKey: "videoBubbleMenu",
          shouldShow: ({ state }: { state: EditorState }) => {
            return isActive(state, Video.name);
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
            },
            {
              priority: 50,
              component: markRaw(BubbleItemVideoSize),
            },
            {
              priority: 60,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 70,
              props: {
                isActive: () =>
                  editor.getAttributes(Video.name).width === "25%",
                icon: markRaw(MdiImageSizeSelectSmall),
                action: () => handleSetSize(editor, "25%", "auto"),
                title: i18n.global.t("editor.extensions.video.small_size"),
              },
            },
            {
              priority: 80,
              props: {
                isActive: () =>
                  editor.getAttributes(Video.name).width === "50%",
                icon: markRaw(MdiImageSizeSelectLarge),
                action: () => handleSetSize(editor, "50%", "auto"),
                title: i18n.global.t("editor.extensions.video.medium_size"),
              },
            },
            {
              priority: 90,
              props: {
                isActive: () =>
                  editor.getAttributes(Video.name).width === "100%",
                icon: markRaw(MdiImageSizeSelectActual),
                action: () => handleSetSize(editor, "100%", "auto"),
                title: i18n.global.t("editor.extensions.video.large_size"),
              },
            },
            {
              priority: 100,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 110,
              props: {
                isActive: () => editor.isActive({ textAlign: "left" }),
                icon: markRaw(MdiFormatAlignLeft),
                action: () => handleSetTextAlign(editor, "left"),
              },
            },
            {
              priority: 120,
              props: {
                isActive: () => editor.isActive({ textAlign: "center" }),
                icon: markRaw(MdiFormatAlignCenter),
                action: () => handleSetTextAlign(editor, "center"),
              },
            },
            {
              priority: 130,
              props: {
                isActive: () => editor.isActive({ textAlign: "right" }),
                icon: markRaw(MdiFormatAlignRight),
                action: () => handleSetTextAlign(editor, "right"),
              },
            },
            {
              priority: 140,
              props: {
                isActive: () => editor.isActive({ textAlign: "justify" }),
                icon: markRaw(MdiFormatAlignJustify),
                action: () => handleSetTextAlign(editor, "justify"),
              },
            },
            {
              priority: 150,
              component: markRaw(BlockActionSeparator),
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
                icon: markRaw(MdiShare),
                title: i18n.global.t("editor.common.tooltip.open_link"),
                action: () => {
                  window.open(editor.getAttributes(Video.name).src, "_blank");
                },
              },
            },
            {
              priority: 180,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 190,
              props: {
                icon: markRaw(MdiDeleteForeverOutline),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }) => {
                  deleteNode(Video.name, editor);
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

const handleSetSize = (editor: Editor, width: string, height: string) => {
  editor
    .chain()
    .updateAttributes(Video.name, { width, height })
    .setNodeSelection(editor.state.selection.from)
    .focus()
    .run();
};

const handleSetTextAlign = (
  editor: Editor,
  align: "left" | "center" | "right" | "justify"
) => {
  editor.chain().focus().setTextAlign(align).run();
};

export default Video;
