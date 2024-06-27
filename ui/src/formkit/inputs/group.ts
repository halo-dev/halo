import type { FormKitTypeDefinition } from "@formkit/core";

import {
  disablesChildren,
  fieldset,
  help,
  inner,
  legend,
  message,
  messages,
  outer,
} from "@formkit/inputs";

export const group: FormKitTypeDefinition = {
  schema: outer(
    fieldset(
      legend("$label"),
      help("$help"),
      inner("$slots.default"),
      messages(message("$message.value"))
    )
  ),

  type: "group",

  props: [],

  features: [disablesChildren],
};
