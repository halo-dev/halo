import { Audio, type AudioOptions } from "@tiptap/extension-audio";
import { isEmpty } from "es-toolkit/compat";
import { markRaw } from "vue";
import MdiMotionPlay from "~icons/mdi/motion-play";
import MdiMotionPlayOutline from "~icons/mdi/motion-play-outline";
import MdiPlayCircle from "~icons/mdi/play-circle";
import MdiPlayCircleOutline from "~icons/mdi/play-circle-outline";
import MingcuteLinkLine from "~icons/mingcute/link-line";
import MingcuteMusic2Line from "~icons/mingcute/music-2-line";
import MingcuteShare3Line from "~icons/mingcute/share-3-line";
import { BlockActionSeparator } from "@/components";
import MingcuteDelete2Line from "@/components/icon/MingcuteDelete2Line.vue";
import ToolboxItem from "@/components/toolbox/ToolboxItem.vue";
import { i18n } from "@/locales";
import {
  Editor,
  findParentNode,
  isActive,
  nodeInputRule,
  PluginKey,
  VueNodeViewRenderer,
  type Range,
} from "@/tiptap";
import type { EditorState } from "@/tiptap/pm";
import type { ExtensionOptions, NodeBubbleMenuType } from "@/types";
import { deleteNode } from "@/utils";
import type { UploadFile } from "@/utils/upload";
import AudioView from "./AudioView.vue";
import BubbleItemAudioLink from "./BubbleItemAudioLink.vue";
import BubbleItemAudioPosition from "./BubbleItemAudioPosition.vue";

export const AUDIO_BUBBLE_MENU_KEY = new PluginKey("audioBubbleMenu");

export interface ExtensionAudioOptions extends AudioOptions, ExtensionOptions {
  uploadAudio?: UploadFile;
}

export const ExtensionAudio = Audio.extend<ExtensionAudioOptions>({
  fakeSelection: true,

  addAttributes() {
    return {
      ...this.parent?.(),
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
      ...this.parent!(),
      uploadAudio: undefined,
      getCommandMenuItems() {
        return {
          priority: 110,
          icon: markRaw(MingcuteMusic2Line),
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
          component: markRaw(ToolboxItem),
          props: {
            editor,
            icon: markRaw(MingcuteMusic2Line),
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
            return isActive(state, ExtensionAudio.name);
          },
          items: [
            {
              priority: 10,
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionAudio.name).src
                  );
                },
                isActive: () => {
                  return editor.getAttributes(ExtensionAudio.name).autoplay;
                },
                icon: markRaw(
                  editor.getAttributes(ExtensionAudio.name).autoplay
                    ? MdiPlayCircle
                    : MdiPlayCircleOutline
                ),
                action: () => {
                  editor
                    .chain()
                    .updateAttributes(ExtensionAudio.name, {
                      autoplay: editor.getAttributes(ExtensionAudio.name)
                        .autoplay
                        ? false
                        : true,
                    })
                    .setNodeSelection(editor.state.selection.from)
                    .focus()
                    .run();
                },
                title: editor.getAttributes(ExtensionAudio.name).autoplay
                  ? i18n.global.t("editor.extensions.audio.disable_autoplay")
                  : i18n.global.t("editor.extensions.audio.enable_autoplay"),
              },
            },
            {
              priority: 20,
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionAudio.name).src
                  );
                },
                isActive: () => {
                  return editor.getAttributes(ExtensionAudio.name).loop;
                },
                icon: markRaw(
                  editor.getAttributes(ExtensionAudio.name).loop
                    ? MdiMotionPlay
                    : MdiMotionPlayOutline
                ),
                action: () => {
                  editor
                    .chain()
                    .updateAttributes(ExtensionAudio.name, {
                      loop: editor.getAttributes(ExtensionAudio.name).loop
                        ? false
                        : true,
                    })
                    .setNodeSelection(editor.state.selection.from)
                    .focus()
                    .run();
                },
                title: editor.getAttributes(ExtensionAudio.name).loop
                  ? i18n.global.t("editor.extensions.audio.disable_loop")
                  : i18n.global.t("editor.extensions.audio.enable_loop"),
              },
            },
            {
              priority: 30,
              component: markRaw(BlockActionSeparator),
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionAudio.name).src
                  );
                },
              },
            },
            {
              priority: 40,
              component: markRaw(BubbleItemAudioPosition),
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionAudio.name).src
                  );
                },
              },
            },
            {
              priority: 50,
              component: markRaw(BlockActionSeparator),
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionAudio.name).src
                  );
                },
              },
            },
            {
              priority: 60,
              props: {
                icon: markRaw(MingcuteLinkLine),
                title: i18n.global.t("editor.common.button.edit_link"),
                action: () => {
                  return markRaw(BubbleItemAudioLink);
                },
              },
            },
            {
              priority: 70,
              props: {
                visible({ editor }) {
                  return !isEmpty(
                    editor.getAttributes(ExtensionAudio.name).src
                  );
                },
                icon: markRaw(MingcuteShare3Line),
                title: i18n.global.t("editor.common.tooltip.open_link"),
                action: () => {
                  window.open(
                    editor.getAttributes(ExtensionAudio.name).src,
                    "_blank"
                  );
                },
              },
            },
            {
              priority: 80,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 90,
              props: {
                icon: markRaw(MingcuteDelete2Line),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }) => {
                  const figureParent = findParentNode(
                    (node) => node.type.name === "figure"
                  )(editor.state.selection);
                  deleteNode(
                    figureParent ? "figure" : ExtensionAudio.name,
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
