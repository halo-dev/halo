<script lang="ts" setup>
// core libs
import { computed, inject, ref, toRaw } from "vue";

// components
import { Toast, VButton } from "@halo-dev/components";

// types
import type { ConfigMap, Setting, Theme } from "@halo-dev/api-client";
import type { Ref } from "vue";

// hooks
import StickyBlock from "@/components/sticky-block/StickyBlock.vue";
import { useSettingFormConvert } from "@console/composables/use-setting-form";
import { consoleApiClient } from "@halo-dev/api-client";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const queryClient = useQueryClient();

const group = inject<Ref<string>>("activeTab", ref(""));

const selectedTheme = inject<Ref<Theme | undefined>>("selectedTheme");
const setting = inject<Ref<Setting | undefined>>("setting", ref());

const saving = ref(false);

const { data: configMap, suspense } = useQuery<ConfigMap>({
  queryKey: ["theme-configMap", selectedTheme],
  queryFn: async () => {
    const { data } = await consoleApiClient.theme.theme.fetchThemeConfig({
      name: selectedTheme?.value?.metadata.name as string,
    });
    return data;
  },
  enabled: computed(() => {
    return !!setting.value && !!selectedTheme?.value;
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

  if (!configMapToUpdate || !selectedTheme?.value) {
    saving.value = false;
    return;
  }

  await consoleApiClient.theme.theme.updateThemeConfig({
    name: selectedTheme?.value?.metadata.name,
    configMap: configMapToUpdate,
  });

  Toast.success(t("core.common.toast.save_success"));

  queryClient.invalidateQueries({ queryKey: ["theme-configMap"] });

  saving.value = false;
};

await suspense();
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
        v-permission="['system:themes:manage']"
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
