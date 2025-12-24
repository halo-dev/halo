import type { FormKitTypeDefinition } from "@formkit/core";
import {
  $extend,
  boxInner,
  boxLabel,
  boxWrapper,
  createSection,
  help,
  message,
  messages,
  outer,
  type FormKitInputs,
} from "@formkit/inputs";
import { defineAsyncComponent } from "vue";
import switchFeature from "./feature";

const cmpName = "SwitchInput";

const switchSection = createSection("input", () => ({
  $cmp: cmpName,
  props: {
    context: "$node.context",
  },
}));

/**
 * Input definition for a switch input.
 * @public
 */
export const switchInput: FormKitTypeDefinition = {
  schema: outer(
    boxWrapper(
      boxInner(switchSection()),
      $extend(boxLabel("$label"), {
        if: "$label",
      })
    ),
    help("$help"),
    messages(message("$message.value"))
  ),
  /**
   * The type of node, can be a list, group, or input.
   */
  type: "input",
  /**
   * The family of inputs this one belongs too. For example "text" and "email"
   * are both part of the "text" family. This is primary used for styling.
   */
  family: "box",

  props: ["onValue", "offValue", "disabled", "type"],

  features: [switchFeature],

  library: {
    SwitchInput: defineAsyncComponent(() => import("./SwitchInput.vue")),
  },
  /**
   * The key used to memoize the schema.
   */
  schemaMemoKey: "custom-switch-input",
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    switch: {
      type: "switch";
      onValue?: unknown;
      offValue?: unknown;
      disabled?: boolean;
      size?: "xs" | "sm" | "md" | "lg";
      state: "default" | "success" | "error";
    };
  }
}
