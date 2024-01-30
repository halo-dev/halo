import { createRepeaterSection } from "../repeaterSection";

const repeaterSection = createRepeaterSection();

export const addButton = repeaterSection("addButton", () => ({
  $cmp: "AddButton",
  props: {
    onClick: "$fns.createAppend()",
    disabled: "$value.length >= $max",
    context: "$node.context",
  },
  bind: "$addAttrs",
  if: "$addButton",
  type: "button",
}));

export const content = repeaterSection("content", "div");

export const controlLabel = repeaterSection("controlLabel", "span");

export const controls = repeaterSection("controls", () => ({
  $el: "ul",
  if: "$removeControl || $insertControl || $upControl || $downControl",
}));

export const down = repeaterSection("down", () => ({
  $el: "li",
  if: "$downControl",
}));

export const downControl = repeaterSection("downControl", () => ({
  $el: "button",
  attrs: {
    disabled: "$index >= $value.length - 1",
    onClick: "$fns.createShift($index, 1)",
    type: "button",
    class: `$classes.control`,
  },
}));

export const downIcon = repeaterSection("downIcon", () => ({
  $cmp: "IconArrowDownCircleLine",
}));

export const empty = repeaterSection("empty", () => ({
  $el: "div",
}));

export const fieldset = repeaterSection("fieldset", () => ({
  $el: "fieldset",
  attrs: {
    id: "$id",
    disabled: "$disabled",
  },
}));

export const group = repeaterSection("group", () => ({
  $formkit: "nativeGroup",
  index: "$index",
}));

export const insert = repeaterSection("insert", () => ({
  $el: "li",
  if: "$insertControl",
}));

export const insertControl = repeaterSection("insertControl", () => ({
  $el: "button",
  attrs: {
    disabled: "$value.length >= $max",
    onClick: "$fns.createInsert($index)",
    type: "button",
    class: `$classes.control`,
  },
}));

export const insertIcon = repeaterSection("insertIcon", () => ({
  $cmp: "IconAddCircle",
}));

export const item = repeaterSection("item", () => ({
  $el: "li",
  for: ["item", "index", "$items"],
  attrs: {
    role: "listitem",
    key: "$item",
    "data-index": "$index",
  },
}));

export const items = repeaterSection("items", () => ({
  $el: "ul",
  attrs: {
    role: "list",
  },
}));

export const remove = repeaterSection("remove", () => ({
  $el: "li",
  if: "$removeControl",
}));

export const removeControl = repeaterSection("removeControl", () => ({
  $el: "button",
  attrs: {
    disabled: "$value.length <= $min",
    onClick: "$fns.createRemover($index)",
    type: "button",
    class: `$classes.control`,
  },
}));

export const removeIcon = repeaterSection("removeIcon", () => ({
  $cmp: "IconCloseCircle",
}));

export const up = repeaterSection("up", () => ({
  $el: "li",
  if: "$upControl",
}));

export const upControl = repeaterSection("upControl", () => ({
  $el: "button",
  attrs: {
    disabled: "$index <= 0",
    onClick: "$fns.createShift($index, -1)",
    type: "button",
    class: `$classes.control`,
  },
}));

export const upIcon = repeaterSection("upIcon", () => ({
  $cmp: "IconArrowUpCircleLine",
}));
