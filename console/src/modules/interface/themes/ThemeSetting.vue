<script lang="ts" setup>
// core libs
import { inject, ref, watch } from "vue";

// components
import { Toast, VButton } from "@halo-dev/components";

// types
import type { Ref } from "vue";
import type { ConfigMap, Setting, Theme } from "@halo-dev/api-client";

// hooks
import { useRouteParams } from "@vueuse/router";
import { apiClient } from "@/utils/api-client";
import { useSettingFormConvert } from "@/composables/use-setting-form";

const group = useRouteParams<string>("group");

const selectedTheme = inject<Ref<Theme | undefined>>("selectedTheme");

const saving = ref(false);
const setting = ref<Setting>();
const configMap = ref<ConfigMap>();

const { configMapFormData, formSchema, convertToSave } = useSettingFormConvert(
  setting,
  configMap,
  group
);

const handleFetchSettings = async () => {
  if (!selectedTheme?.value) return;

  const { data } = await apiClient.theme.fetchThemeSetting({
    name: selectedTheme?.value?.metadata.name,
  });

  setting.value = data;
};

const handleFetchConfigMap = async () => {
  if (!selectedTheme?.value) return;

  const { data } = await apiClient.theme.fetchThemeConfig({
    name: selectedTheme?.value?.metadata.name,
  });

  configMap.value = data;
};

const handleSaveConfigMap = async () => {
  saving.value = true;

  const configMapToUpdate = convertToSave();

  if (!configMapToUpdate || !selectedTheme?.value) {
    saving.value = false;
    return;
  }

  const { data: newConfigMap } = await apiClient.theme.updateThemeConfig({
    name: selectedTheme?.value?.metadata.name,
    configMap: configMapToUpdate,
  });

  Toast.success("保存成功");

  await handleFetchSettings();
  configMap.value = newConfigMap;

  saving.value = false;
};

await handleFetchSettings();
await handleFetchConfigMap();

watch(
  () => selectedTheme?.value,
  () => {
    handleFetchSettings();
    handleFetchConfigMap();
  }
);
</script>
<template>
  <Transition mode="out-in" name="fade">
    <div class="bg-white p-4">
      <div>
        <FormKit
          v-if="group && formSchema && configMapFormData?.[group]"
          :id="group"
          v-model="configMapFormData[group]"
          :name="group"
          :actions="false"
          :preserve="true"
          type="form"
          @submit="handleSaveConfigMap"
        >
          <FormKitSchema
            :schema="formSchema"
            :data="configMapFormData[group]"
          />
        </FormKit>
      </div>
      <div v-permission="['system:themes:manage']" class="pt-5">
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
  </Transition>
</template>
