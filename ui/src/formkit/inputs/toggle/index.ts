import type { FormKitTypeDefinition } from "@formkit/core";
import {
  createSection,
  help,
  label,
  message,
  messages,
  outer,
  wrapper,
  type FormKitInputs,
} from "@formkit/inputs";
import { defineAsyncComponent } from "vue";
import toggleFeature from "./feature";

const cmpName = "ToggleInput";

export const toggleSection = createSection("toggleSection", () => ({
  $cmp: cmpName,
  props: {
    context: "$node.context",
  },
}));

/**
 * Input definition for a toggle input.
 * @public
 */
export const toggle: FormKitTypeDefinition = {
  /**
   * The actual schema of the input, or a function that returns the schema.
   */
  schema: outer(
    wrapper(label("$label"), help("$help"), toggleSection()),
    messages(message("$message.value"))
  ),

  /**
   * The type of node, can be a list, group, or input.
   */
  type: "input",
  /**
   * An array of extra props to accept for this input.
   */
  props: ["options", "multiple", "renderType", "size", "gap"],

  features: [toggleFeature],

  /**
   * Additional features that make this input work.
   */
  library: {
    ToggleInput: defineAsyncComponent(() => import("./ToggleInput.vue")),
  },
  schemaMemoKey: "custom-toggle",
};

export type ToggleValue = string | number | boolean;

export type ToggleOption = {
  render?: string;
  label?: string;
  value: string | number | boolean;
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    toggle: {
      type: "toggle";
      options: ToggleOption[];
      value?: ToggleValue | ToggleValue[];
      multiple?: boolean;
      renderType?: "text" | "image" | "color";
      size?: number;
      gap?: number;
    };
  }
}
