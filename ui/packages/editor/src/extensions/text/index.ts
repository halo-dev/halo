import type { ExtensionOptions, NodeBubbleMenu } from "@/types";
import { Text as TiptapText } from "@tiptap/extension-text";
import { markRaw } from "vue";
import ColorBubbleItem from "@/extensions/color/ColorBubbleItem.vue";
import HighlightBubbleItem from "@/extensions/highlight/HighlightBubbleItem.vue";
import LinkBubbleButton from "@/extensions/link/LinkBubbleButton.vue";
import MdiCodeTags from "~icons/mdi/code-tags";
import MdiFormatColor from "~icons/mdi/format-color";
import MdiFormatColorHighlight from "~icons/mdi/format-color-highlight";
import MdiFormatItalic from "~icons/mdi/format-italic";
import MdiLinkVariantOff from "~icons/mdi/link-variant-off";
import MdiShare from "~icons/mdi/share";
import MdiFormatStrikethrough from "~icons/mdi/format-strikethrough";
import MdiFormatSubscript from "~icons/mdi/format-subscript";
import MdiFormatSuperscript from "~icons/mdi/format-superscript";
import MdiFormatUnderline from "~icons/mdi/format-underline";
import { isActive, isTextSelection } from "@/tiptap/vue-3";
import type { EditorState } from "@/tiptap/pm";
import MdiFormatBold from "~icons/mdi/format-bold";
import { i18n } from "@/locales";

const OTHER_BUBBLE_MENU_TYPES = [
  "audio",
  "video",
  "image",
  "iframe",
  "codeBlock",
];

const Text = TiptapText.extend<ExtensionOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getBubbleMenu(): NodeBubbleMenu {
        return {
          pluginKey: "textBubbleMenu",
          shouldShow: ({ state, from, to }) => {
            const { doc, selection } = state as EditorState;
            const { empty } = selection;
            if (empty) {
              return false;
            }

            if (
              OTHER_BUBBLE_MENU_TYPES.some((type) =>
                isActive(state as EditorState, type)
              )
            ) {
              return false;
            }

            const isEmptyTextBlock =
              doc.textBetween(from || 0, to || 0).length === 0;

            if (isEmptyTextBlock) {
              return false;
            }

            if (!isTextSelection(selection)) {
              return false;
            }

            return true;
          },
          tippyOptions: {
            fixed: false,
          },
          defaultAnimation: false,
          items: [
            {
              priority: 10,
              props: {
                isActive: ({ editor }) => editor.isActive("bold"),
                icon: markRaw(MdiFormatBold),
                title: i18n.global.t("editor.common.bold"),
                action: ({ editor }) => {
                  editor.chain().focus().toggleBold().run();
                },
              },
            },
            {
              priority: 20,
              props: {
                isActive: ({ editor }) => editor.isActive("italic"),
                icon: markRaw(MdiFormatItalic),
                title: i18n.global.t("editor.common.italic"),
                action: ({ editor }) => {
                  editor.chain().focus().toggleItalic().run();
                },
              },
            },
            {
              priority: 30,
              props: {
                isActive: ({ editor }) => editor.isActive("underline"),
                icon: markRaw(MdiFormatUnderline),
                title: i18n.global.t("editor.common.underline"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleUnderline().run(),
              },
            },
            {
              priority: 40,
              props: {
                isActive: ({ editor }) => editor.isActive("strike"),
                icon: markRaw(MdiFormatStrikethrough),
                title: i18n.global.t("editor.common.strike"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleStrike().run(),
              },
            },
            {
              priority: 50,
              component: markRaw(HighlightBubbleItem),
              props: {
                isActive: ({ editor }) => editor.isActive("highlight"),
                icon: markRaw(MdiFormatColorHighlight),
                title: i18n.global.t("editor.common.highlight"),
              },
            },
            {
              priority: 60,
              component: markRaw(ColorBubbleItem),
              props: {
                isActive: ({ editor }) => editor.isActive("color"),
                icon: markRaw(MdiFormatColor),
                title: i18n.global.t("editor.common.color"),
              },
            },
            {
              priority: 70,
              props: {
                isActive: ({ editor }) => editor.isActive("code"),
                icon: markRaw(MdiCodeTags),
                title: i18n.global.t("editor.common.code"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleCode().run(),
              },
            },
            {
              priority: 80,
              props: {
                isActive: ({ editor }) => editor.isActive("superscript"),
                icon: markRaw(MdiFormatSuperscript),
                title: i18n.global.t("editor.common.superscript"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleSuperscript().run(),
              },
            },
            {
              priority: 90,
              props: {
                isActive: ({ editor }) => editor.isActive("subscript"),
                icon: markRaw(MdiFormatSubscript),
                title: i18n.global.t("editor.common.subscript"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleSubscript().run(),
              },
            },
            {
              priority: 100,
              component: markRaw(LinkBubbleButton),
              props: {
                isActive: ({ editor }) => editor.isActive("link"),
              },
            },
            {
              priority: 110,
              props: {
                isActive: () => false,
                visible: ({ editor }) => editor.isActive("link"),
                icon: markRaw(MdiLinkVariantOff),
                title: i18n.global.t("editor.extensions.link.cancel_link"),
                action: ({ editor }) => editor.commands.unsetLink(),
              },
            },
            {
              priority: 120,
              props: {
                isActive: () => false,
                visible: ({ editor }) => editor.isActive("link"),
                icon: markRaw(MdiShare),
                title: i18n.global.t("editor.common.tooltip.open_link"),
                action: ({ editor }) => {
                  const attrs = editor.getAttributes("link");
                  if (attrs?.href) {
                    window.open(attrs.href, "_blank");
                  }
                },
              },
            },
          ],
        };
      },
    };
  },
});

export default Text;
