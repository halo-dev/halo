<script lang="ts" setup>
import StickyBlock from "@/components/sticky-block/StickyBlock.vue";
import { useSettingFormConvert } from "@console/composables/use-setting-form";
import type { ConfigMap, Plugin, Setting } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { Toast, VButton } from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed, inject, ref, toRaw, type Ref } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const queryClient = useQueryClient();

const group = inject<Ref<string>>("activeTab", ref("basic"));
const plugin = inject<Ref<Plugin | undefined>>("plugin");
const setting = inject<Ref<Setting | undefined>>("setting", ref());
const saving = ref(false);

const { data: configMap } = useQuery<ConfigMap>({
  queryKey: ["plugin-configMap", plugin],
  queryFn: async () => {
    const { data } = await consoleApiClient.plugin.plugin.fetchPluginConfig({
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

  await consoleApiClient.plugin.plugin.updatePluginConfig({
    name: plugin.value.metadata.name,
    configMap: configMapToUpdate,
  });

  Toast.success(t("core.common.toast.save_success"));

  queryClient.invalidateQueries({ queryKey: ["plugin-configMap"] });

  saving.value = false;
};
</script>
<template>
  <Transition mode="out-in" name="fade">
    <div class="rounded-b-base bg-white p-4">
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

      <StickyBlock
        v-permission="['system:plugins:manage']"
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
