import { initialValue, type FormKitInputs } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import AttachmentInput from "./AttachmentInput.vue";

export const attachmentInput = createInput(AttachmentInput, {
  type: "input",
  props: ["accepts"],
  forceTypeProp: "text",
  features: [initialValue],
});

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    attachmentInput: {
      type: "attachmentInput";
      value?: string;
    };
  }
}
