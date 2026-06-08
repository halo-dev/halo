import type { FormKitLibrary } from "@formkit/core";
import { group as nativeGroup, select as nativeSelect } from "@formkit/inputs";
import { array } from "./array";
import { attachment } from "./attachment";
import { attachmentGroupSelect } from "./attachment-group-select";
import { attachmentInput } from "./attachment-input";
import { attachmentPolicySelect } from "./attachment-policy-select";
import { categoryCheckbox } from "./category-checkbox";
import { categorySelect } from "./category-select";
import { code } from "./code";
import { color } from "./color";
import { form } from "./form";
import { group } from "./group";
import { iconify } from "./iconify";
import { list } from "./list";
import { menuCheckbox } from "./menu-checkbox";
import { menuItemSelect } from "./menu-item-select";
import { menuRadio } from "./menu-radio";
import { menuSelect } from "./menu-select";
import { password } from "./password";
import { postSelect } from "./post-select";
import { repeater } from "./repeater";
import { roleSelect } from "./role-select";
import { secret } from "./secret";
import { select } from "./select";
import { singlePageSelect } from "./singlePage-select";
import { switchInput } from "./switch";
import { tagCheckbox } from "./tag-checkbox";
import { tagSelect } from "./tag-select";
import { toggle } from "./toggle";
import { userSelect } from "./user-select";
import { verificationForm } from "./verify-form";

export const builtinFormKitInputs: FormKitLibrary = {
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
  switch: switchInput,
  toggle,
};
