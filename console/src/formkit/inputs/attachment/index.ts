import { initialValue } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import AttachmentInput from "./AttachmentInput.vue";

export const attachment = createInput(AttachmentInput, {
  type: "input",
  props: ["accepts"],
  forceTypeProp: "text",
  features: [initialValue],
});
