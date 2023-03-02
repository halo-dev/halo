import { generateClasses } from "@formkit/themes";
import theme from "./theme";
import { zh } from "@formkit/i18n";
import type { DefaultConfigOptions } from "@formkit/vue";
import { form } from "./inputs/form";
import { attachment } from "./inputs/attachment";
import { code } from "./inputs/code";
import { repeater } from "./inputs/repeater";
import { menuCheckbox } from "./inputs/menu-checkbox";
import { menuRadio } from "./inputs/menu-radio";
import { menuItemSelect } from "./inputs/menu-item-select";
import { postSelect } from "./inputs/post-select";
import { singlePageSelect } from "./inputs/singlePage-select";
import { tagSelect } from "./inputs/tag-select";
import { categorySelect } from "./inputs/category-select";
import { categoryCheckbox } from "./inputs/category-checkbox";
import { tagCheckbox } from "./inputs/tag-checkbox";

import radioAlt from "./plugins/radio-alt";
import stopImplicitSubmission from "./plugins/stop-implicit-submission";

const config: DefaultConfigOptions = {
  config: {
    classes: generateClasses(theme),
  },
  plugins: [radioAlt, stopImplicitSubmission],
  inputs: {
    form,
    attachment,
    code,
    repeater,
    menuCheckbox,
    menuRadio,
    menuItemSelect,
    postSelect,
    categorySelect,
    tagSelect,
    singlePageSelect,
    categoryCheckbox,
    tagCheckbox,
  },
  locales: { zh },
  locale: "zh",
};

export default config;
