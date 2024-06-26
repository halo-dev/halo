import { en, zh } from "@formkit/i18n";
import { group as nativeGroup } from "@formkit/inputs";
import { generateClasses } from "@formkit/themes";
import type { DefaultConfigOptions } from "@formkit/vue";
import { attachment } from "./inputs/attachment";
import { attachmentGroupSelect } from "./inputs/attachment-group-select";
import { attachmentPolicySelect } from "./inputs/attachment-policy-select";
import { categoryCheckbox } from "./inputs/category-checkbox";
import { categorySelect } from "./inputs/category-select";
import { code } from "./inputs/code";
import { form } from "./inputs/form";
import { group } from "./inputs/group";
import { list } from "./inputs/list";
import { menuCheckbox } from "./inputs/menu-checkbox";
import { menuItemSelect } from "./inputs/menu-item-select";
import { menuRadio } from "./inputs/menu-radio";
import { password } from "./inputs/password";
import { postSelect } from "./inputs/post-select";
import { repeater } from "./inputs/repeater";
import { roleSelect } from "./inputs/role-select";
import { singlePageSelect } from "./inputs/singlePage-select";
import { tagCheckbox } from "./inputs/tag-checkbox";
import { tagSelect } from "./inputs/tag-select";
import { verificationForm } from "./inputs/verify-form";
import theme from "./theme";

import autoScrollToErrors from "./plugins/auto-scroll-to-errors";
import passwordPreventAutocomplete from "./plugins/password-prevent-autocomplete";
import radioAlt from "./plugins/radio-alt";
import requiredAsterisk from "./plugins/required-asterisk";
import stopImplicitSubmission from "./plugins/stop-implicit-submission";

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
  ],
  inputs: {
    list,
    form,
    password,
    group,
    nativeGroup,
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
    roleSelect,
    attachmentPolicySelect,
    attachmentGroupSelect,
    verificationForm,
  },
  locales: { zh, en },
  locale: "zh",
};

export default config;
