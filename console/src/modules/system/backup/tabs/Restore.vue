<script lang="ts" setup>
import UppyUpload from "@/components/upload/UppyUpload.vue";
import { Dialog, Toast, VAlert } from "@halo-dev/components";
import axios from "axios";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const onUploaded = () => {
  Dialog.success({
    title: t("core.backup.operations.restore.title"),
    description: t("core.backup.operations.restore.description"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    async onConfirm() {
      await handleShutdown();
    },
    async onCancel() {
      await handleShutdown();
    },
  });
};

async function handleShutdown() {
  await axios.post(`/actuator/shutdown`);
  Toast.success(t("core.backup.operations.shutdown.toast_success"));
}
</script>

<template>
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
    </VAlert>
  </div>
  <div class="flex items-center justify-center px-4 py-3">
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
</template>
