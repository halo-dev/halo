import { createRepeaterSection } from "../repeaterSection";

const repeaterSection = createRepeaterSection();

export const arraySection = repeaterSection("arraySection", () => ({
  $cmp: "ArrayInput",
  props: {
    node: "$node",
  },
}));
