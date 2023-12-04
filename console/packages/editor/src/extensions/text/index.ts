import type { ExtensionOptions, NodeBubbleMenu } from "@/types";
import { Text as TiptapText } from "@tiptap/extension-text";
import { markRaw } from "vue";
import ColorBubbleItem from "@/extensions/color/ColorBubbleItem.vue";
import HighlightBubbleItem from "@/extensions/highlight/HighlightBubbleItem.vue";
import LinkBubbleButton from "@/extensions/link/LinkBubbleButton.vue";
import MdiFormatQuoteOpen from "~icons/mdi/format-quote-open";
import MdiCodeTags from "~icons/mdi/code-tags";
import MdiCodeBracesBox from "~icons/mdi/code-braces-box";
import MdiFormatColor from "~icons/mdi/format-color";
import MdiFormatColorHighlight from "~icons/mdi/format-color-highlight";
import MdiFormatItalic from "~icons/mdi/format-italic";
import MdiLinkVariantOff from "~icons/mdi/link-variant-off";
import MdiShare from "~icons/mdi/share";
import MdiFormatStrikethrough from "~icons/mdi/format-strikethrough";
import MdiFormatSubscript from "~icons/mdi/format-subscript";
import MdiFormatSuperscript from "~icons/mdi/format-superscript";
import MdiFormatAlignLeft from "~icons/mdi/format-align-left";
import MdiFormatAlignCenter from "~icons/mdi/format-align-center";
import MdiFormatAlignRight from "~icons/mdi/format-align-right";
import MdiFormatAlignJustify from "~icons/mdi/format-align-justify";
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
              priority: 51,
              component: markRaw(ColorBubbleItem),
              props: {
                isActive: ({ editor }) => editor.isActive("color"),
                icon: markRaw(MdiFormatColor),
                title: i18n.global.t("editor.common.color"),
              },
            },
            {
              priority: 60,
              props: {
                isActive: ({ editor }) => editor.isActive("blockquote"),
                icon: markRaw(MdiFormatQuoteOpen),
                title: i18n.global.t("editor.common.quote"),
                action: ({ editor }) => {
                  return editor.commands.toggleBlockquote();
                },
              },
            },
            {
              priority: 80,
              props: {
                isActive: ({ editor }) => editor.isActive("code"),
                icon: markRaw(MdiCodeTags),
                title: i18n.global.t("editor.common.code"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleCode().run(),
              },
            },
            {
              priority: 90,
              props: {
                isActive: ({ editor }) => editor.isActive("codeBlock"),
                icon: markRaw(MdiCodeBracesBox),
                title: i18n.global.t("editor.common.codeblock"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleCodeBlock().run(),
              },
            },
            {
              priority: 100,
              props: {
                isActive: ({ editor }) => editor.isActive("superscript"),
                icon: markRaw(MdiFormatSuperscript),
                title: i18n.global.t("editor.common.superscript"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleSuperscript().run(),
              },
            },
            {
              priority: 110,
              props: {
                isActive: ({ editor }) => editor.isActive("subscript"),
                icon: markRaw(MdiFormatSubscript),
                title: i18n.global.t("editor.common.subscript"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleSubscript().run(),
              },
            },
            {
              priority: 120,
              props: {
                isActive: ({ editor }) =>
                  editor.isActive({ textAlign: "left" }),
                icon: markRaw(MdiFormatAlignLeft),
                title: i18n.global.t("editor.common.align_left"),
                action: ({ editor }) =>
                  editor.chain().focus().setTextAlign("left").run(),
              },
            },
            {
              priority: 130,
              props: {
                isActive: ({ editor }) =>
                  editor.isActive({ textAlign: "center" }),
                icon: markRaw(MdiFormatAlignCenter),
                title: i18n.global.t("editor.common.align_center"),
                action: ({ editor }) =>
                  editor.chain().focus().setTextAlign("center").run(),
              },
            },
            {
              priority: 140,
              props: {
                isActive: ({ editor }) =>
                  editor.isActive({ textAlign: "right" }),
                icon: markRaw(MdiFormatAlignRight),
                title: i18n.global.t("editor.common.align_right"),
                action: ({ editor }) =>
                  editor.chain().focus().setTextAlign("right").run(),
              },
            },
            {
              priority: 150,
              props: {
                isActive: ({ editor }) =>
                  editor.isActive({ textAlign: "justify" }),
                icon: markRaw(MdiFormatAlignJustify),
                title: i18n.global.t("editor.common.align_justify"),
                action: ({ editor }) =>
                  editor.chain().focus().setTextAlign("justify").run(),
              },
            },
            {
              priority: 160,
              component: markRaw(LinkBubbleButton),
              props: {
                isActive: ({ editor }) => editor.isActive("link"),
              },
            },
            {
              priority: 170,
              props: {
                isActive: () => false,
                visible: ({ editor }) => editor.isActive("link"),
                icon: markRaw(MdiLinkVariantOff),
                title: i18n.global.t("editor.extensions.link.cancel_link"),
                action: ({ editor }) => editor.commands.unsetLink(),
              },
            },
            {
              priority: 180,
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
