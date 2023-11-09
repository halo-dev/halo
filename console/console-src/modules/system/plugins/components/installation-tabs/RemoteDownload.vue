<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { Dialog, Toast, VButton } from "@halo-dev/components";
import type { Plugin } from "@halo-dev/api-client";
import type { Ref } from "vue";
import { inject } from "vue";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import { useQueryClient } from "@tanstack/vue-query";
import type { PluginInstallationErrorResponse } from "../../types";
import { PLUGIN_ALREADY_EXISTS_TYPE } from "../../constants";
import { useRouteQuery } from "@vueuse/router";
import { onMounted } from "vue";
import { nextTick } from "vue";
import { submitForm } from "@formkit/core";

const emit = defineEmits<{
  (event: "close-modal"): void;
}>();

const { t } = useI18n();
const queryClient = useQueryClient();

const pluginToUpgrade = inject<Ref<Plugin | undefined>>(
  "pluginToUpgrade",
  ref()
);

const remoteDownloadUrl = ref("");
const downloading = ref(false);

const handleDownloadPlugin = async () => {
  try {
    downloading.value = true;
    if (pluginToUpgrade.value) {
      await apiClient.plugin.upgradePluginFromUri({
        name: pluginToUpgrade.value.metadata.name,
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

    emit("close-modal");
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

const handleCatchExistsException = async (
  error: PluginInstallationErrorResponse
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
      await apiClient.plugin.upgradePluginFromUri({
        name: error.pluginName,
        upgradeFromUriRequest: {
          uri: remoteDownloadUrl.value,
        },
      });

      Toast.success(t("core.common.toast.upgrade_success"));

      window.location.reload();
    },
  });
};

// handle remote download url from route
const routeRemoteDownloadUrl = useRouteQuery<string | null>(
  "remote-download-url"
);

onMounted(() => {
  if (routeRemoteDownloadUrl.value) {
    remoteDownloadUrl.value = routeRemoteDownloadUrl.value;
    nextTick(() => {
      submitForm("plugin-remote-download-form");
    });
  }
});
</script>

<template>
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
      validation="required"
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
</template>
