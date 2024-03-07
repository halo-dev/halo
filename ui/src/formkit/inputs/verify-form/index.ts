import type { FormKitTypeDefinition } from "@formkit/core";
import {
  createSection,
  disablesChildren,
  formInput,
  message,
  messages,
} from "@formkit/inputs";
import { default as forms } from "./features";
import SubmitButton from "./SubmitButton.vue";

export const actions = createSection("actions", () => ({
  $el: "div",
}));

export const submitInput = createSection("submitButton", () => ({
  $cmp: "SubmitButton",
  type: "submit",
  bind: "$submitAttrs",
  props: {
    context: "$node.context",
  },
}));

/**
 * Input definition for a form.
 * @public
 */
export const verifyForm: FormKitTypeDefinition = {
  /**
   * The actual schema of the input, or a function that returns the schema.
   */
  schema: formInput(
    "$slots.default",
    messages(message("$message.value")),
    actions(submitInput())
  ),
  /**
   * The type of node, can be a list, group, or input.
   */
  type: "group",
  /**
   * An array of extra props to accept for this input.
   */
  props: ["actions", "submitLabel", "submitAttrs", "incompleteMessage"],

  family: "verify-form",

  forceTypeProp: "form",
  /**
   * Additional features that should be added to your input
   */
  features: [forms, disablesChildren],

  library: {
    SubmitButton,
  },
};
