import type { FormKitTypeDefinition } from "@formkit/core";
import {
  disablesChildren,
  formInput,
  forms,
  message,
  messages,
} from "@formkit/inputs";

/**
 * Input definition for a form.
 * @public
 */
export const form: FormKitTypeDefinition = {
  /**
   * The actual schema of the input, or a function that returns the schema.
   */
  schema: formInput("$slots.default", messages(message("$message.value"))),
  /**
   * The type of node, can be a list, group, or input.
   */
  type: "group",
  /**
   * An array of extra props to accept for this input.
   */
  props: [
    "actions",
    "submit",
    "submitLabel",
    "submitAttrs",
    "submitBehavior",
    "incompleteMessage",
  ],
  forceTypeProp: "form",
  /**
   * Additional features that should be added to your input
   */
  features: [forms, disablesChildren],
};
