<script lang="ts" setup>
import { VModal, Dialog, Toast } from "@halo-dev/components";
import UppyUpload from "@/components/upload/UppyUpload.vue";
import { apiClient } from "@/utils/api-client";
import type { Plugin } from "@halo-dev/api-client";
import { computed, ref, watch } from "vue";
import type { SuccessResponse, ErrorResponse } from "@uppy/core";
import type { UppyFile } from "@uppy/utils";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    visible: boolean;
    upgradePlugin?: Plugin;
  }>(),
  {
    visible: false,
    upgradePlugin: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const uploadVisible = ref(false);

const modalTitle = computed(() => {
  return props.upgradePlugin
    ? t("core.plugin.upload_modal.titles.upgrade", {
        display_name: props.upgradePlugin.spec.displayName,
      })
    : t("core.plugin.upload_modal.titles.install");
});

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const endpoint = computed(() => {
  if (props.upgradePlugin) {
    return `/apis/api.console.halo.run/v1alpha1/plugins/${props.upgradePlugin.metadata.name}/upgrade`;
  }
  return "/apis/api.console.halo.run/v1alpha1/plugins/install";
});

const onUploaded = async (response: SuccessResponse) => {
  if (props.upgradePlugin) {
    Toast.success(t("core.common.toast.upgrade_success"));
    window.location.reload();
    return;
  }

  const plugin = response.body as Plugin;
  handleVisibleChange(false);
  Dialog.success({
    title: t("core.plugin.upload_modal.operations.active_after_install.title"),
    description: t(
      "core.plugin.upload_modal.operations.active_after_install.description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      try {
        const { data: pluginToUpdate } =
          await apiClient.extension.plugin.getpluginHaloRunV1alpha1Plugin({
            name: plugin.metadata.name,
          });
        pluginToUpdate.spec.enabled = true;

        await apiClient.extension.plugin.updatepluginHaloRunV1alpha1Plugin({
          name: pluginToUpdate.metadata.name,
          plugin: pluginToUpdate,
        });

        window.location.reload();
      } catch (e) {
        console.error(e);
      }
    },
  });
};

interface PluginInstallationErrorResponse {
  detail: string;
  instance: string;
  pluginName: string;
  requestId: string;
  status: number;
  timestamp: string;
  title: string;
  type: string;
}

const PLUGIN_ALREADY_EXISTS_TYPE =
  "https://halo.run/probs/plugin-alreay-exists";

const onError = (file: UppyFile<unknown>, response: ErrorResponse) => {
  const body = response.body as PluginInstallationErrorResponse;
  if (body.type === PLUGIN_ALREADY_EXISTS_TYPE) {
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
        await apiClient.plugin.upgradePlugin({
          name: body.pluginName,
          file: file.data as File,
        });

        Toast.success(t("core.common.toast.upgrade_success"));

        window.location.reload();
      },
    });
  }
};

watch(
  () => props.visible,
  (newValue) => {
    if (newValue) {
      uploadVisible.value = true;
    } else {
      const uploadVisibleTimer = setTimeout(() => {
        uploadVisible.value = false;
        clearTimeout(uploadVisibleTimer);
      }, 200);
    }
  }
);
</script>
<template>
  <VModal
    :visible="visible"
    :width="600"
    :title="modalTitle"
    @update:visible="handleVisibleChange"
  >
    <UppyUpload
      v-if="uploadVisible"
      :restrictions="{
        maxNumberOfFiles: 1,
        allowedFileTypes: ['.jar'],
      }"
      :endpoint="endpoint"
      auto-proceed
      @uploaded="onUploaded"
      @error="onError"
    />
  </VModal>
</template>
