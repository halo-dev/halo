import type { FormKitTypeDefinition } from "@formkit/core";
import {
  help,
  icon,
  inner,
  label,
  message,
  messages,
  outer,
  prefix,
  suffix,
  wrapper,
} from "@formkit/inputs";
import SecretSelect from "./SecretSelect.vue";
import { SecretSection } from "./sections";

export const secret: FormKitTypeDefinition = {
  schema: outer(
    wrapper(
      label("$label"),
      inner(icon("prefix"), prefix(), SecretSection(), suffix(), icon("suffix"))
    ),
    help("$help"),
    messages(message("$message.value"))
  ),
  type: "input",
  props: ["requiredKey"],
  library: {
    SecretSelect: SecretSelect,
  },
  schemaMemoKey: "custom-secret-select",
};
