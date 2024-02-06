import type { FormKitTypeDefinition } from "@formkit/core";

import {
  disablesChildren,
  outer,
  fieldset,
  legend,
  help,
  inner,
  message,
  messages,
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
