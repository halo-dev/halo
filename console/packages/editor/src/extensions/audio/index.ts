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
import AudioView from "./AudioView.vue";
import MdiMusicCircleOutline from "~icons/mdi/music-circle-outline";
import ToolboxItemVue from "@/components/toolbox/ToolboxItem.vue";
import { i18n } from "@/locales";
import MdiPlayCircle from "~icons/mdi/play-circle";
import MdiPlayCircleOutline from "~icons/mdi/play-circle-outline";
import MdiMotionPlayOutline from "~icons/mdi/motion-play-outline";
import MdiMotionPlay from "~icons/mdi/motion-play";
import { BlockActionSeparator } from "@/components";
import BubbleItemAudioLink from "./BubbleItemAudioLink.vue";
import MdiLinkVariant from "~icons/mdi/link-variant";
import MdiShare from "~icons/mdi/share";
import { deleteNode } from "@/utils";
import MdiDeleteForeverOutline from "@/components/icon/MdiDeleteForeverOutline.vue";

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    audio: {
      setAudio: (options: { src: string }) => ReturnType;
    };
  }
}

const Audio = Node.create<ExtensionOptions>({
  name: "audio",

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
                { type: "audio", attrs: { src: "" } },
                { type: "paragraph", content: "" },
              ])
              .run();
          },
        };
      },
      getToolboxItems({ editor }: { editor: Editor }) {
        return {
          priority: 20,
          component: markRaw(ToolboxItemVue),
          props: {
            editor,
            icon: markRaw(MdiMusicCircleOutline),
            title: i18n.global.t("editor.extensions.commands_menu.audio"),
            action: () => {
              editor
                .chain()
                .focus()
                .insertContent([{ type: "audio", attrs: { src: "" } }])
                .run();
            },
          },
        };
      },
      getBubbleMenu({ editor }: { editor: Editor }): NodeBubbleMenu {
        return {
          pluginKey: "audioBubbleMenu",
          shouldShow: ({ state }: { state: EditorState }) => {
            return isActive(state, Audio.name);
          },
          items: [
            {
              priority: 10,
              props: {
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
                  deleteNode(Audio.name, editor);
                },
              },
            },
          ],
        };
      },
      getDraggable() {
        return {
          getRenderContainer({ dom }) {
            let container = dom;
            while (
              container &&
              !container.hasAttribute("data-node-view-wrapper")
            ) {
              container = container.parentElement as HTMLElement;
            }
            return {
              el: container,
            };
          },
        };
      },
    };
  },
});

export default Audio;
