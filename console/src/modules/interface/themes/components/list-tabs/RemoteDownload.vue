<script lang="ts" setup>
import { apiClient } from "@/utils/api-client";
import { Dialog, Toast, VButton } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import type { Ref } from "vue";
import { inject } from "vue";
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import type { ThemeInstallationErrorResponse } from "../../types";
import { useThemeStore } from "@/stores/theme";
import { THEME_ALREADY_EXISTS_TYPE } from "../../constants";
import { useRouteQuery } from "@vueuse/router";
import { onMounted } from "vue";
import { nextTick } from "vue";
import { submitForm } from "@formkit/core";

const { t } = useI18n();
const queryClient = useQueryClient();
const themeStore = useThemeStore();

const activeTabId = inject<Ref<string>>("activeTabId", ref(""));
const remoteDownloadUrl = ref("");
const downloading = ref(false);

const handleDownloadTheme = async () => {
  try {
    downloading.value = true;

    await apiClient.theme.installThemeFromUri({
      installFromUriRequest: {
        uri: remoteDownloadUrl.value,
      },
    });

    Toast.success(t("core.common.toast.install_success"));

    queryClient.invalidateQueries({ queryKey: ["themes"] });
    themeStore.fetchActivatedTheme();

    activeTabId.value = "installed";

    // eslint-disable-next-line
  } catch (error: any) {
    const data = error?.response.data as ThemeInstallationErrorResponse;
    if (data?.type === THEME_ALREADY_EXISTS_TYPE) {
      handleCatchExistsException(data);
    }

    console.error("Failed to download theme", error);
  } finally {
    routeRemoteDownloadUrl.value = null;
    downloading.value = false;
  }
};

const handleCatchExistsException = async (
  error: ThemeInstallationErrorResponse
) => {
  Dialog.info({
    title: t("core.theme.operations.existed_during_installation.title"),
    description: t(
      "core.theme.operations.existed_during_installation.description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      await apiClient.theme.upgradeThemeFromUri({
        name: error.themeName,
        upgradeFromUriRequest: {
          uri: remoteDownloadUrl.value,
        },
      });

      Toast.success(t("core.common.toast.upgrade_success"));

      queryClient.invalidateQueries({ queryKey: ["themes"] });
      themeStore.fetchActivatedTheme();

      activeTabId.value = "installed";
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
      submitForm("theme-remote-download-form");
    });
  }
});
</script>

<template>
  <FormKit
    id="theme-remote-download-form"
    name="theme-remote-download-form"
    type="form"
    :preserve="true"
    @submit="handleDownloadTheme"
  >
    <FormKit
      v-model="remoteDownloadUrl"
      :label="$t('core.theme.list_modal.tabs.remote_download.fields.url')"
      type="text"
      validation="required"
    ></FormKit>
  </FormKit>

  <div class="pt-5">
    <VButton
      :loading="downloading"
      type="secondary"
      @click="$formkit.submit('theme-remote-download-form')"
    >
      {{ $t("core.common.buttons.download") }}
    </VButton>
  </div>
</template>
