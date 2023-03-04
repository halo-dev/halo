import { createSection } from "@formkit/inputs";

export const CategorySelectSection = createSection(
  "CategorySelectSection",
  () => ({
    $cmp: "CategorySelect",
    props: {
      context: "$node.context",
    },
  })
);
