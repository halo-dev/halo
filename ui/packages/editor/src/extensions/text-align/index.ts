import type { ExtensionOptions } from "@/types";
import TiptapTextAlign, {
  type TextAlignOptions,
} from "@tiptap/extension-text-align";

export type ExtensionTextAlignOptions = ExtensionOptions &
  Partial<TextAlignOptions>;

export const ExtensionTextAlign =
  TiptapTextAlign.extend<ExtensionTextAlignOptions>().configure({
    types: ["heading", "paragraph"],
  });
