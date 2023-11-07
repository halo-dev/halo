import { createInput } from "@formkit/vue";
import CodeInput from "./CodeInput.vue";

export const code = createInput(CodeInput, {
  type: "input",
  props: ["height", "language"],
  forceTypeProp: "textarea",
});
