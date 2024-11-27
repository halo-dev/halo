<script lang="ts" setup>
import { formatDatetime } from "@/utils/date";
import type { BackupFile } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  Toast,
  VAlert,
  VButton,
  VEmpty,
  VEntity,
  VEntityField,
  VLoading,
  VTabItem,
  VTabs,
} from "@halo-dev/components";
import { useMutation, useQuery } from "@tanstack/vue-query";
import axios from "axios";
import prettyBytes from "pretty-bytes";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const complete = ref(false);
const showUploader = ref(false);
const activeTabId = ref<"local" | "remote" | "backups">("local");

const onProcessCompleted = () => {
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
  await axios.post(`/actuator/restart`);
  Toast.success(t("core.backup.operations.restart.toast_success"));

  setTimeout(() => {
    complete.value = true;
  }, 1000);
}

// Remote download to restore
const remoteDownloadUrl = ref("");
const { isLoading: downloading, mutate: handleRemoteDownload } = useMutation({
  mutationKey: ["remote-download-restore"],
  mutationFn: async () => {
    return await consoleApiClient.migration.restoreBackup({
      downloadUrl: remoteDownloadUrl.value,
    });
  },
  retry: false,
  onSuccess() {
    onProcessCompleted();
  },
});

const {
  data: backupFiles,
  refetch: refetchBackupFiles,
  isLoading: isLoadingBackupFiles,
  isFetching: isFetchingBackupFiles,
} = useQuery({
  queryKey: ["backup-files", activeTabId],
  queryFn: async () => {
    const { data } = await consoleApiClient.migration.getBackupFiles();
    return data;
  },
  enabled: computed(() => activeTabId.value === "backups"),
});

function handleRestoreFromBackup(backupFile: BackupFile) {
  Dialog.info({
    title: t("core.backup.operations.restore_by_backup.title"),
    description: t("core.backup.operations.restore_by_backup.description", {
      filename: backupFile.filename,
    }),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    async onConfirm() {
      await consoleApiClient.migration.restoreBackup({
        filename: backupFile.filename,
      });
      setTimeout(() => {
        onProcessCompleted();
      }, 200);
    },
  });
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
              <strong>
                {{ $t("core.backup.restore.tips.second") }}
              </strong>
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
    <div v-if="showUploader" class="flex flex-col px-4 pb-3">
      <VTabs v-model:active-id="activeTabId" type="pills">
        <VTabItem
          id="local"
          :label="$t('core.backup.restore.tabs.local.label')"
        >
          <UppyUpload
            :restrictions="{
              maxNumberOfFiles: 1,
              allowedFileTypes: ['.zip'],
            }"
            endpoint="/apis/console.api.migration.halo.run/v1alpha1/restorations"
            width="100%"
            @uploaded="onProcessCompleted"
          />
        </VTabItem>
        <VTabItem
          id="remote"
          :label="$t('core.backup.restore.tabs.remote.label')"
        >
          <FormKit
            id="restore-remote-download-form"
            name="restore-remote-download-form"
            type="form"
            :preserve="true"
            @submit="handleRemoteDownload()"
          >
            <FormKit
              v-model="remoteDownloadUrl"
              :label="$t('core.backup.restore.tabs.remote.fields.url')"
              type="text"
              validation="required"
            ></FormKit>
          </FormKit>

          <div class="pt-5">
            <VButton
              :loading="downloading"
              type="secondary"
              @click="$formkit.submit('restore-remote-download-form')"
            >
              {{ $t("core.backup.operations.remote_download.button") }}
            </VButton>
          </div>
        </VTabItem>
        <VTabItem
          id="backups"
          :label="$t('core.backup.restore.tabs.backup.label')"
        >
          <VLoading v-if="isLoadingBackupFiles" />
          <ul
            v-else-if="backupFiles?.length"
            class="box-border h-full w-full divide-y divide-gray-100 overflow-hidden rounded-base border"
            role="list"
          >
            <li v-for="backupFile in backupFiles" :key="backupFile.filename">
              <VEntity>
                <template #start>
                  <VEntityField
                    :title="backupFile.filename"
                    :description="prettyBytes(backupFile.size || 0)"
                  >
                  </VEntityField>
                </template>
                <template #end>
                  <VEntityField v-if="backupFile.lastModifiedTime">
                    <template #description>
                      <span class="truncate text-xs tabular-nums text-gray-500">
                        {{ formatDatetime(backupFile.lastModifiedTime) }}
                      </span>
                    </template>
                  </VEntityField>
                  <VEntityField v-permission="['system:migrations:manage']">
                    <template #description>
                      <VButton
                        size="sm"
                        @click="handleRestoreFromBackup(backupFile)"
                      >
                        {{
                          $t("core.backup.operations.restore_by_backup.button")
                        }}
                      </VButton>
                    </template>
                  </VEntityField>
                </template>
              </VEntity>
            </li>
          </ul>

          <VEmpty
            v-else
            :title="$t('core.backup.restore.tabs.backup.empty.title')"
            :message="$t('core.backup.restore.tabs.backup.empty.message')"
          >
            <template #actions>
              <VButton
                :loading="isFetchingBackupFiles"
                @click="refetchBackupFiles"
              >
                {{ $t("core.common.buttons.refresh") }}
              </VButton>
            </template>
          </VEmpty>
        </VTabItem>
      </VTabs>
    </div>
  </div>

  <div v-else class="flex h-72 flex-col items-center justify-center">
    <VLoading />
    <div class="text-xs text-gray-600">
      {{ $t("core.backup.restore.tips.complete") }}
    </div>
  </div>
</template>
