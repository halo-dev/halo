import { Extension, type Extensions } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import type { HardBreakOptions } from "@tiptap/extension-hard-break";
import type { HorizontalRuleOptions } from "@tiptap/extension-horizontal-rule";
import type {
  CharacterCountOptions,
  DropcursorOptions,
  PlaceholderOptions,
} from "@tiptap/extensions";
import { filterDuplicateExtensions } from "../utils";
import type { ExtensionAudioOptions } from "./audio";
import { ExtensionAudio } from "./audio";
import {
  ExtensionBlockquote,
  type ExtensionBlockquoteOptions,
} from "./blockquote";
import { ExtensionBold, type ExtensionBoldOptions } from "./bold";
import {
  ExtensionBulletList,
  type ExtensionBulletListOptions,
} from "./bullet-list";
import { ExtensionCharacterCount } from "./character-count";
import {
  ExtensionClearFormat,
  type ExtensionClearFormatOptions,
} from "./clear-format";
import { ExtensionCode, type ExtensionCodeOptions } from "./code";
import {
  ExtensionCodeBlock,
  type ExtensionCodeBlockOptions,
} from "./code-block";
import { ExtensionColor, type ExtensionColorOptions } from "./color";
import { ExtensionColumns, type ExtensionColumnsOptions } from "./columns";
import { ExtensionCommandsMenu } from "./commands-menu";
import { ExtensionDetails, type ExtensionDetailsOptions } from "./details";
import { ExtensionDocument } from "./document";
import { ExtensionDropcursor } from "./drop-cursor";
import { ExtensionFigure, type ExtensionFigureOptions } from "./figure";
import { ExtensionFontSize, type ExtensionFontSizeOptions } from "./font-size";
import { ExtensionFormatBrush } from "./format-brush";
import { ExtensionGallery, type ExtensionGalleryOptions } from "./gallery";
import { ExtensionGapCursor } from "./gap-cursor";
import { ExtensionHardBreak } from "./hard-break";
import { ExtensionHeading, type ExtensionHeadingOptions } from "./heading";
import {
  ExtensionHighlight,
  type ExtensionHighlightOptions,
} from "./highlight";
import { ExtensionHistory } from "./history";
import { ExtensionHorizontalRule } from "./horizontal-rule";
import { ExtensionIframe } from "./iframe";
import { ExtensionImage, type ExtensionImageOptions } from "./image";
import { ExtensionIndent, type ExtensionIndentOptions } from "./indent";
import { ExtensionItalic, type ExtensionItalicOptions } from "./italic";
import { ExtensionLink, type ExtensionLinkOptions } from "./link";
import {
  ExtensionListKeymap,
  type ExtensionListKeymapOptions,
} from "./list-keymap";
import {
  ExtensionNodeSelected,
  type ExtensionNodeSelectedOptions,
} from "./node-selected";
import {
  ExtensionOrderedList,
  type ExtensionOrderedListOptions,
} from "./ordered-list";
import {
  ExtensionParagraph,
  type ExtensionParagraphOptions,
} from "./paragraph";
import { ExtensionPlaceholder } from "./placeholder";
import { ExtensionRangeSelection } from "./range-selection";
import { ExtensionSearchAndReplace } from "./search-and-replace";
import { ExtensionStrike, type ExtensionStrikeOptions } from "./strike";
import {
  ExtensionSubscript,
  type ExtensionSubscriptOptions,
} from "./subscript";
import {
  ExtensionSuperscript,
  type ExtensionSuperscriptOptions,
} from "./superscript";
import { ExtensionTable, type ExtensionTableOptions } from "./table";
import { ExtensionTaskList, type ExtensionTaskListOptions } from "./task-list";
import { ExtensionText, type ExtensionTextOptions } from "./text";
import {
  ExtensionTextAlign,
  type ExtensionTextAlignOptions,
} from "./text-align";
import {
  ExtensionTextStyle,
  type ExtensionTextStyleOptions,
} from "./text-style";
import { ExtensionTrailingNode } from "./trailing-node";
import {
  ExtensionUnderline,
  type ExtensionUnderlineOptions,
} from "./underline";
import { ExtensionUpload } from "./upload";
import { ExtensionVideo, type ExtensionVideoOptions } from "./video";

export interface ExtensionsKitOptions {
  audio: Partial<ExtensionAudioOptions> | false;
  blockquote: Partial<ExtensionBlockquoteOptions> | false;
  bold: Partial<ExtensionBoldOptions> | false;
  bulletList: Partial<ExtensionBulletListOptions> | false;
  characterCount: Partial<CharacterCountOptions> | false;
  clearFormat: Partial<ExtensionClearFormatOptions> | false;
  code: Partial<ExtensionCodeOptions> | false;
  codeBlock: Partial<ExtensionOptions & ExtensionCodeBlockOptions> | false;
  color: Partial<ExtensionColorOptions> | false;
  columns: Partial<ExtensionColumnsOptions> | false;
  commandsMenu?: false;
  details: Partial<ExtensionDetailsOptions> | false;
  document?: boolean;
  dropCursor: Partial<DropcursorOptions> | false;
  figure: Partial<ExtensionFigureOptions> | false;
  fontSize: Partial<ExtensionFontSizeOptions> | false;
  formatBrush: Partial<ExtensionOptions> | false;
  gallery: Partial<ExtensionGalleryOptions> | false;
  gapCursor?: boolean;
  hardBreak: Partial<HardBreakOptions> | false;
  heading: Partial<ExtensionHeadingOptions> | false;
  highlight: Partial<ExtensionHighlightOptions> | false;
  history: Partial<ExtensionOptions> | false;
  horizontalRule: Partial<HorizontalRuleOptions> | false;
  iframe: Partial<ExtensionOptions> | false;
  image: Partial<ExtensionImageOptions> | false;
  indent: Partial<ExtensionIndentOptions> | false;
  italic: Partial<ExtensionItalicOptions> | false;
  link: Partial<ExtensionLinkOptions> | false;
  listKeymap: Partial<ExtensionListKeymapOptions> | false;
  nodeSelected: Partial<ExtensionNodeSelectedOptions> | false;
  orderedList: Partial<ExtensionOrderedListOptions> | false;
  paragraph: Partial<ExtensionParagraphOptions> | false;
  placeholder: Partial<PlaceholderOptions> | false;
  rangeSelection?: boolean;
  searchAndReplace?: boolean;
  strike: Partial<ExtensionStrikeOptions> | false;
  subscript: Partial<ExtensionSubscriptOptions> | false;
  superscript: Partial<ExtensionSuperscriptOptions> | false;
  table: Partial<ExtensionTableOptions> | false;
  taskList: Partial<ExtensionTaskListOptions> | false;
  text: Partial<ExtensionTextOptions> | false;
  textAlign: Partial<ExtensionTextAlignOptions> | false;
  textStyle: Partial<ExtensionTextStyleOptions> | false;
  trailingNode?: boolean;
  underline: Partial<ExtensionUnderlineOptions> | false;
  upload?: boolean;
  video: Partial<ExtensionVideoOptions> | false;
  customExtensions?: Extensions;
}

export const ExtensionsKit = Extension.create<ExtensionsKitOptions>({
  name: "halo-extensions-kit",
  addExtensions() {
    const internalExtensions: Extensions = [];

    if (this.options.audio !== false) {
      internalExtensions.push(ExtensionAudio.configure(this.options.audio));
    }

    if (this.options.blockquote !== false) {
      internalExtensions.push(
        ExtensionBlockquote.configure(this.options.blockquote)
      );
    }

    if (this.options.bold !== false) {
      internalExtensions.push(ExtensionBold.configure(this.options.bold));
    }

    if (this.options.bulletList !== false) {
      internalExtensions.push(
        ExtensionBulletList.configure(this.options.bulletList)
      );
    }

    if (this.options.characterCount !== false) {
      internalExtensions.push(
        ExtensionCharacterCount.configure(this.options.characterCount)
      );
    }

    if (this.options.clearFormat !== false) {
      internalExtensions.push(
        ExtensionClearFormat.configure(this.options.clearFormat)
      );
    }

    if (this.options.code !== false) {
      internalExtensions.push(ExtensionCode.configure(this.options.code));
    }

    if (this.options.codeBlock !== false) {
      internalExtensions.push(
        ExtensionCodeBlock.configure(this.options.codeBlock)
      );
    }

    if (this.options.color !== false) {
      internalExtensions.push(ExtensionColor.configure(this.options.color));
    }

    if (this.options.columns !== false) {
      internalExtensions.push(ExtensionColumns.configure(this.options.columns));
    }

    if (this.options.commandsMenu !== false) {
      internalExtensions.push(ExtensionCommandsMenu);
    }

    if (this.options.details !== false) {
      internalExtensions.push(ExtensionDetails.configure(this.options.details));
    }

    if (this.options.document !== false) {
      internalExtensions.push(ExtensionDocument);
    }

    if (this.options.dropCursor !== false) {
      internalExtensions.push(
        ExtensionDropcursor.configure(this.options.dropCursor)
      );
    }

    if (this.options.figure !== false) {
      internalExtensions.push(ExtensionFigure.configure(this.options.figure));
    }

    if (this.options.fontSize !== false) {
      internalExtensions.push(
        ExtensionFontSize.configure(this.options.fontSize)
      );
    }

    if (this.options.formatBrush !== false) {
      internalExtensions.push(
        ExtensionFormatBrush.configure(this.options.formatBrush)
      );
    }

    if (this.options.gallery !== false) {
      internalExtensions.push(ExtensionGallery.configure(this.options.gallery));
    }

    if (this.options.gapCursor !== false) {
      internalExtensions.push(ExtensionGapCursor);
    }

    if (this.options.hardBreak !== false) {
      internalExtensions.push(
        ExtensionHardBreak.configure(this.options.hardBreak)
      );
    }

    if (this.options.heading !== false) {
      internalExtensions.push(ExtensionHeading.configure(this.options.heading));
    }

    if (this.options.highlight !== false) {
      internalExtensions.push(
        ExtensionHighlight.configure(this.options.highlight)
      );
    }

    if (this.options.history !== false) {
      internalExtensions.push(ExtensionHistory.configure(this.options.history));
    }

    if (this.options.horizontalRule !== false) {
      internalExtensions.push(
        ExtensionHorizontalRule.configure(this.options.horizontalRule)
      );
    }

    if (this.options.iframe !== false) {
      internalExtensions.push(ExtensionIframe.configure(this.options.iframe));
    }

    if (this.options.image !== false) {
      internalExtensions.push(ExtensionImage.configure(this.options.image));
    }

    if (this.options.indent !== false) {
      internalExtensions.push(ExtensionIndent.configure(this.options.indent));
    }

    if (this.options.italic !== false) {
      internalExtensions.push(ExtensionItalic.configure(this.options.italic));
    }

    if (this.options.link !== false) {
      internalExtensions.push(ExtensionLink.configure(this.options.link));
    }

    if (this.options.listKeymap !== false) {
      internalExtensions.push(
        ExtensionListKeymap.configure(this.options.listKeymap)
      );
    }

    if (this.options.nodeSelected !== false) {
      internalExtensions.push(
        ExtensionNodeSelected.configure(this.options.nodeSelected)
      );
    }

    if (this.options.orderedList !== false) {
      internalExtensions.push(
        ExtensionOrderedList.configure(this.options.orderedList)
      );
    }

    if (this.options.paragraph !== false) {
      internalExtensions.push(
        ExtensionParagraph.configure(this.options.paragraph)
      );
    }

    if (this.options.placeholder !== false) {
      internalExtensions.push(
        ExtensionPlaceholder.configure(this.options.placeholder)
      );
    }

    if (this.options.rangeSelection !== false) {
      internalExtensions.push(ExtensionRangeSelection);
    }

    if (this.options.searchAndReplace !== false) {
      internalExtensions.push(ExtensionSearchAndReplace);
    }

    if (this.options.strike !== false) {
      internalExtensions.push(ExtensionStrike.configure(this.options.strike));
    }

    if (this.options.subscript !== false) {
      internalExtensions.push(
        ExtensionSubscript.configure(this.options.subscript)
      );
    }

    if (this.options.superscript !== false) {
      internalExtensions.push(
        ExtensionSuperscript.configure(this.options.superscript)
      );
    }

    if (this.options.table !== false) {
      internalExtensions.push(ExtensionTable.configure(this.options.table));
    }

    if (this.options.taskList !== false) {
      internalExtensions.push(
        ExtensionTaskList.configure(this.options.taskList)
      );
    }

    if (this.options.text !== false) {
      internalExtensions.push(ExtensionText.configure(this.options.text));
    }

    if (this.options.textAlign !== false) {
      internalExtensions.push(
        ExtensionTextAlign.configure(this.options.textAlign)
      );
    }

    if (this.options.textStyle !== false) {
      internalExtensions.push(
        ExtensionTextStyle.configure(this.options.textStyle)
      );
    }

    if (this.options.trailingNode !== false) {
      internalExtensions.push(ExtensionTrailingNode);
    }

    if (this.options.underline !== false) {
      internalExtensions.push(
        ExtensionUnderline.configure(this.options.underline)
      );
    }

    if (this.options.upload !== false) {
      internalExtensions.push(ExtensionUpload);
    }

    if (this.options.video !== false) {
      internalExtensions.push(ExtensionVideo.configure(this.options.video));
    }

    const extensions =
      filterDuplicateExtensions([
        ...internalExtensions,
        ...(this.options.customExtensions || []),
      ]) || [];

    return extensions;
  },
});
