import { createSection } from "@formkit/inputs";

export const SelectSection = createSection("SelectMain", () => ({
  $cmp: "SelectMain",
  props: {
    context: "$node.context",
  },
}));
