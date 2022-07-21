import type { Setting, SettingSpec } from "@halo-dev/api-client";
import type { FormKitSchemaCondition, FormKitSchemaNode } from "@formkit/core";

export interface FormKitSettingSpec extends Omit<SettingSpec, "formSchema"> {
  formSchema: FormKitSchemaCondition | FormKitSchemaNode[];
}

export interface FormKitSetting extends Omit<Setting, "spec"> {
  spec: Array<FormKitSettingSpec>;
}
