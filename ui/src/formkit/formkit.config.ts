import { createAutoHeightTextareaPlugin } from "@formkit/addons";
import { en, zh } from "@formkit/i18n";
import { group as nativeGroup, select as nativeSelect } from "@formkit/inputs";
import { generateClasses } from "@formkit/themes";
import type { DefaultConfigOptions } from "@formkit/vue";
import { array } from "./inputs/array";
import { attachment } from "./inputs/attachment";
import { attachmentGroupSelect } from "./inputs/attachment-group-select";
import { attachmentInput } from "./inputs/attachment-input";
import { attachmentPolicySelect } from "./inputs/attachment-policy-select";
import { categoryCheckbox } from "./inputs/category-checkbox";
import { categorySelect } from "./inputs/category-select";
import { code } from "./inputs/code";
import { color } from "./inputs/color";
import { form } from "./inputs/form";
import { group } from "./inputs/group";
import { iconify } from "./inputs/iconify";
import { list } from "./inputs/list";
import { menuCheckbox } from "./inputs/menu-checkbox";
import { menuItemSelect } from "./inputs/menu-item-select";
import { menuRadio } from "./inputs/menu-radio";
import { menuSelect } from "./inputs/menu-select";
import { password } from "./inputs/password";
import { postSelect } from "./inputs/post-select";
import { repeater } from "./inputs/repeater";
import { roleSelect } from "./inputs/role-select";
import { secret } from "./inputs/secret";
import { select } from "./inputs/select";
import { singlePageSelect } from "./inputs/singlePage-select";
import { tagCheckbox } from "./inputs/tag-checkbox";
import { tagSelect } from "./inputs/tag-select";
import { toggle } from "./inputs/toggle";
import { userSelect } from "./inputs/user-select";
import { verificationForm } from "./inputs/verify-form";
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
  inputs: {
    attachmentInput,
    attachmentGroupSelect,
    attachmentPolicySelect,
    categoryCheckbox,
    categorySelect,
    code,
    form,
    group,
    list,
    menuCheckbox,
    menuItemSelect,
    menuRadio,
    menuSelect,
    nativeGroup,
    password,
    postSelect,
    repeater,
    roleSelect,
    secret,
    singlePageSelect,
    tagCheckbox,
    tagSelect,
    verificationForm,
    userSelect,
    nativeSelect,
    select,
    array,
    color,
    iconify,
    attachment,
    toggle,
  },
  locales: { zh, en },
  locale: "zh",
};

export default config;
