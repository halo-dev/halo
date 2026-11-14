import { Document as TiptapDocument } from "@tiptap/extension-document";

export const ExtensionDocument = TiptapDocument.extend({
  addHaloEditorMetadata() {
    return {
      ai: false,
    };
  },
});
