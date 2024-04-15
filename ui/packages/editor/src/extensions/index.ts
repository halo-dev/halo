// Tiptap official extensions
import ExtensionHistory from "./history";
import ExtensionHeading from "./heading";
import ExtensionBold from "./bold";
import ExtensionItalic from "./italic";
import ExtensionStrike from "./strike";
import ExtensionUnderline from "./underline";
import ExtensionHighlight from "./highlight";
import ExtensionBlockquote from "./blockquote";
import ExtensionCode from "./code";
import ExtensionSuperscript from "./superscript";
import ExtensionSubscript from "./subscript";
import ExtensionBulletList from "./bullet-list";
import ExtensionOrderedList from "./ordered-list";
import ExtensionTaskList from "./task-list";
import ExtensionListKeymap from "./list-keymap";
import ExtensionTable from "./table";
import ExtensionTextAlign from "./text-align";
import ExtensionTextStyle from "@tiptap/extension-text-style";
import ExtensionLink from "./link";
import ExtensionColor from "./color";
import ExtensionFontSize from "./font-size";
import ExtensionDropcursor from "@tiptap/extension-dropcursor";
import ExtensionGapcursor from "@tiptap/extension-gapcursor";
import ExtensionHardBreak from "@tiptap/extension-hard-break";
import ExtensionHorizontalRule from "@tiptap/extension-horizontal-rule";
import ExtensionDocument from "@tiptap/extension-document";
import ExtensionParagraph from "./paragraph";
import ExtensionPlaceholder from "@tiptap/extension-placeholder";
import { i18n } from "@/locales";

// Custom extensions
import { ExtensionCommands } from "../extensions/commands-menu";
import { ExtensionCodeBlock, lowlight } from "@/extensions/code-block";
import ExtensionIframe from "./iframe";
import ExtensionVideo from "./video";
import ExtensionAudio from "./audio";
import ExtensionImage from "./image";
import ExtensionIndent from "./indent";
import { ExtensionColumns, ExtensionColumn } from "./columns";
import ExtensionText from "./text";
import ExtensionDraggable from "./draggable";
import ExtensionNodeSelected from "./node-selected";
import ExtensionTrailingNode from "./trailing-node";
import ExtensionSearchAndReplace from "./search-and-replace";
import ExtensionClearFormat from "./clear-format";
import ExtensionFormatBrush from "./format-brush";

const allExtensions = [
  ExtensionBlockquote,
  ExtensionBold,
  ExtensionBulletList,
  ExtensionCode,
  ExtensionDocument,
  ExtensionDropcursor.configure({
    width: 2,
    class: "dropcursor",
    color: "skyblue",
  }),
  ExtensionGapcursor,
  ExtensionHardBreak,
  ExtensionHeading,
  ExtensionHistory,
  ExtensionHorizontalRule,
  ExtensionItalic,
  ExtensionOrderedList,
  ExtensionStrike,
  ExtensionText,
  ExtensionImage,
  ExtensionTaskList,
  ExtensionHighlight,
  ExtensionColor,
  ExtensionFontSize,
  ExtensionLink.configure({
    autolink: true,
    openOnClick: false,
  }),
  ExtensionTextAlign.configure({
    types: ["heading", "paragraph"],
  }),
  ExtensionUnderline,
  ExtensionTable.configure({
    resizable: true,
  }),
  ExtensionSubscript,
  ExtensionSuperscript,
  ExtensionPlaceholder.configure({
    placeholder: i18n.global.t("editor.extensions.commands_menu.placeholder"),
  }),
  ExtensionCommands.configure({
    suggestion: {},
  }),
  ExtensionCodeBlock.configure({
    lowlight,
  }),
  ExtensionIframe,
  ExtensionVideo,
  ExtensionAudio,
  ExtensionIndent,
  ExtensionColumns,
  ExtensionColumn,
  ExtensionNodeSelected,
  ExtensionTrailingNode,
  ExtensionSearchAndReplace,
  ExtensionClearFormat,
  ExtensionFormatBrush,
];

export {
  allExtensions,
  ExtensionBlockquote,
  ExtensionBold,
  ExtensionBulletList,
  ExtensionCode,
  ExtensionDocument,
  ExtensionDropcursor,
  ExtensionGapcursor,
  ExtensionHardBreak,
  ExtensionHeading,
  ExtensionHistory,
  ExtensionHorizontalRule,
  ExtensionItalic,
  ExtensionOrderedList,
  ExtensionStrike,
  ExtensionText,
  ExtensionImage,
  ExtensionTaskList,
  ExtensionLink,
  ExtensionTextAlign,
  ExtensionTextStyle,
  ExtensionUnderline,
  ExtensionTable,
  ExtensionSubscript,
  ExtensionSuperscript,
  ExtensionParagraph,
  ExtensionPlaceholder,
  ExtensionHighlight,
  ExtensionCommands,
  ExtensionCodeBlock,
  lowlight,
  ExtensionIframe,
  ExtensionVideo,
  ExtensionAudio,
  ExtensionColor,
  ExtensionFontSize,
  ExtensionIndent,
  ExtensionDraggable,
  ExtensionColumns,
  ExtensionColumn,
  ExtensionNodeSelected,
  ExtensionTrailingNode,
  ExtensionListKeymap,
  ExtensionSearchAndReplace,
  ExtensionClearFormat,
  ExtensionFormatBrush,
};
