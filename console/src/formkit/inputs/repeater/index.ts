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
  suffix,
} from "@formkit/inputs";
import { repeaterItems } from "./sections";
import Repeater from "./Repeater.vue";

export const repeater: FormKitTypeDefinition = {
  schema: outer(
    fieldset(
      legend("$label"),
      help("$help"),
      inner(prefix(), repeaterItems("$slots.default"), suffix())
    ),
    messages(message("$message.value"))
  ),
  type: "list",
  library: {
    Repeater: Repeater,
  },
};
