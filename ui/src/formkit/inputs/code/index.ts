import type { FormKitInputs } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import CodeInput from "./CodeInput.vue";

export const code = createInput(CodeInput, {
  type: "input",
  props: ["height", "language"],
  forceTypeProp: "textarea",
});

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    code: {
      type: "code";
      value?: string;
    };
  }
}
