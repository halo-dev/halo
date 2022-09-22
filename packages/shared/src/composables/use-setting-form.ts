// core libs
// types
import type { Ref } from "vue";
import { ref } from "vue";
import { apiClient } from "../utils/api-client";

// libs
import cloneDeep from "lodash.clonedeep";
import merge from "lodash.merge";
import type { ConfigMap, Setting, SettingForm } from "@halo-dev/api-client";
import type { FormKitSchemaCondition, FormKitSchemaNode } from "@formkit/core";

const initialConfigMap: ConfigMap = {
  apiVersion: "v1alpha1",
  kind: "ConfigMap",
  metadata: {
    name: "",
  },
  data: {},
};

interface useSettingFormReturn {
  setting: Ref<Setting | undefined>;
  configMap: Ref<ConfigMap>;
  configMapFormData: Ref<Record<string, Record<string, string>> | undefined>;
  saving: Ref<boolean>;
  handleFetchSettings: () => void;
  handleFetchConfigMap: () => void;
  handleSaveConfigMap: () => void;
  handleReset: () => void;
}

export function useSettingForm(
  settingName: Ref<string | undefined>,
  configMapName: Ref<string | undefined>
): useSettingFormReturn {
  const setting = ref<Setting>();
  const configMap = ref<ConfigMap>(cloneDeep(initialConfigMap));
  const configMapFormData = ref<
    Record<string, Record<string, string>> | undefined
  >();

  const saving = ref(false);

  const handleFetchSettings = async () => {
    if (!settingName.value) {
      setting.value = undefined;
      return;
    }
    try {
      const { data } = await apiClient.extension.setting.getv1alpha1Setting({
        name: settingName.value,
      });
      setting.value = data;

      // init configMapFormData
      if (!configMapFormData.value) {
        const { forms } = setting.value.spec;
        const initialConfigMapFormData: Record<
          string,
          Record<string, string>
        > = {};
        forms.forEach((form) => {
          initialConfigMapFormData[form.group] = {};
          const formSchema = form.formSchema as (
            | FormKitSchemaCondition
            | FormKitSchemaNode
          )[];
          formSchema.forEach((schema) => {
            // @ts-ignore
            if ("name" in schema && "$formkit" in schema) {
              initialConfigMapFormData[form.group][schema.name] =
                schema.value || undefined;
            }
          });
        });
        configMapFormData.value = cloneDeep(initialConfigMapFormData);
      }
    } catch (e) {
      console.error("Failed to fetch setting", e);
    }
  };

  const handleFetchConfigMap = async () => {
    if (!configMapName.value) {
      configMap.value = cloneDeep(initialConfigMap);
      configMapFormData.value = undefined;
      return;
    }
    try {
      const response = await apiClient.extension.configMap.getv1alpha1ConfigMap(
        {
          name: configMapName.value,
        }
      );

      configMap.value = response.data;

      const { data } = configMap.value;

      if (data) {
        // merge objects value
        const { forms } = setting.value?.spec || {};

        forms?.forEach((form) => {
          if (!configMapFormData.value) {
            return;
          }
          configMapFormData.value[form.group] = merge(
            configMapFormData.value[form.group] || {},
            JSON.parse(data[form.group] || "{}")
          );
        });
      }
    } catch (e) {
      console.error("Failed to fetch configMap", e);
    }
  };

  const handleSaveConfigMap = async () => {
    try {
      saving.value = true;

      if (!configMap.value.metadata.name && configMapName.value) {
        configMap.value.metadata.name = configMapName.value;
      }

      setting.value?.spec.forms.forEach((item: SettingForm) => {
        // @ts-ignore
        configMap.value.data[item.group] = JSON.stringify(
          configMapFormData?.value?.[item.group]
        );
      });

      if (!configMap.value.metadata.creationTimestamp) {
        await apiClient.extension.configMap.createv1alpha1ConfigMap({
          configMap: configMap.value,
        });
      } else {
        await apiClient.extension.configMap.updatev1alpha1ConfigMap({
          configMap: configMap.value,
          name: configMap.value.metadata.name,
        });
      }
    } catch (e) {
      console.error("Failed to save configMap", e);
    } finally {
      await handleFetchSettings();
      await handleFetchConfigMap();
      saving.value = false;
    }
  };

  const handleReset = () => {
    setting.value = undefined;
    configMap.value = cloneDeep(initialConfigMap);
    configMapFormData.value = undefined;
  };

  return {
    setting,
    configMap,
    configMapFormData,
    saving,
    handleFetchSettings,
    handleFetchConfigMap,
    handleSaveConfigMap,
    handleReset,
  };
}
