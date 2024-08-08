// Tiptap official extensions
import { i18n } from "@/locales";
import ExtensionDocument from "@tiptap/extension-document";
import ExtensionDropcursor from "@tiptap/extension-dropcursor";
import ExtensionHardBreak from "@tiptap/extension-hard-break";
import ExtensionHorizontalRule from "@tiptap/extension-horizontal-rule";
import ExtensionPlaceholder from "@tiptap/extension-placeholder";
import ExtensionBlockquote from "./blockquote";
import ExtensionBold from "./bold";
import ExtensionBulletList from "./bullet-list";
import ExtensionCode from "./code";
import ExtensionColor from "./color";
import ExtensionFontSize from "./font-size";
import ExtensionHeading from "./heading";
import ExtensionHighlight from "./highlight";
import ExtensionHistory from "./history";
import ExtensionItalic from "./italic";
import ExtensionLink from "./link";
import ExtensionListKeymap from "./list-keymap";
import ExtensionOrderedList from "./ordered-list";
import ExtensionParagraph from "./paragraph";
import ExtensionStrike from "./strike";
import ExtensionSubscript from "./subscript";
import ExtensionSuperscript from "./superscript";
import ExtensionTable from "./table";
import ExtensionTaskList from "./task-list";
import ExtensionTextAlign from "./text-align";
import ExtensionUnderline from "./underline";

// Custom extensions
import ExtensionTextStyle from "@/extensions/text-style";
import {
  ExtensionCodeBlock,
  type ExtensionCodeBlockOptions,
} from "@/extensions/code-block";
import { ExtensionCommands } from "../extensions/commands-menu";
import ExtensionAudio from "./audio";
import ExtensionClearFormat from "./clear-format";
import { ExtensionColumn, ExtensionColumns } from "./columns";
import ExtensionDraggable from "./draggable";
import ExtensionFormatBrush from "./format-brush";
import ExtensionGapcursor from "./gap-cursor";
import ExtensionIframe from "./iframe";
import ExtensionImage from "./image";
import ExtensionIndent from "./indent";
import ExtensionNodeSelected from "./node-selected";
import { ExtensionRangeSelection, RangeSelection } from "./range-selection";
import ExtensionSearchAndReplace from "./search-and-replace";
import ExtensionText from "./text";
import ExtensionTrailingNode from "./trailing-node";
import ExtensionVideo from "./video";

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
  ExtensionCodeBlock,
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
  ExtensionRangeSelection,
];

export {
  ExtensionAudio,
  ExtensionBlockquote,
  ExtensionBold,
  ExtensionBulletList,
  ExtensionClearFormat,
  ExtensionCode,
  ExtensionCodeBlock,
  ExtensionColor,
  ExtensionColumn,
  ExtensionColumns,
  ExtensionCommands,
  ExtensionDocument,
  ExtensionDraggable,
  ExtensionDropcursor,
  ExtensionFontSize,
  ExtensionFormatBrush,
  ExtensionGapcursor,
  ExtensionHardBreak,
  ExtensionHeading,
  ExtensionHighlight,
  ExtensionHistory,
  ExtensionHorizontalRule,
  ExtensionIframe,
  ExtensionImage,
  ExtensionIndent,
  ExtensionItalic,
  ExtensionLink,
  ExtensionListKeymap,
  ExtensionNodeSelected,
  ExtensionOrderedList,
  ExtensionParagraph,
  ExtensionPlaceholder,
  ExtensionRangeSelection,
  ExtensionSearchAndReplace,
  ExtensionStrike,
  ExtensionSubscript,
  ExtensionSuperscript,
  ExtensionTable,
  ExtensionTaskList,
  ExtensionText,
  ExtensionTextAlign,
  ExtensionTextStyle,
  ExtensionTrailingNode,
  ExtensionUnderline,
  ExtensionVideo,
  RangeSelection,
  allExtensions,
};

export type { ExtensionCodeBlockOptions };
