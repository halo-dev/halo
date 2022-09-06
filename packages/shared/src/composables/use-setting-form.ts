// core libs
// types
import type { Ref } from "vue";
import { ref } from "vue";
import { apiClient } from "../utils/api-client";

// libs
import cloneDeep from "lodash.clonedeep";
import merge from "lodash.merge";
import type { FormKitSetting, FormKitSettingSpec } from "../types/formkit";
import type { ConfigMap } from "@halo-dev/api-client";
import type { FormKitSchemaNode } from "@formkit/core";

const initialConfigMap: ConfigMap = {
  apiVersion: "v1alpha1",
  kind: "ConfigMap",
  metadata: {
    name: "",
  },
  data: {},
};

interface useSettingFormReturn {
  settings: Ref<FormKitSetting | undefined>;
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
  const settings = ref<FormKitSetting | undefined>();
  const configMap = ref<ConfigMap>(cloneDeep(initialConfigMap));
  const configMapFormData = ref<
    Record<string, Record<string, string>> | undefined
  >();

  const saving = ref(false);

  const handleFetchSettings = async () => {
    if (!settingName.value) {
      settings.value = undefined;
      return;
    }
    try {
      const response = await apiClient.extension.setting.getv1alpha1Setting({
        name: settingName.value,
      });
      settings.value = response.data as FormKitSetting;

      // init configMapFormData
      if (!configMapFormData.value) {
        const { spec: schemaGroups } = settings.value;
        const initialConfigMapFormData: Record<
          string,
          Record<string, string>
        > = {};
        schemaGroups.forEach((schemaGroup) => {
          initialConfigMapFormData[schemaGroup.group] = {};
          const formSchema = schemaGroup.formSchema as FormKitSchemaNode[];
          formSchema.forEach((schema) => {
            // @ts-ignore
            if ("name" in schema && "$formkit" in schema) {
              initialConfigMapFormData[schemaGroup.group][schema.name] =
                schema.value || undefined;
            }
          });
        });
        configMapFormData.value = cloneDeep(initialConfigMapFormData);
      }
    } catch (e) {
      console.error(e);
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
        const { spec: schemaGroups } = settings.value || {};

        schemaGroups?.forEach((schemaGroup) => {
          if (!configMapFormData.value) {
            return;
          }
          configMapFormData.value[schemaGroup.group] = merge(
            configMapFormData.value[schemaGroup.group] || {},
            JSON.parse(data[schemaGroup.group] || "{}")
          );
        });
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleSaveConfigMap = async () => {
    try {
      saving.value = true;

      if (!configMap.value.metadata.name && configMapName.value) {
        configMap.value.metadata.name = configMapName.value;
      }

      settings?.value?.spec.forEach((item: FormKitSettingSpec) => {
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
      console.error(e);
    } finally {
      await handleFetchSettings();
      await handleFetchConfigMap();
      saving.value = false;
    }
  };

  const handleReset = () => {
    settings.value = undefined;
    configMap.value = cloneDeep(initialConfigMap);
    configMapFormData.value = undefined;
  };

  return {
    settings,
    configMap,
    configMapFormData,
    saving,
    handleFetchSettings,
    handleFetchConfigMap,
    handleSaveConfigMap,
    handleReset,
  };
}
