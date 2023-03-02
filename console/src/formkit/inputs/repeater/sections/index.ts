import { createSection } from "@formkit/inputs";

export const repeaterItems = createSection("repeaterItems", () => ({
  $cmp: "Repeater",
  props: {
    context: "$node.context",
  },
}));
