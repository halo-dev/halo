import type { FormKitTypeDefinition } from "@formkit/core";

import {
  outer,
  fieldset,
  legend,
  help,
  inner,
  prefix,
  $if,
  suffix,
  messages,
  message,
} from "@formkit/inputs";
import { repeats } from "./features/repeats";
import {
  addButton,
  content,
  controls,
  down,
  downControl,
  downIcon,
  empty,
  group,
  insert,
  insertControl,
  insertIcon,
  item,
  items,
  remove,
  removeControl,
  removeIcon,
  up,
  upControl,
  upIcon,
} from "./sections";
import {
  IconAddCircle,
  IconCloseCircle,
  IconArrowUpCircleLine,
  IconArrowDownCircleLine,
} from "@halo-dev/components";
import AddButton from "./AddButton.vue";
import { i18n } from "@/locales";

/**
 * Input definition for a repeater input.
 * @public
 */
export const repeater: FormKitTypeDefinition = {
  /**
   * The actual schema of the input, or a function that returns the schema.
   */
  schema: outer(
    fieldset(
      legend("$label"),
      help("$help"),

      inner(
        prefix(),
        $if(
          "$value.length === 0",
          $if("$slots.empty", empty()),
          $if(
            "$slots.default",
            items(
              item(
                content(group("$slots.default")),
                controls(
                  up(upControl(upIcon())),
                  remove(removeControl(removeIcon())),
                  insert(insertControl(insertIcon())),
                  down(downControl(downIcon()))
                )
              )
            ),
            suffix()
          )
        ),
        addButton(`$addLabel || (${i18n.global.t("core.common.buttons.add")})`)
      )
    ),
    messages(message("$message.value"))
  ),
  /**
   * The type of node, can be a list, group, or input.
   */
  type: "list",
  /**
   * An array of extra props to accept for this input.
   */
  props: [
    "min",
    "max",
    "upControl",
    "downControl",
    "removeControl",
    "insertControl",
    "addLabel",
    "addButton",
  ],
  /**
   * Additional features that make this input work.
   */
  features: [repeats],
  library: {
    IconAddCircle,
    IconCloseCircle,
    IconArrowUpCircleLine,
    IconArrowDownCircleLine,
    AddButton,
  },
};
