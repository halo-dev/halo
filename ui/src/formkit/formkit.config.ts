import { createAutoHeightTextareaPlugin } from "@formkit/addons";
import { en, zh } from "@formkit/i18n";
import { generateClasses } from "@formkit/themes";
import type { DefaultConfigOptions } from "@formkit/vue";
import { builtinFormKitInputs } from "./inputs";
import autoScrollToErrors from "./plugins/auto-scroll-to-errors";
import passwordPreventAutocomplete from "./plugins/password-prevent-autocomplete";
import radioAlt from "./plugins/radio-alt";
import requiredAsterisk from "./plugins/required-asterisk";
import stopImplicitSubmission from "./plugins/stop-implicit-submission";
import theme from "./theme";

const config: DefaultConfigOptions = {
  config: {
    classes: generateClasses(theme),
  },
  plugins: [
    radioAlt,
    stopImplicitSubmission,
    passwordPreventAutocomplete,
    requiredAsterisk,
    autoScrollToErrors,
    createAutoHeightTextareaPlugin(),
  ],
  inputs: builtinFormKitInputs,
  locales: { zh, en },
  locale: "zh",
};

export default config;
