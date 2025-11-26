import { BlockActionSeparator } from "@/components";
import MdiDeleteForeverOutline from "@/components/icon/MdiDeleteForeverOutline.vue";
import ToolboxItemVue from "@/components/toolbox/ToolboxItem.vue";
import { i18n } from "@/locales";
import {
  Editor,
  findParentNode,
  isActive,
  mergeAttributes,
  Node,
  nodeInputRule,
  PluginKey,
  VueNodeViewRenderer,
  type Range,
} from "@/tiptap";
import type { EditorState } from "@/tiptap/pm";
import type { ExtensionOptions, NodeBubbleMenuType } from "@/types";
import { deleteNode } from "@/utils";
import type { Attachment } from "@halo-dev/api-client";
import type { AxiosRequestConfig } from "axios";
import { isEmpty } from "es-toolkit/compat";
import { markRaw } from "vue";
import MdiLinkVariant from "~icons/mdi/link-variant";
import MdiMotionPlay from "~icons/mdi/motion-play";
import MdiMotionPlayOutline from "~icons/mdi/motion-play-outline";
import MdiMusicCircleOutline from "~icons/mdi/music-circle-outline";
import MdiPlayCircle from "~icons/mdi/play-circle";
import MdiPlayCircleOutline from "~icons/mdi/play-circle-outline";
import MdiShare from "~icons/mdi/share";
import AudioView from "./AudioView.vue";
import BubbleItemAudioLink from "./BubbleItemAudioLink.vue";

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    audio: {
      setAudio: (options: { src: string }) => ReturnType;
    };
  }
}

export const AUDIO_BUBBLE_MENU_KEY = new PluginKey("audioBubbleMenu");

export interface ExtensionAudioOptions extends ExtensionOptions {
  uploadAudio?: (
    file: File,
    options?: AxiosRequestConfig
  ) => Promise<Attachment>;
}

export const ExtensionAudio = Node.create<ExtensionAudioOptions>({
  name: "audio",
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
        tag: "audio",
      },
    ];
  },

  renderHTML({ HTMLAttributes }) {
    return ["audio", mergeAttributes(HTMLAttributes)];
  },

  addCommands() {
    return {
      setAudio:
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
        find: /^\$audio\$$/,
        type: this.type,
        getAttributes: () => {
          return { width: "100%" };
        },
      }),
    ];
  },

  addNodeView() {
    return VueNodeViewRenderer(AudioView);
  },

  addOptions() {
    return {
      ...this.parent?.(),
      uploadAudio: undefined,
      getCommandMenuItems() {
        return {
          priority: 110,
          icon: markRaw(MdiMusicCircleOutline),
          title: "editor.extensions.commands_menu.audio",
          keywords: ["audio", "yinpin"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .insertContent([
                {
                  type: "figure",
                  attrs: { contentType: "audio" },
                  content: [{ type: "audio", attrs: { src: "" } }],
                },
                { type: "paragraph", content: "" },
              ])
              .run();
          },
        };
      },
      getToolboxItems({ editor }: { editor: Editor }) {
        return {
          priority: 30,
          component: markRaw(ToolboxItemVue),
          props: {
            editor,
            icon: markRaw(MdiMusicCircleOutline),
            title: i18n.global.t("editor.extensions.commands_menu.audio"),
            action: () => {
              editor
                .chain()
                .focus()
                .insertContent([
                  {
                    type: "figure",
                    attrs: { contentType: "audio" },
                    content: [{ type: "audio", attrs: { src: "" } }],
                  },
                ])
                .run();
            },
          },
        };
      },
      getBubbleMenu({ editor }: { editor: Editor }): NodeBubbleMenuType {
        return {
          pluginKey: AUDIO_BUBBLE_MENU_KEY,
          shouldShow: ({ state }: { state: EditorState }) => {
            return isActive(state, Audio.name);
          },
          items: [
            {
              priority: 10,
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Audio.name).src);
                },
                isActive: () => {
                  return editor.getAttributes(Audio.name).autoplay;
                },
                icon: markRaw(
                  editor.getAttributes(Audio.name).autoplay
                    ? MdiPlayCircle
                    : MdiPlayCircleOutline
                ),
                action: () => {
                  editor
                    .chain()
                    .updateAttributes(Audio.name, {
                      autoplay: editor.getAttributes(Audio.name).autoplay
                        ? null
                        : true,
                    })
                    .setNodeSelection(editor.state.selection.from)
                    .focus()
                    .run();
                },
                title: editor.getAttributes(Audio.name).autoplay
                  ? i18n.global.t("editor.extensions.audio.disable_autoplay")
                  : i18n.global.t("editor.extensions.audio.enable_autoplay"),
              },
            },
            {
              priority: 20,
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Audio.name).src);
                },
                isActive: () => {
                  return editor.getAttributes(Audio.name).loop;
                },
                icon: markRaw(
                  editor.getAttributes(Audio.name).loop
                    ? MdiMotionPlay
                    : MdiMotionPlayOutline
                ),
                action: () => {
                  editor
                    .chain()
                    .updateAttributes(Audio.name, {
                      loop: editor.getAttributes(Audio.name).loop ? null : true,
                    })
                    .setNodeSelection(editor.state.selection.from)
                    .focus()
                    .run();
                },
                title: editor.getAttributes(Audio.name).loop
                  ? i18n.global.t("editor.extensions.audio.disable_loop")
                  : i18n.global.t("editor.extensions.audio.enable_loop"),
              },
            },
            {
              priority: 30,
              component: markRaw(BlockActionSeparator),
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Audio.name).src);
                },
              },
            },
            {
              priority: 40,
              props: {
                icon: markRaw(MdiLinkVariant),
                title: i18n.global.t("editor.common.button.edit_link"),
                action: () => {
                  return markRaw(BubbleItemAudioLink);
                },
              },
            },
            {
              priority: 50,
              props: {
                visible({ editor }) {
                  return !isEmpty(editor.getAttributes(Audio.name).src);
                },
                icon: markRaw(MdiShare),
                title: i18n.global.t("editor.common.tooltip.open_link"),
                action: () => {
                  window.open(editor.getAttributes(Audio.name).src, "_blank");
                },
              },
            },
            {
              priority: 60,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 70,
              props: {
                icon: markRaw(MdiDeleteForeverOutline),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }) => {
                  const figureParent = findParentNode(
                    (node) => node.type.name === "figure"
                  )(editor.state.selection);
                  deleteNode(figureParent ? "figure" : Audio.name, editor);
                },
              },
            },
          ],
        };
      },
    };
  },
});
