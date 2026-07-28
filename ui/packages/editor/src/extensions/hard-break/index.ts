import { HardBreak as TiptapHardBreak } from "@tiptap/extension-hard-break";

export const ExtensionHardBreak = TiptapHardBreak.extend({
  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "A line break inside the current text block without starting a new paragraph.",
        exposure: "available",
        useWhen: ["A semantic line break is required within one block."],
        avoidWhen: ["A new paragraph better represents the separation."],
        generation: {
          mode: "direct-html",
        },
        examples: ["<p>First line<br>Second line</p>"],
      },
    };
  },
});
