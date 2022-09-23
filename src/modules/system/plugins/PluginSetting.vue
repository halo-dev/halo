<script lang="ts" setup>
// core libs
import { computed, ref } from "vue";

// hooks
import { useSettingForm } from "@/composables/use-setting-form";
import { apiClient } from "@/utils/api-client";

// components
import { VButton } from "@halo-dev/components";

// types
import type { Plugin } from "@halo-dev/api-client";
import { useRouteParams } from "@vueuse/router";
import type { FormKitSchemaCondition, FormKitSchemaNode } from "@formkit/core";

const name = useRouteParams<string>("name");
const group = useRouteParams<string>("group");

const plugin = ref<Plugin | undefined>();

const settingName = computed(() => plugin?.value?.spec.settingName);
const configMapName = computed(() => plugin?.value?.spec.configMapName);

const {
  setting,
  configMapFormData,
  saving,
  handleFetchSettings,
  handleFetchConfigMap,
  handleSaveConfigMap,
} = useSettingForm(settingName, configMapName);

const formSchema = computed(() => {
  if (!setting.value) {
    return;
  }
  const { forms } = setting.value.spec;
  return forms.find((item) => item.group === group.value)?.formSchema as (
    | FormKitSchemaCondition
    | FormKitSchemaNode
  )[];
});

const handleFetchPlugin = async () => {
  try {
    const { data } =
      await apiClient.extension.plugin.getpluginHaloRunV1alpha1Plugin({
        name: name.value,
      });
    plugin.value = data;

    if (settingName.value && configMapName.value) {
      await handleFetchSettings();
      await handleFetchConfigMap();
    }
  } catch (e) {
    console.error("Failed to fetch plugin and settings", e);
  }
};

await handleFetchPlugin();
</script>
<template>
  <div class="bg-white p-4 sm:px-6">
    <div class="w-1/3">
      <FormKit
        v-if="group && formSchema && configMapFormData"
        :id="group"
        v-model="configMapFormData[group]"
        :name="group"
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
