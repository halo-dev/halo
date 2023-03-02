<script lang="ts" setup>
// core libs
import { computed, ref } from "vue";

// components
import { VButton } from "@halo-dev/components";

// hooks
import { useSettingForm } from "@/composables/use-setting-form";
import { useRouteParams } from "@vueuse/router";
import type { FormKitSchemaCondition, FormKitSchemaNode } from "@formkit/core";
import { useSystemConfigMapStore } from "@/stores/system-configmap";

const group = useRouteParams<string>("group");

const {
  setting,
  configMapFormData,
  saving,
  handleFetchConfigMap,
  handleFetchSettings,
  handleSaveConfigMap,
} = useSettingForm(ref("system"), ref("system"));

const formSchema = computed(() => {
  if (!setting.value) {
    return;
  }
  return setting.value.spec.forms.find((item) => item.group === group?.value)
    ?.formSchema as (FormKitSchemaCondition | FormKitSchemaNode)[];
});

const systemConfigMapStore = useSystemConfigMapStore();

const handleSave = async () => {
  await handleSaveConfigMap();
  await systemConfigMapStore.fetchSystemConfigMap();
};

await handleFetchSettings();
await handleFetchConfigMap();
</script>
<template>
  <Transition mode="out-in" name="fade">
    <div class="bg-white p-4">
      <div>
        <FormKit
          v-if="group && formSchema && configMapFormData"
          :id="group"
          v-model="configMapFormData[group]"
          :name="group"
          :actions="false"
          :preserve="true"
          type="form"
          @submit="handleSave"
        >
          <FormKitSchema
            :schema="formSchema"
            :data="configMapFormData[group]"
          />
        </FormKit>
      </div>
      <div v-permission="['system:configmaps:manage']" class="pt-5">
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
