import { generateClasses } from "@formkit/themes";
import theme from "./theme";
import { createAutoAnimatePlugin } from "@formkit/addons";
import { zh } from "@formkit/i18n";
import type { DefaultConfigOptions } from "@formkit/vue";
import { form } from "./inputs/form";
import { menuCheckbox } from "./inputs/menu-checkbox";
import { menuRadio } from "./inputs/menu-radio";
import { menuItemSelect } from "./inputs/menu-item-select";
import { postSelect } from "./inputs/post-select";
import { singlePageSelect } from "./inputs/singlePage-select";
import { categorySelect } from "./inputs/category-select";
import { tagSelect } from "./inputs/tag-select";
import { categoryCheckbox } from "./inputs/category-checkbox";
import { tagCheckbox } from "./inputs/tag-checkbox";

const config: DefaultConfigOptions = {
  config: {
    classes: generateClasses(theme),
  },
  plugins: [createAutoAnimatePlugin()],
  inputs: {
    form,
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
