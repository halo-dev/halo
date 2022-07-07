import { generateClasses } from "@formkit/themes";
import theme from "./theme";
import { createAutoAnimatePlugin } from "@formkit/addons";
import { zh } from "@formkit/i18n";
import type { DefaultConfigOptions } from "@formkit/vue";
import { form } from "./inputs/form";

const config: DefaultConfigOptions = {
  config: {
    classes: generateClasses(theme),
  },
  plugins: [createAutoAnimatePlugin()],
  inputs: {
    form,
  },
  locales: { zh },
  locale: "zh",
};

export default config;
