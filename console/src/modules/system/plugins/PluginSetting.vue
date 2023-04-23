<script lang="ts" setup>
// core libs
import { inject, ref, type Ref, computed } from "vue";

// hooks
import { useSettingFormConvert } from "@/composables/use-setting-form";
import { apiClient } from "@/utils/api-client";

// components
import { Toast, VButton } from "@halo-dev/components";

// types
import type { ConfigMap, Plugin, Setting } from "@halo-dev/api-client";
import { useRouteParams } from "@vueuse/router";
import { useI18n } from "vue-i18n";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { toRaw } from "vue";

const { t } = useI18n();
const queryClient = useQueryClient();

const group = useRouteParams<string>("group");

const plugin = inject<Ref<Plugin | undefined>>("plugin");
const setting = inject<Ref<Setting | undefined>>("setting", ref());
const saving = ref(false);

const { data: configMap, suspense } = useQuery<ConfigMap>({
  queryKey: ["plugin-configMap", plugin],
  queryFn: async () => {
    const { data } = await apiClient.plugin.fetchPluginConfig({
      name: plugin?.value?.metadata.name as string,
    });
    return data;
  },
  enabled: computed(() => {
    return !!setting.value && !!plugin?.value;
  }),
});

const { configMapFormData, formSchema, convertToSave } = useSettingFormConvert(
  setting,
  configMap,
  group
);

const handleSaveConfigMap = async () => {
  saving.value = true;
  const configMapToUpdate = convertToSave();
  if (!configMapToUpdate || !plugin?.value) {
    saving.value = false;
    return;
  }

  await apiClient.plugin.updatePluginConfig({
    name: plugin.value.metadata.name,
    configMap: configMapToUpdate,
  });

  Toast.success(t("core.common.toast.save_success"));

  queryClient.invalidateQueries({ queryKey: ["plugin-configMap"] });

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
      <div v-permission="['system:plugins:manage']" class="pt-5">
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
