import { generateClasses } from "@formkit/themes";
import theme from "./theme";
import type { FormKitOptions } from "@formkit/core";

const config: FormKitOptions = {
  config: {
    classes: generateClasses(theme),
  },
};

export default config;
