import TiptapTextAlign, {
  type TextAlignOptions,
} from "@tiptap/extension-text-align";
import type { ExtensionOptions } from "@/types";

export type ExtensionTextAlignOptions = ExtensionOptions &
  Partial<TextAlignOptions>;

export const ExtensionTextAlign =
  TiptapTextAlign.extend<ExtensionTextAlignOptions>().configure({
    types: ["heading", "paragraph"],
  });
