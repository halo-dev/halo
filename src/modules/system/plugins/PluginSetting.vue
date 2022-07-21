<script lang="ts" setup>
import { VButton } from "@halo-dev/components";
import type { Ref } from "vue";
import { computed, inject, onMounted, ref, watch } from "vue";
import type { ConfigMap, Plugin } from "@halo-dev/api-client";
import type {
  FormKitSetting,
  FormKitSettingSpec,
} from "@halo-dev/admin-shared";
import { apiClient } from "@halo-dev/admin-shared";

const plugin = inject<Ref<Plugin>>("plugin", ref({} as Plugin));
const settings = ref<FormKitSetting>({} as FormKitSetting);
const configmapFormData = ref<Record<string, Record<string, string>>>({});
const configmap = ref<ConfigMap>({
  data: {},
  apiVersion: "v1alpha1",
  kind: "ConfigMap",
  metadata: {
    name: "",
  },
});
const saving = ref(false);

const group = inject<Ref<string | undefined>>("activeTab");

const formSchema = computed(() => {
  if (!settings.value.spec) {
    return;
  }
  return settings.value.spec.find((item) => item.group === group?.value)
    ?.formSchema;
});

const handleFetchSettings = async () => {
  if (!plugin.value.spec?.settingName) {
    return;
  }
  try {
    const response = await apiClient.extension.setting.getv1alpha1Setting(
      plugin.value.spec.settingName as string
    );
    settings.value = response.data as FormKitSetting;

    const { spec } = settings.value;

    if (spec) {
      spec.forEach((item: FormKitSettingSpec) => {
        configmapFormData.value[item.group] = {};
      });
    }
  } catch (e) {
    console.error(e);
  }
};

const handleFetchConfigMap = async () => {
  if (!plugin.value.spec?.configMapName) {
    return;
  }
  try {
    const response = await apiClient.extension.configMap.getv1alpha1ConfigMap(
      plugin.value.spec?.configMapName as string
    );
    configmap.value = response.data;

    const { data } = configmap.value;

    if (data) {
      Object.keys(data).forEach((key) => {
        configmapFormData.value[key] = JSON.parse(data[key]);
      });
    }
  } catch (e) {
    console.error(e);
  }
};

const handleSaveConfigMap = async () => {
  try {
    saving.value = true;

    if (!configmap.value.metadata.name && plugin.value.spec.configMapName) {
      configmap.value.metadata.name = plugin.value.spec.configMapName;
    }

    settings.value.spec.forEach((item: FormKitSettingSpec) => {
      // @ts-ignore
      configmap.value.data[item.group] = JSON.stringify(
        configmapFormData.value[item.group]
      );
    });

    if (!configmap.value.metadata.creationTimestamp) {
      await apiClient.extension.configMap.createv1alpha1ConfigMap(
        configmap.value
      );
    } else {
      await apiClient.extension.configMap.updatev1alpha1ConfigMap(
        configmap.value.metadata.name,
        configmap.value
      );
    }
  } catch (e) {
    console.error(e);
  } finally {
    await handleFetchConfigMap();
    saving.value = false;
  }
};

onMounted(() => {
  handleFetchSettings();
  handleFetchConfigMap();
});

watch([() => plugin.value, () => group?.value], () => {
  handleFetchConfigMap();
  handleFetchConfigMap();
});
</script>
<template>
  <div class="bg-white p-4 sm:px-6">
    <div class="w-1/3">
      <FormKit
        v-if="group && formSchema"
        :id="group"
        v-model="configmapFormData[group]"
        :actions="false"
        :preserve="true"
        type="form"
        @submit="handleSaveConfigMap"
      >
        <FormKitSchema :schema="formSchema" />
      </FormKit>
    </div>
    <div class="pt-5">
      <div class="flex justify-start">
        <VButton
          :loading="saving"
          type="secondary"
          @click="$formkit.submit(group || '')"
        >
          保存
        </VButton>
      </div>
    </div>
  </div>
</template>
