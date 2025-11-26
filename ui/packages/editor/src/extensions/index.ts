import {
  CODE_BLOCK_BUBBLE_MENU_KEY,
  ExtensionCodeBlock,
  type ExtensionCodeBlockOptions,
} from "@/extensions/code-block";
import ExtensionTextStyle from "@/extensions/text-style";
import { i18n } from "@/locales";
import ExtensionDocument from "@tiptap/extension-document";
import ExtensionHardBreak from "@tiptap/extension-hard-break";
import ExtensionHorizontalRule from "@tiptap/extension-horizontal-rule";
import { CharacterCount, Dropcursor, Placeholder } from "@tiptap/extensions";
import { ExtensionCommands } from "../extensions/commands-menu";
import ExtensionAudio, { AUDIO_BUBBLE_MENU_KEY } from "./audio";
import ExtensionBlockquote from "./blockquote";
import ExtensionBold from "./bold";
import ExtensionBulletList from "./bullet-list";
import ExtensionClearFormat from "./clear-format";
import ExtensionCode from "./code";
import ExtensionColor from "./color";
import {
  COLUMNS_BUBBLE_MENU_KEY,
  ExtensionColumn,
  ExtensionColumns,
} from "./columns";
import ExtensionDetails, { DETAILS_BUBBLE_MENU_KEY } from "./details";
import ExtensionFigure from "./figure";
import ExtensionFontSize from "./font-size";
import ExtensionFormatBrush from "./format-brush";
import ExtensionGallery, { GALLERY_BUBBLE_MENU_KEY } from "./gallery";
import ExtensionGapcursor from "./gap-cursor";
import ExtensionHeading from "./heading";
import ExtensionHighlight from "./highlight";
import ExtensionHistory from "./history";
import ExtensionIframe, { IFRAME_BUBBLE_MENU_KEY } from "./iframe";
import ExtensionImage, { IMAGE_BUBBLE_MENU_KEY } from "./image";
import ExtensionIndent from "./indent";
import ExtensionItalic from "./italic";
import ExtensionLink from "./link";
import ExtensionListKeymap from "./list-keymap";
import ExtensionNodeSelected from "./node-selected";
import ExtensionOrderedList from "./ordered-list";
import ExtensionParagraph from "./paragraph";
import { ExtensionRangeSelection, RangeSelection } from "./range-selection";
import ExtensionSearchAndReplace from "./search-and-replace";
import ExtensionStrike from "./strike";
import ExtensionSubscript from "./subscript";
import ExtensionSuperscript from "./superscript";
import ExtensionTable, { TABLE_BUBBLE_MENU_KEY } from "./table";
import ExtensionTaskList from "./task-list";
import ExtensionText, { TEXT_BUBBLE_MENU_KEY } from "./text";
import ExtensionTextAlign from "./text-align";
import ExtensionTrailingNode from "./trailing-node";
import ExtensionUnderline from "./underline";
import ExtensionUpload from "./upload";
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
  ExtensionFigure,
  ExtensionImage,
  ExtensionGallery,
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
  ExtensionCommands,
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
  ExtensionUpload,
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
  ExtensionFigure,
  ExtensionFontSize,
  ExtensionFormatBrush,
  ExtensionGallery,
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
  ExtensionUpload,
  ExtensionVideo,
  GALLERY_BUBBLE_MENU_KEY,
  IFRAME_BUBBLE_MENU_KEY,
  IMAGE_BUBBLE_MENU_KEY,
  RangeSelection,
  TABLE_BUBBLE_MENU_KEY,
  TEXT_BUBBLE_MENU_KEY,
  VIDEO_BUBBLE_MENU_KEY,
};

export type { ExtensionCodeBlockOptions };
