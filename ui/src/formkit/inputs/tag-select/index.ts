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
  type FormKitInputs,
} from "@formkit/inputs";
import { defineAsyncComponent } from "vue";
import { TagSelectSection } from "./sections";

export const tagSelect: FormKitTypeDefinition = {
  schema: outer(
    wrapper(
      label("$label"),
      inner(
        icon("prefix"),
        prefix(),
        TagSelectSection(),
        suffix(),
        icon("suffix")
      )
    ),
    help("$help"),
    messages(message("$message.value"))
  ),
  type: "input",
  props: ["multiple"],
  library: {
    TagSelect: defineAsyncComponent(() => import("./TagSelect.vue")),
  },
  schemaMemoKey: "custom-tag-select",
};

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    tagSelect: {
      type: "tagSelect";
      value?: string | string[];
    };
  }
}
