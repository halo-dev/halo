<script lang="ts" setup>
// core libs
import { computed, inject, ref, watchEffect } from "vue";

// hooks
import { useSettingForm } from "@halo-dev/admin-shared";

// components
import { VButton } from "@halo-dev/components";

// types
import type { Ref } from "vue";
import type { Plugin } from "@halo-dev/api-client";

const plugin = inject<Ref<Plugin>>("plugin", ref({} as Plugin));
const group = inject<Ref<string | undefined>>("activeTab");

const settingName = computed(() => plugin.value.spec?.settingName);
const configMapName = computed(() => plugin.value.spec?.configMapName);

const {
  settings,
  configMapFormData,
  saving,
  handleFetchSettings,
  handleFetchConfigMap,
  handleSaveConfigMap,
} = useSettingForm(settingName, configMapName);

const formSchema = computed(() => {
  if (!settings?.value?.spec) {
    return;
  }
  return settings.value.spec.find((item) => item.group === group?.value)
    ?.formSchema;
});

watchEffect(async () => {
  if (settingName.value && configMapName.value) {
    await handleFetchSettings();
    await handleFetchConfigMap();
  }
});
</script>
<template>
  <div class="bg-white p-4 sm:px-6">
    <div class="w-1/3">
      <FormKit
        v-if="group && formSchema && configMapFormData"
        :id="group"
        v-model="configMapFormData[group]"
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
