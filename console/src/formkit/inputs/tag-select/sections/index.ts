import { createSection } from "@formkit/inputs";

export const TagSelectSection = createSection("TagSelectSection", () => ({
  $cmp: "TagSelect",
  props: {
    context: "$node.context",
  },
}));
