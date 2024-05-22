// core libs
// types
import { computed, watch, type ComputedRef, type Ref } from "vue";
import { ref } from "vue";

// libs
import { cloneDeep } from "lodash-es";
import type { ConfigMap, Setting, SettingForm } from "@halo-dev/api-client";
import type { FormKitSchemaCondition, FormKitSchemaNode } from "@formkit/core";

interface useSettingFormConvertReturn {
  formSchema: ComputedRef<
    (FormKitSchemaCondition | FormKitSchemaNode)[] | undefined
  >;
  configMapFormData: Ref<Record<string, Record<string, string>>>;
  convertToSave: () => ConfigMap | undefined;
}

export function useSettingFormConvert(
  setting: Ref<Setting | undefined>,
  configMap: Ref<ConfigMap | undefined>,
  group: Ref<string>
): useSettingFormConvertReturn {
  const configMapFormData = ref<Record<string, Record<string, string>>>({});

  const formSchema = computed(() => {
    if (!setting.value) {
      return;
    }
    const { forms } = setting.value.spec;
    return forms.find((item) => item.group === group?.value)?.formSchema as (
      | FormKitSchemaCondition
      | FormKitSchemaNode
    )[];
  });

  watch(
    () => configMap.value,
    () => {
      const { forms } = setting.value?.spec || {};

      forms?.forEach((form) => {
        configMapFormData.value[form.group] = JSON.parse(
          configMap.value?.data?.[form.group] || "{}"
        );
      });

      Object.keys(configMap.value?.data || {}).forEach((key) => {
        if (!forms?.find((item) => item.group === key)) {
          configMapFormData.value[key] = JSON.parse(
            configMap.value?.data?.[key] || "{}"
          );
        }
      });
    },
    {
      immediate: true,
    }
  );

  function convertToSave() {
    const configMapToUpdate = cloneDeep(configMap.value);

    if (!configMapToUpdate) {
      return;
    }

    const data: {
      [key: string]: string;
    } = {};

    const { forms } = setting.value?.spec || {};

    forms?.forEach((item: SettingForm) => {
      data[item.group] = JSON.stringify(configMapFormData?.value?.[item.group]);
    });

    Object.keys(configMap.value?.data || {}).forEach((key) => {
      if (!forms?.find((item) => item.group === key)) {
        data[key] = configMap.value?.data?.[key] || "{}";
      }
    });

    configMapToUpdate.data = data;
    return configMapToUpdate;
  }

  return {
    formSchema,
    configMapFormData,
    convertToSave,
  };
}
