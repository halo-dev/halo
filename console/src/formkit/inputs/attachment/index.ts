import { initialValue } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import AttachmentInput from "./AttachmentInput.vue";

import type { FormKitInputs } from "@formkit/inputs";

declare module "@formkit/inputs" {
  interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    attachment: {
      type: "attachment";
      value?: string;
    };
  }
}

export const attachment = createInput(AttachmentInput, {
  type: "input",
  props: ["accepts"],
  forceTypeProp: "text",
  features: [initialValue],
});
