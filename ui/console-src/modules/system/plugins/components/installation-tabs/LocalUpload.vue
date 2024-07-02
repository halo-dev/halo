<script lang="ts" setup>
import AppDownloadAlert from "@/components/common/AppDownloadAlert.vue";
import type { Plugin } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import type { ErrorResponse, SuccessResponse, UppyFile } from "@uppy/core";
import { computed, inject, ref, type Ref } from "vue";
import { useI18n } from "vue-i18n";
import { PLUGIN_ALREADY_EXISTS_TYPE } from "../../constants";
import type { PluginInstallationErrorResponse } from "../../types";

const emit = defineEmits<{
  (event: "close-modal"): void;
}>();

const { t } = useI18n();
const queryClient = useQueryClient();

const pluginToUpgrade = inject<Ref<Plugin | undefined>>(
  "pluginToUpgrade",
  ref()
);

const endpoint = computed(() => {
  if (pluginToUpgrade.value) {
    return `/apis/api.console.halo.run/v1alpha1/plugins/${pluginToUpgrade.value.metadata.name}/upgrade`;
  }
  return "/apis/api.console.halo.run/v1alpha1/plugins/install";
});

const onUploaded = async (response: SuccessResponse) => {
  if (pluginToUpgrade.value) {
    Toast.success(t("core.common.toast.upgrade_success"));
    window.location.reload();
    return;
  }

  emit("close-modal");

  queryClient.invalidateQueries({ queryKey: ["plugins"] });

  handleShowActiveModalAfterInstall(response.body as Plugin);
};

const onError = (file: UppyFile, response: ErrorResponse) => {
  const body = response.body as PluginInstallationErrorResponse;

  if (body.type === PLUGIN_ALREADY_EXISTS_TYPE) {
    handleCatchExistsException(body, file.data as File);
  }
};

const handleShowActiveModalAfterInstall = (plugin: Plugin) => {
  Dialog.success({
    title: t("core.plugin.upload_modal.operations.active_after_install.title"),
    description: t(
      "core.plugin.upload_modal.operations.active_after_install.description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        await consoleApiClient.plugin.plugin.changePluginRunningState({
          name: plugin.metadata.name,
          pluginRunningStateRequest: {
            enable: true,
          },
        });

        window.location.reload();
      } catch (e) {
        console.error(e);
      }
    },
  });
};

const handleCatchExistsException = async (
  error: PluginInstallationErrorResponse,
  file?: File
) => {
  Dialog.info({
    title: t(
      "core.plugin.upload_modal.operations.existed_during_installation.title"
    ),
    description: t(
      "core.plugin.upload_modal.operations.existed_during_installation.description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await consoleApiClient.plugin.plugin.upgradePlugin({
        name: error.pluginName,
        file: file,
      });

      Toast.success(t("core.common.toast.upgrade_success"));

      window.location.reload();
    },
  });
};
</script>

<template>
  <div class="mb-3">
    <AppDownloadAlert />
  </div>

  <UppyUpload
    :restrictions="{
      maxNumberOfFiles: 1,
      allowedFileTypes: ['.jar'],
    }"
    :endpoint="endpoint"
    width="100%"
    auto-proceed
    @uploaded="onUploaded"
    @error="onError"
  />
</template>
