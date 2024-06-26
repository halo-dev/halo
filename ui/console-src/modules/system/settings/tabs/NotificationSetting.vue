<script lang="ts" setup>
import StickyBlock from "@/components/sticky-block/StickyBlock.vue";
import type { FormKitSchemaCondition, FormKitSchemaNode } from "@formkit/core";
import type { NotifierDescriptor, Setting } from "@halo-dev/api-client";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
import { Toast, VButton } from "@halo-dev/components";
import { useMutation, useQuery, useQueryClient } from "@tanstack/vue-query";
import type { Ref } from "vue";
import { computed, inject, ref, toRaw } from "vue";
import { useI18n } from "vue-i18n";

const queryClient = useQueryClient();
const { t } = useI18n();

const notifierDescriptor = inject<Ref<NotifierDescriptor | undefined>>(
  "notifierDescriptor",
  ref()
);

const name = computed(
  (): string => notifierDescriptor.value?.metadata.name as string
);

const { data: setting } = useQuery<Setting | undefined>({
  queryKey: ["notifier-setting", notifierDescriptor],
  queryFn: async () => {
    const { data } = await coreApiClient.setting.getSetting({
      name: notifierDescriptor.value?.spec?.senderSettingRef?.name as string,
    });
    return data;
  },
  enabled: computed(() => !!notifierDescriptor.value),
});

const configMapData = ref<Record<string, unknown>>({});

useQuery<Record<string, unknown>>({
  queryKey: ["notifier-configMap", notifierDescriptor],
  queryFn: async () => {
    const { data } =
      await consoleApiClient.notification.notifier.fetchSenderConfig({
        name: name.value,
      });
    return data as Record<string, unknown>;
  },
  onSuccess(data) {
    configMapData.value = data;
  },
  enabled: computed(() => !!notifierDescriptor.value),
});

const formSchema = computed(() => {
  return setting.value?.spec.forms.find(
    (form) =>
      form.group === notifierDescriptor.value?.spec?.senderSettingRef?.group
  )?.formSchema as (FormKitSchemaCondition | FormKitSchemaNode)[];
});

const { isLoading: isMutating, mutate } = useMutation({
  mutationKey: ["save-notifier-configMap", notifierDescriptor],
  mutationFn: async () => {
    const { data } =
      await consoleApiClient.notification.notifier.saveSenderConfig({
        name: name.value,
        body: configMapData.value,
      });
    return data;
  },
  onSuccess() {
    queryClient.invalidateQueries({ queryKey: ["notifier-configMap"] });
    Toast.success(t("core.common.toast.save_success"));
  },
});
</script>

<template>
  <Transition v-if="formSchema" mode="out-in" appear name="fade">
    <div>
      <FormKit
        :id="name"
        v-model="configMapData"
        :name="name"
        :preserve="true"
        type="form"
        @submit="mutate()"
      >
        <FormKitSchema :schema="toRaw(formSchema)" :data="configMapData" />
      </FormKit>

      <StickyBlock
        v-permission="['system:configmaps:manage']"
        class="-mx-4 -mb-4 rounded-b-base rounded-t-lg bg-white p-4 pt-5"
        position="bottom"
      >
        <VButton
          :loading="isMutating"
          type="secondary"
          @click="$formkit.submit(name)"
        >
          {{ $t("core.common.buttons.save") }}
        </VButton>
      </StickyBlock>
    </div>
  </Transition>
</template>
