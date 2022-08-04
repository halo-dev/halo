<script lang="ts" setup>
import type { Ref } from "vue";
import { computed, inject, ref } from "vue";
import { VButton } from "@halo-dev/components";
import { apiClient } from "@halo-dev/admin-shared";
import type { ConfigMap, Theme } from "@halo-dev/api-client";
import type {
  FormKitSetting,
  FormKitSettingSpec,
} from "@halo-dev/admin-shared/src";

const settings = inject<Ref<FormKitSetting | undefined>>("settings");
const configmapFormData =
  inject<Ref<Record<string, Record<string, string>> | undefined>>(
    "configmapFormData"
  );
const configmap = inject<Ref<ConfigMap>>("configmap", {} as Ref<ConfigMap>);
const selectedTheme = inject<Ref<Theme>>("selectedTheme", ref({} as Theme));
const group = inject<Ref<string | undefined>>("activeTab");

const saving = ref(false);

const formSchema = computed(() => {
  if (!settings?.value?.spec) {
    return;
  }
  return settings.value.spec.find((item) => item.group === group?.value)
    ?.formSchema;
});

const handleFetchSettings = inject<() => void>("handleFetchSettings");
const handleFetchConfigMap = inject<() => void>("handleFetchConfigMap");

const handleSaveConfigMap = async () => {
  try {
    saving.value = true;

    if (
      !configmap.value.metadata.name &&
      selectedTheme.value.spec.configMapName
    ) {
      configmap.value.metadata.name = selectedTheme.value.spec.configMapName;
    }

    settings?.value?.spec.forEach((item: FormKitSettingSpec) => {
      // @ts-ignore
      configmap.value.data[item.group] = JSON.stringify(
        configmapFormData?.value?.[item.group]
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
    handleFetchSettings?.();
    handleFetchConfigMap?.();
    saving.value = false;
  }
};
</script>
<template>
  <div class="bg-white p-4 sm:px-6">
    <div class="w-1/3">
      <FormKit
        v-if="group && formSchema && configmapFormData"
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
