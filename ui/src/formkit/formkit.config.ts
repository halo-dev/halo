import { generateClasses } from "@formkit/themes";
import theme from "./theme";
import { zh, en } from "@formkit/i18n";
import type { DefaultConfigOptions } from "@formkit/vue";
import { form } from "./inputs/form";
import { group } from "./inputs/group";
import { group as nativeGroup } from "@formkit/inputs";
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
import { roleSelect } from "./inputs/role-select";
import { attachmentPolicySelect } from "./inputs/attachment-policy-select";
import { attachmentGroupSelect } from "./inputs/attachment-group-select";
import { password } from "./inputs/password";
import { verificationForm } from "./inputs/verify-form";

import radioAlt from "./plugins/radio-alt";
import stopImplicitSubmission from "./plugins/stop-implicit-submission";
import passwordPreventAutocomplete from "./plugins/password-prevent-autocomplete";
import requiredAsterisk from "./plugins/required-asterisk";
import autoScrollToErrors from "./plugins/auto-scroll-to-errors";

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
