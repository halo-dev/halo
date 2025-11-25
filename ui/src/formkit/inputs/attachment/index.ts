import { initialValue, type FormKitInputs } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import AttachmentInput from "./AttachmentInput.vue";

export const attachment = createInput(AttachmentInput, {
  type: "input",
  props: ["accepts"],
  forceTypeProp: "text",
  features: [initialValue],
});

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    attachment: {
      type: "attachment";
      value?: string;
    };
  }
}
