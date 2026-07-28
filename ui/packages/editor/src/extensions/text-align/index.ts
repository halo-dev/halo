import TiptapTextAlign, {
  type TextAlignOptions,
} from "@tiptap/extension-text-align";
import type { ExtensionOptions } from "@/types";

export type ExtensionTextAlignOptions = ExtensionOptions &
  Partial<TextAlignOptions>;

export const ExtensionTextAlign =
  TiptapTextAlign.extend<ExtensionTextAlignOptions>({
    addHaloEditorMetadata() {
      return {
        contributions: (this.options.types ?? []).map((name) => ({
          targets: [{ kind: "node" as const, name }],
          metadata: {
            ai: {
              attributeGuidance: {
                textAlign: {
                  description: "Horizontal alignment of text in this block.",
                  allowedValues: this.options.alignments,
                  omitWhen: ["The editor default alignment is appropriate."],
                },
              },
            },
          },
        })),
      };
    },
  }).configure({
    types: ["heading", "paragraph"],
  });
