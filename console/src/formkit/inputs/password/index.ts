import type { FormKitTypeDefinition } from "@formkit/core";
import {
  outer,
  inner,
  wrapper,
  label,
  help,
  messages,
  message,
  icon,
  prefix,
  suffix,
  textInput,
} from "@formkit/inputs";
import RevealButton from "./RevealButton.vue";

import { createSection } from "@formkit/inputs";

export const RevealButtonSuffix = createSection("RevealButtonSuffix", () => ({
  $cmp: "RevealButton",
  props: {
    context: "$node.context",
  },
}));

/**
 * Input definition for a text.
 * @public
 */
export const password: FormKitTypeDefinition = {
  /**
   * The actual schema of the input, or a function that returns the schema.
   */
  schema: outer(
    wrapper(
      label("$label"),
      inner(
        icon("prefix", "label"),
        prefix(),
        textInput(),
        suffix(),
        RevealButtonSuffix()
      )
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
  family: "text",
  /**
   * An array of extra props to accept for this input.
   */
  props: [],
  /**
   * Forces node.props.type to be this explicit value.
   */
  forceTypeProp: "password",
  /**
   * Additional features that should be added to your input
   */
  features: [],
  /**
   * The key used to memoize the schema.
   */
  schemaMemoKey: "92o49lnph2p",
  library: {
    RevealButton,
  },
};
