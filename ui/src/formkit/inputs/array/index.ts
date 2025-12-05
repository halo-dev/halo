import type { FormKitTypeDefinition } from "@formkit/core";

import {
  fieldset,
  help,
  inner,
  legend,
  message,
  messages,
  outer,
  prefix,
  type FormKitInputs,
} from "@formkit/inputs";
import ArrayInput from "./ArrayInput.vue";
import { arraySection } from "./sections/index";
/**
 * Input definition for a array input.
 * @public
 */
export const array: FormKitTypeDefinition = {
  /**
   * The actual schema of the input, or a function that returns the schema.
   */
  schema: outer(
    fieldset(legend("$label"), help("$help"), inner(prefix(), arraySection())),
    messages(message("$message.value"))
  ),
  /**
   * The type of node, can be a list, group, or input.
   */
  type: "list",
  /**
   * An array of extra props to accept for this input.
   */
  props: [
    "min",
    "max",
    "upControl",
    "downControl",
    "removeControl",
    "insertControl",
    "addLabel",
    "addButton",
    "itemLabel",
  ],
  /**
   * Additional features that make this input work.
   */
  library: {
    ArrayInput,
  },
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    array: {
      type: "array";
      value?: Record<string, unknown>[];
    };
  }
}
