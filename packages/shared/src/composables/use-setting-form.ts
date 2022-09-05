// core libs
// types
import type { Ref } from "vue";
import { ref } from "vue";
import { apiClient } from "../utils/api-client";

// libs
import cloneDeep from "lodash.clonedeep";
import type { FormKitSetting, FormKitSettingSpec } from "../types/formkit";
import type { ConfigMap } from "@halo-dev/api-client";

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
      const response = await apiClient.extension.setting.getv1alpha1Setting(
        settingName.value
      );
      settings.value = response.data as FormKitSetting;
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
        configMapName.value
      );
      configMap.value = response.data;

      const { data } = configMap.value;

      if (data) {
        configMapFormData.value = Object.keys(data).reduce((acc, key) => {
          // @ts-ignore
          acc[key] = JSON.parse(data[key]);
          return acc;
        }, {});
      }
    } catch (e) {
      console.error(e);
    } finally {
      if (!configMapFormData.value) {
        configMapFormData.value = settings.value?.spec.reduce((acc, item) => {
          // @ts-ignore
          acc[item.group] = {};
          return acc;
        }, {});
      }
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
        await apiClient.extension.configMap.createv1alpha1ConfigMap(
          configMap.value
        );
      } else {
        await apiClient.extension.configMap.updatev1alpha1ConfigMap(
          configMap.value.metadata.name,
          configMap.value
        );
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
