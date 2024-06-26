import { createDynamicListSection } from "../dynamicListSection";

const dynamicListSection = createDynamicListSection();

export const addButton = dynamicListSection("addButton", () => ({
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

export const content = dynamicListSection("content", () => ({
  $el: "div",
  attrs: {
    key: "$item",
    index: "$index",
    "data-index": "$index",
  },
}));

export const controlLabel = dynamicListSection("controlLabel", "span");

export const controls = dynamicListSection("controls", () => ({
  $el: "ul",
  if: "$removeControl || $insertControl || $upControl || $downControl",
}));

export const down = dynamicListSection("down", () => ({
  $el: "li",
  if: "$downControl",
}));

export const downControl = dynamicListSection("downControl", () => ({
  $el: "button",
  attrs: {
    disabled: "$index >= $value.length - 1",
    onClick: "$fns.createShift($index, 1)",
    type: "button",
    class: `$classes.control`,
  },
}));

export const downIcon = dynamicListSection("downIcon", () => ({
  $cmp: "IconArrowDownCircleLine",
}));

export const empty = dynamicListSection("empty", () => ({
  $el: "div",
}));

export const fieldset = dynamicListSection("fieldset", () => ({
  $el: "fieldset",
  attrs: {
    id: "$id",
    disabled: "$disabled",
  },
}));

export const insert = dynamicListSection("insert", () => ({
  $el: "li",
  if: "$insertControl",
}));

export const insertControl = dynamicListSection("insertControl", () => ({
  $el: "button",
  attrs: {
    disabled: "$value.length >= $max",
    onClick: "$fns.createInsert($index)",
    type: "button",
    class: `$classes.control`,
  },
}));

export const insertIcon = dynamicListSection("insertIcon", () => ({
  $cmp: "IconAddCircle",
}));

export const item = dynamicListSection("item", () => ({
  $el: "li",
  for: ["item", "index", "$items"],
  attrs: {
    role: "listitem",
    key: "$item",
    index: "$index",
    "data-index": "$index",
  },
}));

export const items = dynamicListSection("items", () => ({
  $el: "ul",
  attrs: {
    role: "list",
  },
}));

export const remove = dynamicListSection("remove", () => ({
  $el: "li",
  if: "$removeControl",
}));

export const removeControl = dynamicListSection("removeControl", () => ({
  $el: "button",
  attrs: {
    disabled: "$value.length <= $min",
    onClick: "$fns.createRemover($index)",
    type: "button",
    class: `$classes.control`,
  },
}));

export const removeIcon = dynamicListSection("removeIcon", () => ({
  $cmp: "IconCloseCircle",
}));

export const up = dynamicListSection("up", () => ({
  $el: "li",
  if: "$upControl",
}));

export const upControl = dynamicListSection("upControl", () => ({
  $el: "button",
  attrs: {
    disabled: "$index <= 0",
    onClick: "$fns.createShift($index, -1)",
    type: "button",
    class: `$classes.control`,
  },
}));

export const upIcon = dynamicListSection("upIcon", () => ({
  $cmp: "IconArrowUpCircleLine",
}));
