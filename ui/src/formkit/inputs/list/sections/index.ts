import { createListSection } from "../listSection";

const listSection = createListSection();

export const addButton = listSection("addButton", () => ({
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

export const content = listSection("content", () => ({
  $el: "div",
  attrs: {
    key: "$item",
    index: "$index",
    "data-index": "$index",
  },
}));

export const controlLabel = listSection("controlLabel", "span");

export const controls = listSection("controls", () => ({
  $el: "ul",
  if: "$removeControl || $insertControl || $upControl || $downControl",
}));

export const down = listSection("down", () => ({
  $el: "li",
  if: "$downControl",
}));

export const downControl = listSection("downControl", () => ({
  $el: "button",
  attrs: {
    disabled: "$index >= $value.length - 1",
    onClick: "$fns.createShift($index, 1)",
    type: "button",
    class: `$classes.control`,
  },
}));

export const downIcon = listSection("downIcon", () => ({
  $cmp: "IconArrowDownCircleLine",
}));

export const empty = listSection("empty", () => ({
  $el: "div",
}));

export const fieldset = listSection("fieldset", () => ({
  $el: "fieldset",
  attrs: {
    id: "$id",
    disabled: "$disabled",
  },
}));

export const insert = listSection("insert", () => ({
  $el: "li",
  if: "$insertControl",
}));

export const insertControl = listSection("insertControl", () => ({
  $el: "button",
  attrs: {
    disabled: "$value.length >= $max",
    onClick: "$fns.createInsert($index)",
    type: "button",
    class: `$classes.control`,
  },
}));

export const insertIcon = listSection("insertIcon", () => ({
  $cmp: "IconAddCircle",
}));

export const item = listSection("item", () => ({
  $el: "li",
  for: ["item", "index", "$items"],
  attrs: {
    role: "listitem",
    key: "$item",
    index: "$index",
    "data-index": "$index",
  },
}));

export const items = listSection("items", () => ({
  $el: "ul",
  attrs: {
    role: "list",
  },
}));

export const remove = listSection("remove", () => ({
  $el: "li",
  if: "$removeControl",
}));

export const removeControl = listSection("removeControl", () => ({
  $el: "button",
  attrs: {
    disabled: "$value.length <= $min",
    onClick: "$fns.createRemover($index)",
    type: "button",
    class: `$classes.control`,
  },
}));

export const removeIcon = listSection("removeIcon", () => ({
  $cmp: "IconCloseCircle",
}));

export const up = listSection("up", () => ({
  $el: "li",
  if: "$upControl",
}));

export const upControl = listSection("upControl", () => ({
  $el: "button",
  attrs: {
    disabled: "$index <= 0",
    onClick: "$fns.createShift($index, -1)",
    type: "button",
    class: `$classes.control`,
  },
}));

export const upIcon = listSection("upIcon", () => ({
  $cmp: "IconArrowUpCircleLine",
}));
