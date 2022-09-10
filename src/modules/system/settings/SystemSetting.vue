<script lang="ts" setup>
// core libs
import { computed, ref } from "vue";

// components
import { VButton } from "@halo-dev/components";

// hooks
import { useSettingForm } from "@halo-dev/admin-shared";
import { useRouteParams } from "@vueuse/router";

const group = useRouteParams<string>("group");

const {
  settings,
  configMapFormData,
  saving,
  handleFetchConfigMap,
  handleFetchSettings,
  handleSaveConfigMap,
} = useSettingForm(ref("system"), ref("system"));

const formSchema = computed(() => {
  if (!settings?.value?.spec) {
    return;
  }
  return settings.value.spec.find((item) => item.group === group?.value)
    ?.formSchema;
});

await handleFetchSettings();
await handleFetchConfigMap();
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
