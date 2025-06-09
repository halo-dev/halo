<script lang="ts" setup>
import HasPermission from "@/components/permission/HasPermission.vue";
import StickyBlock from "@/components/sticky-block/StickyBlock.vue";
import { useGlobalInfoStore } from "@/stores/global-info";
import type { FormKitSchemaCondition, FormKitSchemaNode } from "@formkit/core";
import type { Setting } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { Toast, VButton, VLoading } from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { computed, inject, ref, toRaw, type Ref } from "vue";
import { useI18n } from "vue-i18n";

const { t, locale } = useI18n();
const queryClient = useQueryClient();

const group = inject<Ref<string>>("activeTab", ref("basic"));
const setting = inject<Ref<Setting | undefined>>("setting", ref());
const isSubmitting = ref(false);

const { data: configMapGroupData, isLoading } = useQuery({
  queryKey: ["core:system:configMap:group-data", group],
  queryFn: async () => {
    const { data } =
      await consoleApiClient.configMap.system.getSystemConfigByGroup({
        group: group.value,
      });
    return data as Record<string, unknown>;
  },
  enabled: computed(() => !!group.value),
});

const formSchema = computed(() => {
  if (!setting.value) {
    return;
  }
  const { forms } = setting.value.spec;
  return forms.find((item) => item.group === group?.value)?.formSchema as (
    | FormKitSchemaCondition
    | FormKitSchemaNode
  )[];
});

const handleSaveConfigMap = async (data: Record<string, unknown>) => {
  try {
    isSubmitting.value = true;
    await consoleApiClient.configMap.system.updateSystemConfigByGroup({
      group: group.value,
      body: data,
    });

    queryClient.invalidateQueries({
      queryKey: ["core:system:configMap:group-data"],
    });

    await useGlobalInfoStore().fetchGlobalInfo();

    if (group.value === "basic") {
      const language = data.language;
      locale.value = language as string;
      document.cookie = `language=${language}; path=/; SameSite=Lax; Secure`;
    }

    Toast.success(t("core.common.toast.save_success"));
  } catch (error) {
    console.error(error);
    Toast.error(t("core.common.toast.save_failed_and_retry"));
  } finally {
    isSubmitting.value = false;
  }
};
</script>
<template>
  <div class="p-4">
    <VLoading v-if="isLoading" />
    <FormKit
      v-else-if="group && formSchema && configMapGroupData"
      :id="group"
      :value="configMapGroupData"
      :name="group"
      :preserve="true"
      type="form"
      @submit="handleSaveConfigMap"
    >
      <FormKitSchema
        :schema="toRaw(formSchema)"
        :data="toRaw(configMapGroupData)"
      />
    </FormKit>

    <HasPermission
      v-if="!isLoading"
      :permissions="['system:configmaps:manage']"
    >
      <StickyBlock
        class="-mx-4 -mb-4 rounded-b-base rounded-t-lg bg-white p-4 pt-5"
        position="bottom"
      >
        <VButton
          :loading="isSubmitting"
          type="secondary"
          @click="$formkit.submit(group || '')"
        >
          {{ $t("core.common.buttons.save") }}
        </VButton>
      </StickyBlock>
    </HasPermission>
  </div>
</template>
