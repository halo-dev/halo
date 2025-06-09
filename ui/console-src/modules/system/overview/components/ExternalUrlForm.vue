<script setup lang="ts">
import { setFocus } from "@/formkit/utils/focus";
import { useGlobalInfoFetch } from "@console/composables/use-global-info";
import { consoleApiClient } from "@halo-dev/api-client";
import { Dialog, Toast, VButton, VLoading, VSpace } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";

const BASIC_GROUP = "basic";

const { t } = useI18n();
const { globalInfo } = useGlobalInfoFetch();

const emit = defineEmits<{
  (event: "close"): void;
}>();

const isRestarting = ref(false);

function onSubmit({ externalUrl }: { externalUrl: string }) {
  Dialog.warning({
    title: t("core.overview.external_url_form.operations.save.title"),
    description: t(
      "core.overview.external_url_form.operations.save.description"
    ),
    confirmType: "danger",
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      const { data: basicConfig } =
        await consoleApiClient.configMap.system.getSystemConfigByGroup({
          group: BASIC_GROUP,
        });

      await consoleApiClient.configMap.system.updateSystemConfigByGroup({
        group: BASIC_GROUP,
        body: {
          ...basicConfig,
          externalUrl: externalUrl.trim(),
        },
      });

      await axios.post(`/actuator/restart`);

      isRestarting.value = true;

      Toast.success(t("core.common.toast.save_success"));
    },
  });
}

onMounted(() => {
  setFocus("externalUrl");
});

useQuery({
  queryKey: ["check-health"],
  queryFn: async () => {
    const { data } = await axios.get("/actuator/health");
    return data;
  },
  onSuccess(data) {
    if (data.status === "UP") {
      window.location.reload();
    }
  },
  retry: true,
  retryDelay: 2000,
  enabled: computed(() => isRestarting.value),
});
</script>

<template>
  <template v-if="!isRestarting">
    <FormKit
      id="external-url-form"
      type="form"
      name="external-url-form"
      @submit="onSubmit"
    >
      <FormKit
        id="externalUrl"
        :model-value="globalInfo?.externalUrl"
        type="url"
        name="externalUrl"
        validation="url|required"
        :validation-label="$t('core.overview.fields.external_url')"
        :classes="{ outer: '!pb-0' }"
      ></FormKit>
    </FormKit>
    <VSpace class="mt-4">
      <VButton type="secondary" @click="$formkit.submit('external-url-form')">
        {{ $t("core.common.buttons.save") }}
      </VButton>
      <VButton @click="emit('close')">
        {{ $t("core.common.buttons.cancel") }}
      </VButton>
    </VSpace>
  </template>
  <template v-else>
    <div class="flex items-center gap-2">
      <VLoading class="!inline-flex !py-0" />
      <div class="text-xs text-gray-600">
        {{ $t("core.overview.external_url_form.tips.restarting") }}
      </div>
    </div>
  </template>
</template>
