<script lang="ts" setup>
import {
  VModal,
  Dialog,
  Toast,
  VTabs,
  VTabItem,
  VButton,
} from "@halo-dev/components";
import UppyUpload from "@/components/upload/UppyUpload.vue";
import { apiClient } from "@/utils/api-client";
import type { Plugin } from "@halo-dev/api-client";
import { computed, ref, watch, nextTick } from "vue";
import type { SuccessResponse, ErrorResponse, UppyFile } from "@uppy/core";
import { useI18n } from "vue-i18n";
import { useQueryClient } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import { submitForm } from "@formkit/core";
import AppDownloadAlert from "@/components/common/AppDownloadAlert.vue";

const { t } = useI18n();
const queryClient = useQueryClient();

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

  handleVisibleChange(false);
  queryClient.invalidateQueries({ queryKey: ["plugins"] });

  handleShowActiveModalAfterInstall(response.body as Plugin);
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
    handleCatchExistsException(body, file.data as File);
  }
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
      if (activeTabId.value === "local") {
        await apiClient.plugin.upgradePlugin({
          name: error.pluginName,
          file: file,
        });
      } else if (activeTabId.value === "remote") {
        await apiClient.plugin.upgradePluginFromUri({
          name: error.pluginName,
          upgradeFromUriRequest: {
            uri: remoteDownloadUrl.value,
          },
        });
      } else {
        throw new Error("Unknown tab id");
      }

      Toast.success(t("core.common.toast.upgrade_success"));

      window.location.reload();
    },
  });
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

// remote download
const activeTabId = ref("local");
const remoteDownloadUrl = ref("");
const downloading = ref(false);

const handleDownloadPlugin = async () => {
  try {
    downloading.value = true;
    if (props.upgradePlugin) {
      await apiClient.plugin.upgradePluginFromUri({
        name: props.upgradePlugin.metadata.name,
        upgradeFromUriRequest: {
          uri: remoteDownloadUrl.value,
        },
      });

      Toast.success(t("core.common.toast.upgrade_success"));
      window.location.reload();
      return;
    }

    const { data: plugin } = await apiClient.plugin.installPluginFromUri({
      installFromUriRequest: {
        uri: remoteDownloadUrl.value,
      },
    });

    handleVisibleChange(false);
    queryClient.invalidateQueries({ queryKey: ["plugins"] });

    handleShowActiveModalAfterInstall(plugin);

    // eslint-disable-next-line
  } catch (error: any) {
    const data = error?.response.data as PluginInstallationErrorResponse;
    if (data?.type === PLUGIN_ALREADY_EXISTS_TYPE) {
      handleCatchExistsException(data);
    }

    console.error("Failed to download plugin", error);
  } finally {
    routeRemoteDownloadUrl.value = null;
    downloading.value = false;
  }
};

// handle remote download url from route
const routeRemoteDownloadUrl = useRouteQuery<string | null>(
  "remote-download-url"
);
watch(
  () => props.visible,
  (visible) => {
    if (routeRemoteDownloadUrl.value && visible) {
      activeTabId.value = "remote";
      remoteDownloadUrl.value = routeRemoteDownloadUrl.value;
      nextTick(() => {
        submitForm("plugin-remote-download-form");
      });
    }
  }
);
</script>
<template>
  <VModal
    :visible="visible"
    :width="600"
    :title="modalTitle"
    :centered="false"
    @update:visible="handleVisibleChange"
  >
    <VTabs v-model:active-id="activeTabId" type="outline" class="!rounded-none">
      <VTabItem id="local" :label="$t('core.plugin.upload_modal.tabs.local')">
        <div class="pb-3">
          <AppDownloadAlert />
        </div>

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
      </VTabItem>
      <VTabItem
        id="remote"
        :label="$t('core.plugin.upload_modal.tabs.remote.title')"
      >
        <FormKit
          id="plugin-remote-download-form"
          name="plugin-remote-download-form"
          type="form"
          :preserve="true"
          @submit="handleDownloadPlugin"
        >
          <FormKit
            v-model="remoteDownloadUrl"
            :label="$t('core.plugin.upload_modal.tabs.remote.fields.url')"
            type="text"
          ></FormKit>
        </FormKit>

        <div class="pt-5">
          <VButton
            :loading="downloading"
            type="secondary"
            @click="$formkit.submit('plugin-remote-download-form')"
          >
            {{ $t("core.common.buttons.download") }}
          </VButton>
        </div>
      </VTabItem>
    </VTabs>
  </VModal>
</template>
