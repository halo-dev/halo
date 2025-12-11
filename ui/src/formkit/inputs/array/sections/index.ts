import { createRepeaterSection } from "../../repeater/repeaterSection";

const repeaterSection = createRepeaterSection();

export const arraySection = repeaterSection("arraySection", () => ({
  $cmp: "ArrayInput",
  props: {
    node: "$node",
  },
}));
