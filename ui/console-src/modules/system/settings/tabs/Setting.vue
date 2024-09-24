<script lang="ts" setup>
// core libs
import { computed, inject, ref, toRaw, type Ref } from "vue";

// components
import StickyBlock from "@/components/sticky-block/StickyBlock.vue";
import { Toast, VButton } from "@halo-dev/components";

// hooks
import { useGlobalInfoStore } from "@/stores/global-info";
import { useSettingFormConvert } from "@console/composables/use-setting-form";
import type { ConfigMap, Setting } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

const SYSTEM_CONFIGMAP_NAME = "system";

const { t } = useI18n();
const queryClient = useQueryClient();

const group = inject<Ref<string>>("activeTab", ref("basic"));
const setting = inject<Ref<Setting | undefined>>("setting", ref());
const saving = ref(false);

const { data: configMap } = useQuery<ConfigMap>({
  queryKey: ["system-configMap"],
  queryFn: async () => {
    const { data } = await coreApiClient.configMap.getConfigMap({
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

  await coreApiClient.configMap.updateConfigMap({
    name: SYSTEM_CONFIGMAP_NAME,
    configMap: configMapToUpdate,
  });

  Toast.success(t("core.common.toast.save_success"));

  queryClient.invalidateQueries({ queryKey: ["system-configMap"] });
  await useGlobalInfoStore().fetchGlobalInfo();

  saving.value = false;
};
</script>
<template>
  <Transition mode="out-in" name="fade">
    <div class="p-4">
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

      <StickyBlock
        v-permission="['system:configmaps:manage']"
        class="-mx-4 -mb-4 rounded-b-base rounded-t-lg bg-white p-4 pt-5"
        position="bottom"
      >
        <VButton
          :loading="saving"
          type="secondary"
          @click="$formkit.submit(group || '')"
        >
          {{ $t("core.common.buttons.save") }}
        </VButton>
      </StickyBlock>
    </div>
  </Transition>
</template>
