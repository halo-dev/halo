import ColorBubbleItem from "@/extensions/color/ColorBubbleItem.vue";
import HighlightBubbleItem from "@/extensions/highlight/HighlightBubbleItem.vue";
import LinkBubbleButton from "@/extensions/link/LinkBubbleButton.vue";
import { RangeSelection } from "@/extensions/range-selection";
import { i18n } from "@/locales";
import { PluginKey, type EditorState } from "@/tiptap/pm";
import { isActive, isTextSelection } from "@/tiptap/vue-3";
import type { ExtensionOptions, NodeBubbleMenuType } from "@/types";
import { Text as TiptapText } from "@tiptap/extension-text";
import { markRaw } from "vue";
import MingcuteBoldLine from "~icons/mingcute/bold-line";
import MingcuteCodeLine from "~icons/mingcute/code-line";
import MingcuteItalicLine from "~icons/mingcute/italic-line";
import MingcuteMarkPenLine from "~icons/mingcute/mark-pen-line";
import MingcuteShare3Line from "~icons/mingcute/share-3-line";
import MingcuteStrikethroughLine from "~icons/mingcute/strikethrough-line";
import MingcuteTextColorLine from "~icons/mingcute/text-color-line";
import MingcuteUnderlineLine from "~icons/mingcute/underline-line";
import MingcuteUnlinkLine from "~icons/mingcute/unlink-line";
import PhTextSubscript from "~icons/ph/text-subscript";
import PhTextSuperscript from "~icons/ph/text-superscript";
import { ExtensionBold } from "../bold";
import { ExtensionCode } from "../code";
import { ExtensionColor } from "../color";
import { ExtensionHighlight } from "../highlight";
import { ExtensionItalic } from "../italic";
import { ExtensionLink } from "../link";
import { ExtensionStrike } from "../strike";
import { ExtensionSubscript } from "../subscript";
import { ExtensionSuperscript } from "../superscript";
import { ExtensionUnderline } from "../underline";

const OTHER_BUBBLE_MENU_TYPES = [
  "audio",
  "video",
  "image",
  "iframe",
  "codeBlock",
];

export const TEXT_BUBBLE_MENU_KEY = new PluginKey("textBubbleMenu");

export type ExtensionTextOptions = ExtensionOptions;

export const ExtensionText = TiptapText.extend<ExtensionTextOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getBubbleMenu(): NodeBubbleMenuType {
        return {
          pluginKey: TEXT_BUBBLE_MENU_KEY,
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

            if (
              !isTextSelection(selection) &&
              !(selection instanceof RangeSelection)
            ) {
              return false;
            }

            return true;
          },
          items: [
            {
              priority: 10,
              props: {
                isActive: ({ editor }) => editor.isActive(ExtensionBold.name),
                icon: markRaw(MingcuteBoldLine),
                title: i18n.global.t("editor.common.bold"),
                action: ({ editor }) => {
                  editor.chain().focus().toggleBold().run();
                },
              },
            },
            {
              priority: 20,
              props: {
                isActive: ({ editor }) => editor.isActive(ExtensionItalic.name),
                icon: markRaw(MingcuteItalicLine),
                title: i18n.global.t("editor.common.italic"),
                action: ({ editor }) => {
                  editor.chain().focus().toggleItalic().run();
                },
              },
            },
            {
              priority: 30,
              props: {
                isActive: ({ editor }) =>
                  editor.isActive(ExtensionUnderline.name),
                icon: markRaw(MingcuteUnderlineLine),
                title: i18n.global.t("editor.common.underline"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleUnderline().run(),
              },
            },
            {
              priority: 40,
              props: {
                isActive: ({ editor }) => editor.isActive(ExtensionStrike.name),
                icon: markRaw(MingcuteStrikethroughLine),
                title: i18n.global.t("editor.common.strike"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleStrike().run(),
              },
            },
            {
              priority: 50,
              component: markRaw(HighlightBubbleItem),
              props: {
                isActive: ({ editor }) =>
                  editor.isActive(ExtensionHighlight.name),
                icon: markRaw(MingcuteMarkPenLine),
                title: i18n.global.t("editor.common.highlight"),
              },
            },
            {
              priority: 60,
              component: markRaw(ColorBubbleItem),
              props: {
                isActive: ({ editor }) => editor.isActive(ExtensionColor.name),
                icon: markRaw(MingcuteTextColorLine),
                title: i18n.global.t("editor.common.color"),
              },
            },
            {
              priority: 70,
              props: {
                isActive: ({ editor }) => editor.isActive(ExtensionCode.name),
                icon: markRaw(MingcuteCodeLine),
                title: i18n.global.t("editor.common.code"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleCode().run(),
              },
            },
            {
              priority: 80,
              props: {
                isActive: ({ editor }) =>
                  editor.isActive(ExtensionSuperscript.name),
                icon: markRaw(PhTextSuperscript),
                title: i18n.global.t("editor.common.superscript"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleSuperscript().run(),
              },
            },
            {
              priority: 90,
              props: {
                isActive: ({ editor }) =>
                  editor.isActive(ExtensionSubscript.name),
                icon: markRaw(PhTextSubscript),
                title: i18n.global.t("editor.common.subscript"),
                action: ({ editor }) =>
                  editor.chain().focus().toggleSubscript().run(),
              },
            },
            {
              priority: 100,
              component: markRaw(LinkBubbleButton),
              props: {
                isActive: ({ editor }) => editor.isActive(ExtensionLink.name),
              },
            },
            {
              priority: 110,
              props: {
                isActive: () => false,
                visible: ({ editor }) => editor.isActive(ExtensionLink.name),
                icon: markRaw(MingcuteUnlinkLine),
                title: i18n.global.t("editor.extensions.link.cancel_link"),
                action: ({ editor }) => editor.commands.unsetLink(),
              },
            },
            {
              priority: 120,
              props: {
                isActive: () => false,
                visible: ({ editor }) => editor.isActive(ExtensionLink.name),
                icon: markRaw(MingcuteShare3Line),
                title: i18n.global.t("editor.common.tooltip.open_link"),
                action: ({ editor }) => {
                  const attrs = editor.getAttributes(ExtensionLink.name);
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
