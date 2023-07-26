<script lang="ts" setup>
import UppyUpload from "@/components/upload/UppyUpload.vue";
import { Dialog, Toast, VAlert, VButton, VLoading } from "@halo-dev/components";
import { useQuery } from "@tanstack/vue-query";
import axios from "axios";
import { computed } from "vue";
import { ref } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const complete = ref(false);
const showUploader = ref(false);

const onUploaded = () => {
  Dialog.success({
    title: t("core.backup.operations.restore.title"),
    description: t("core.backup.operations.restore.description"),
    confirmText: t("core.common.buttons.confirm"),
    showCancel: false,
    async onConfirm() {
      await handleShutdown();
    },
  });
};

async function handleShutdown() {
  await axios.post(`/actuator/shutdown`);
  Toast.success(t("core.backup.operations.shutdown.toast_success"));

  setTimeout(() => {
    complete.value = true;
  }, 1000);
}

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
  enabled: computed(() => complete.value),
});
</script>

<template>
  <div v-if="!complete">
    <div class="px-4 py-3">
      <VAlert :title="$t('core.common.text.tip')" :closable="false">
        <template #description>
          <ul>
            <li>{{ $t("core.backup.restore.tips.first") }}</li>
            <li>
              {{ $t("core.backup.restore.tips.second") }}
            </li>
            <li>
              {{ $t("core.backup.restore.tips.third") }}
            </li>
          </ul>
        </template>
        <template v-if="!showUploader" #actions>
          <VButton @click="showUploader = true">
            {{ $t("core.backup.restore.start") }}
          </VButton>
        </template>
      </VAlert>
    </div>
    <div v-if="showUploader" class="flex items-center justify-center px-4 py-3">
      <UppyUpload
        :restrictions="{
          maxNumberOfFiles: 1,
          allowedFileTypes: ['.zip'],
        }"
        endpoint="/apis/api.console.migration.halo.run/v1alpha1/restorations"
        width="100%"
        @uploaded="onUploaded"
      />
    </div>
  </div>

  <div v-else class="flex h-72 flex-col items-center justify-center">
    <VLoading />
    <div class="text-xs text-gray-600">
      {{ $t("core.backup.restore.tips.complete") }}
    </div>
  </div>
</template>
