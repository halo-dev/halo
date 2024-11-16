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
import SelectMain from "./SelectMain.vue";
import { SelectSection } from "./sections/index";

/**
 * Custom Select component.
 *
 * features:
 *
 * 1. Supports both single and multiple selection, controlled by the `multiple` prop. The display format of the input differs between single and multiple selection modes.
 * 2. Supports passing in an `options` prop to render dropdown options, or using the `action` prop to pass a function for dynamically retrieving options. The handling differs based on the method:
 *    a. If `options` is passed, it will be used directly to render the dropdown options.
 *    b. If the `action` prop is provided, it should be used to fetch options from an API, and additional features like pagination and search may also be enabled.
 * 3. Supports sorting functionality. If sorting is enabled, the list allows drag-and-drop sorting, and the current position of the node can be displayed in the dropdown.
 * 4. Supports an add feature. If the target content is not in the list, it allows adding the currently entered content as a `value`.
 * 5. Allows restricting the maximum number of selections, controlled by the `maxCount` prop.
 */
export const select: FormKitTypeDefinition = {
  schema: outer(
    wrapper(
      label("$label"),
      inner(icon("prefix"), prefix(), SelectSection(), suffix(), icon("suffix"))
    ),
    help("$help"),
    messages(message("$message.value"))
  ),

  type: "input",

  props: [
    "clearable",
    "multiple",
    "maxCount",
    "sortable",
    "action",
    "requestOption",
    "placeholder",
    "remote",
    "remoteOption",
    "allowCreate",
    "remoteOptimize",
    "searchable",
    "autoSelect",
  ],

  library: {
    SelectMain: SelectMain,
  },

  schemaMemoKey: "custom-select",
};
