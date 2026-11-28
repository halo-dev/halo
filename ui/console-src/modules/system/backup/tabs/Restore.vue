<script lang="ts" setup>
import type { BackupFile } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import {
  Dialog,
  Toast,
  VAlert,
  VButton,
  VEmpty,
  VEntity,
  VEntityContainer,
  VEntityField,
  VLoading,
  VModal,
  VSpace,
  VTabItem,
  VTabs,
} from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useMutation, useQuery } from "@tanstack/vue-query";
import axios from "axios";
import prettyBytes from "pretty-bytes";
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import SubmitButton from "@/components/button/SubmitButton.vue";

const { t } = useI18n();

const complete = ref(false);
const showUploader = ref(false);
const activeTabId = ref<"local" | "remote" | "backups">("local");

const onProcessCompleted = () => {
  Dialog.success({
    title: t("core.backup.operations.restore.title"),
    description: t("core.backup.operations.restore.description"),
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
const remotePassword = ref("");
const { isLoading: downloading, mutate: handleRemoteDownload } = useMutation({
  mutationKey: ["remote-download-restore"],
  mutationFn: async () => {
    return await consoleApiClient.migration.restoreBackup({
      downloadUrl: remoteDownloadUrl.value,
      password: remotePassword.value || undefined,
    });
  },
  retry: false,
  onSuccess() {
    onProcessCompleted();
  },
});

const localPassword = ref("");

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

const restoreTarget = ref<BackupFile>();
const restorePassword = ref("");
const isRestoring = ref(false);

function handleRestoreFromBackup(backupFile: BackupFile) {
  if (backupFile.filename?.endsWith(".enc")) {
    restoreTarget.value = backupFile;
    restorePassword.value = "";
    return;
  }

  Dialog.info({
    title: t("core.backup.operations.restore_by_backup.title"),
    description: t("core.backup.operations.restore_by_backup.description", {
      filename: backupFile.filename,
    }),
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

async function handleRestoreEncryptedBackup() {
  if (!restoreTarget.value) {
    return;
  }
  try {
    isRestoring.value = true;
    await consoleApiClient.migration.restoreBackup({
      filename: restoreTarget.value.filename,
      password: restorePassword.value,
    });
    restoreTarget.value = undefined;
    setTimeout(() => {
      onProcessCompleted();
    }, 200);
  } finally {
    isRestoring.value = false;
  }
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
          <FormKit
            v-model="localPassword"
            :label="$t('core.backup.restore.fields.password.label')"
            :help="$t('core.backup.restore.fields.password.help')"
            type="password"
            autocomplete="current-password"
          ></FormKit>
          <UppyUpload
            :restrictions="{
              maxNumberOfFiles: 1,
              allowedFileTypes: ['.zip', '.enc'],
            }"
            endpoint="/apis/console.api.migration.halo.run/v1alpha1/restorations"
            :meta="{ password: localPassword }"
            :allowed-meta-fields="['password']"
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
            <FormKit
              v-model="remotePassword"
              :label="$t('core.backup.restore.fields.password.label')"
              :help="$t('core.backup.restore.fields.password.help')"
              type="password"
              autocomplete="current-password"
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
          <VEntityContainer v-else-if="backupFiles?.length">
            <VEntity
              v-for="backupFile in backupFiles"
              :key="backupFile.filename"
            >
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
                      {{ utils.date.format(backupFile.lastModifiedTime) }}
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
          </VEntityContainer>

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

  <VModal
    v-if="restoreTarget"
    :width="500"
    :title="$t('core.backup.operations.restore_by_backup.title')"
    @close="restoreTarget = undefined"
  >
    <p class="mb-4 text-sm text-gray-600">
      {{
        $t("core.backup.operations.restore_by_backup.description", {
          filename: restoreTarget.filename,
        })
      }}
    </p>
    <FormKit
      id="restore-backup-password-form"
      type="form"
      :actions="false"
      @submit="handleRestoreEncryptedBackup"
    >
      <FormKit
        v-model="restorePassword"
        :label="$t('core.backup.restore.fields.password.label')"
        type="password"
        validation="required"
        autocomplete="current-password"
      ></FormKit>
    </FormKit>
    <template #footer>
      <VSpace>
        <SubmitButton
          :loading="isRestoring"
          type="secondary"
          :text="$t('core.common.buttons.submit')"
          @submit="$formkit.submit('restore-backup-password-form')"
        >
        </SubmitButton>
        <VButton @click="restoreTarget = undefined">
          {{ $t("core.common.buttons.cancel_and_shortcut") }}
        </VButton>
      </VSpace>
    </template>
  </VModal>
</template>
