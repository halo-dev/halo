import { createSection } from "@formkit/inputs";

export const SecretSection = createSection("SecretSection", () => ({
  $cmp: "SecretSelect",
  props: {
    context: "$node.context",
  },
}));
