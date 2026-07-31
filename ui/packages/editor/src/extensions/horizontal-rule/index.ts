import { HorizontalRule as TiptapHorizontalRule } from "@tiptap/extension-horizontal-rule";

export const ExtensionHorizontalRule = TiptapHorizontalRule.extend({
  addHaloEditorMetadata() {
    return {
      ai: {
        description: "A thematic break between sections of content.",
        exposure: "available",
        useWhen: ["Separating major shifts in topic or scene."],
        avoidWhen: ["A heading or paragraph break already provides structure."],
        generation: {
          mode: "direct-html",
        },
        examples: ["<hr>"],
      },
    };
  },
});
