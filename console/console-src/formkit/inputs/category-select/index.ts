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
import CategorySelect from "./CategorySelect.vue";
import { CategorySelectSection } from "./sections";
import type { FormKitInputs } from "@formkit/inputs";

declare module "@formkit/inputs" {
  interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    categorySelect: {
      type: "categorySelect";
      value?: string | string[];
    };
  }
}

export const categorySelect: FormKitTypeDefinition = {
  schema: outer(
    wrapper(
      label("$label"),
      inner(
        icon("prefix"),
        prefix(),
        CategorySelectSection(),
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
    CategorySelect: CategorySelect,
  },
  schemaMemoKey: "custom-category-select",
};
