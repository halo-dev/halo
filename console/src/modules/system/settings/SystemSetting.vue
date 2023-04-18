<script lang="ts" setup>
// core libs
import { computed, ref, type Ref, inject, toRaw } from "vue";

// components
import { Toast, VButton } from "@halo-dev/components";

// hooks
import { useSettingFormConvert } from "@/composables/use-setting-form";
import { useRouteParams } from "@vueuse/router";
import { useSystemConfigMapStore } from "@/stores/system-configmap";
import type { ConfigMap, Setting } from "@halo-dev/api-client";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { apiClient } from "@/utils/api-client";
import { useI18n } from "vue-i18n";

const SYSTEM_CONFIGMAP_NAME = "system";

const { t } = useI18n();
const systemConfigMapStore = useSystemConfigMapStore();
const queryClient = useQueryClient();

const saving = ref(false);
const group = useRouteParams<string>("group");
const setting = inject<Ref<Setting | undefined>>("setting", ref());

const { data: configMap, suspense } = useQuery<ConfigMap>({
  queryKey: ["system-configMap"],
  queryFn: async () => {
    const { data } = await apiClient.extension.configMap.getv1alpha1ConfigMap({
      name: SYSTEM_CONFIGMAP_NAME,
    });
    return data;
  },
  enabled: computed(() => !!setting.value),
});

const { configMapFormData, formSchema, convertToSave } = useSettingFormConvert(
  setting,
  configMap,
  group
);

const handleSaveConfigMap = async () => {
  saving.value = true;

  const configMapToUpdate = convertToSave();

  if (!configMapToUpdate) {
    saving.value = false;
    return;
  }

  const { data } = await apiClient.extension.configMap.updatev1alpha1ConfigMap({
    name: SYSTEM_CONFIGMAP_NAME,
    configMap: configMapToUpdate,
  });

  Toast.success(t("core.common.toast.save_success"));

  queryClient.invalidateQueries({ queryKey: ["system-configMap"] });

  systemConfigMapStore.configMap = data;

  saving.value = false;
};

await suspense();
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
            :schema="toRaw(formSchema)"
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
            {{ $t("core.common.buttons.save") }}
          </VButton>
        </div>
      </div>
    </div>
  </Transition>
</template>
