import type { FormKitTypeDefinition } from "@formkit/core";

import { i18n } from "@/locales";
import {
  $if,
  disablesChildren,
  fieldset,
  help,
  inner,
  legend,
  message,
  messages,
  outer,
  prefix,
  renamesRadios,
  suffix,
} from "@formkit/inputs";
import {
  IconAddCircle,
  IconArrowDownCircleLine,
  IconArrowUpCircleLine,
  IconCloseCircle,
} from "@halo-dev/components";
import AddButton from "./AddButton.vue";
import { lists } from "./features/lists";
import {
  addButton,
  content,
  controls,
  down,
  downControl,
  downIcon,
  empty,
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

/**
 * Input definition for a dynamic list input.
 * @public
 */
export const list: FormKitTypeDefinition = {
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
                content("$slots.default"),
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
        suffix(),
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
    "itemType",
  ],
  /**
   * Additional features that should be added to your input
   */
  features: [lists, disablesChildren, renamesRadios],

  library: {
    IconAddCircle,
    IconCloseCircle,
    IconArrowUpCircleLine,
    IconArrowDownCircleLine,
    AddButton,
  },
};
