import type { FormKitTypeDefinition } from "@formkit/core";
import {
  createSection,
  disablesChildren,
  message,
  messages,
} from "@formkit/inputs";
import { default as verifyFeature } from "./features";
import VerificationButton from "./VerificationButton.vue";

export const verifyInput = createSection("verificationForm", () => ({
  $el: "div",
  bind: "$attrs",
  attrs: {
    id: "$id",
    name: "$node.name",
    "data-loading": "$state.loading || undefined",
  },
}));

export const actions = createSection("actions", () => ({
  $el: "div",
}));

export const verificationButton = createSection("verificationButton", () => ({
  $cmp: "VerificationButton",
  type: "button",
  bind: "$buttonAttrs",
  props: {
    context: "$node.context",
  },
}));

/**
 * Input definition for a form.
 * @public
 */
export const verificationForm: FormKitTypeDefinition = {
  /**
   * The actual schema of the input, or a function that returns the schema.
   */
  schema: verifyInput(
    "$slots.default",
    messages(message("$message.value")),
    actions(verificationButton())
  ),
  /**
   * The type of node, can be a list, group, or input.
   */
  type: "group",
  /**
   * An array of extra props to accept for this input.
   */
  props: ["action", "label", "buttonAttrs"],

  /**
   * Additional features that should be added to your input
   */
  features: [verifyFeature, disablesChildren],

  library: {
    VerificationButton,
  },
};
