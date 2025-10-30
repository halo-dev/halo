// Tiptap official extensions
import { i18n } from "@/locales";
import ExtensionDocument from "@tiptap/extension-document";
import ExtensionHardBreak from "@tiptap/extension-hard-break";
import ExtensionHorizontalRule from "@tiptap/extension-horizontal-rule";
import { CharacterCount, Dropcursor, Placeholder } from "@tiptap/extensions";
import ExtensionBlockquote from "./blockquote";
import ExtensionBold from "./bold";
import ExtensionBulletList from "./bullet-list";
import ExtensionCode from "./code";
import ExtensionColor from "./color";
import ExtensionDetails, { DETAILS_BUBBLE_MENU_KEY } from "./details";
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
import ExtensionTable, { TABLE_BUBBLE_MENU_KEY } from "./table";
import ExtensionTaskList from "./task-list";
import ExtensionTextAlign from "./text-align";
import ExtensionUnderline from "./underline";

// Custom extensions
import {
  CODE_BLOCK_BUBBLE_MENU_KEY,
  ExtensionCodeBlock,
  type ExtensionCodeBlockOptions,
} from "@/extensions/code-block";
import ExtensionTextStyle from "@/extensions/text-style";
import { ExtensionCommands } from "../extensions/commands-menu";
import ExtensionAudio, { AUDIO_BUBBLE_MENU_KEY } from "./audio";
import ExtensionClearFormat from "./clear-format";
import {
  COLUMNS_BUBBLE_MENU_KEY,
  ExtensionColumn,
  ExtensionColumns,
} from "./columns";
import ExtensionFormatBrush from "./format-brush";
import ExtensionGapcursor from "./gap-cursor";
import ExtensionIframe, { IFRAME_BUBBLE_MENU_KEY } from "./iframe";
import ExtensionImage, { IMAGE_BUBBLE_MENU_KEY } from "./image";
import ExtensionIndent from "./indent";
import ExtensionNodeSelected from "./node-selected";
import { ExtensionRangeSelection, RangeSelection } from "./range-selection";
import ExtensionSearchAndReplace from "./search-and-replace";
import ExtensionText, { TEXT_BUBBLE_MENU_KEY } from "./text";
import ExtensionTrailingNode from "./trailing-node";
import ExtensionVideo, { VIDEO_BUBBLE_MENU_KEY } from "./video";

const allExtensions = [
  ExtensionBlockquote,
  ExtensionBold,
  ExtensionBulletList,
  ExtensionCode,
  ExtensionDocument,
  CharacterCount,
  Dropcursor.configure({
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
  Placeholder.configure({
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
  ExtensionDetails,
];

export {
  allExtensions,
  AUDIO_BUBBLE_MENU_KEY,
  CODE_BLOCK_BUBBLE_MENU_KEY,
  COLUMNS_BUBBLE_MENU_KEY,
  DETAILS_BUBBLE_MENU_KEY,
  ExtensionAudio,
  ExtensionBlockquote,
  ExtensionBold,
  ExtensionBulletList,
  CharacterCount as ExtensionCharacterCount,
  ExtensionClearFormat,
  ExtensionCode,
  ExtensionCodeBlock,
  ExtensionColor,
  ExtensionColumn,
  ExtensionColumns,
  ExtensionCommands,
  ExtensionDetails,
  ExtensionDocument,
  Dropcursor as ExtensionDropcursor,
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
  Placeholder as ExtensionPlaceholder,
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
  IFRAME_BUBBLE_MENU_KEY,
  IMAGE_BUBBLE_MENU_KEY,
  RangeSelection,
  TABLE_BUBBLE_MENU_KEY,
  TEXT_BUBBLE_MENU_KEY,
  VIDEO_BUBBLE_MENU_KEY,
};

export type { ExtensionCodeBlockOptions };
