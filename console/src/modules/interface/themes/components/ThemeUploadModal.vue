<script lang="ts" setup>
import {
  Dialog,
  Toast,
  VButton,
  VModal,
  VTabItem,
  VTabs,
} from "@halo-dev/components";
import UppyUpload from "@/components/upload/UppyUpload.vue";
import { computed, ref, watch, nextTick } from "vue";
import type { Theme } from "@halo-dev/api-client";
import { useI18n } from "vue-i18n";
import { useQueryClient } from "@tanstack/vue-query";
import { useThemeStore } from "@/stores/theme";
import { apiClient } from "@/utils/api-client";
import { useRouteQuery } from "@vueuse/router";
import { submitForm } from "@formkit/core";
import type { ErrorResponse, UppyFile } from "@uppy/core";
import AppDownloadAlert from "@/components/common/AppDownloadAlert.vue";

const { t } = useI18n();
const queryClient = useQueryClient();
const themeStore = useThemeStore();

const props = withDefaults(
  defineProps<{
    visible: boolean;
    upgradeTheme?: Theme;
  }>(),
  {
    visible: false,
    upgradeTheme: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const uploadVisible = ref(false);

const modalTitle = computed(() => {
  return props.upgradeTheme
    ? t("core.theme.upload_modal.titles.upgrade", {
        display_name: props.upgradeTheme.spec.displayName,
      })
    : t("core.theme.upload_modal.titles.install");
});

const handleVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const endpoint = computed(() => {
  if (props.upgradeTheme) {
    return `/apis/api.console.halo.run/v1alpha1/themes/${props.upgradeTheme.metadata.name}/upgrade`;
  }
  return "/apis/api.console.halo.run/v1alpha1/themes/install";
});

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

const onUploaded = () => {
  Toast.success(
    t(
      props.upgradeTheme
        ? "core.common.toast.upgrade_success"
        : "core.common.toast.install_success"
    )
  );

  queryClient.invalidateQueries({ queryKey: ["themes"] });
  themeStore.fetchActivatedTheme();

  handleVisibleChange(false);
};

interface ThemeInstallationErrorResponse {
  detail: string;
  instance: string;
  themeName: string;
  requestId: string;
  status: number;
  timestamp: string;
  title: string;
  type: string;
}

const THEME_ALREADY_EXISTS_TYPE = "https://halo.run/probs/theme-alreay-exists";

const onError = (file: UppyFile<unknown>, response: ErrorResponse) => {
  const body = response.body as ThemeInstallationErrorResponse;

  if (body.type === THEME_ALREADY_EXISTS_TYPE) {
    handleCatchExistsException(body, file.data as File);
  }
};

const handleCatchExistsException = async (
  error: ThemeInstallationErrorResponse,
  file?: File
) => {
  Dialog.info({
    title: t(
      "core.theme.upload_modal.operations.existed_during_installation.title"
    ),
    description: t(
      "core.theme.upload_modal.operations.existed_during_installation.description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    onConfirm: async () => {
      if (activeTabId.value === "local") {
        if (!file) {
          throw new Error("File is required");
        }

        await apiClient.theme.upgradeTheme({
          name: error.themeName,
          file: file,
        });
      } else if (activeTabId.value === "remote") {
        await apiClient.theme.upgradeThemeFromUri({
          name: error.themeName,
          upgradeFromUriRequest: {
            uri: remoteDownloadUrl.value,
          },
        });
      } else {
        throw new Error("Unknown tab id");
      }

      Toast.success(t("core.common.toast.upgrade_success"));

      queryClient.invalidateQueries({ queryKey: ["themes"] });
      themeStore.fetchActivatedTheme();

      handleVisibleChange(false);
    },
  });
};

// remote download
const activeTabId = ref("local");
const remoteDownloadUrl = ref("");
const downloading = ref(false);

const handleDownloadTheme = async () => {
  try {
    downloading.value = true;

    if (props.upgradeTheme) {
      await apiClient.theme.upgradeThemeFromUri({
        name: props.upgradeTheme.metadata.name,
        upgradeFromUriRequest: {
          uri: remoteDownloadUrl.value,
        },
      });
    } else {
      await apiClient.theme.installThemeFromUri({
        installFromUriRequest: {
          uri: remoteDownloadUrl.value,
        },
      });
    }

    Toast.success(
      t(
        props.upgradeTheme
          ? "core.common.toast.upgrade_success"
          : "core.common.toast.install_success"
      )
    );

    queryClient.invalidateQueries({ queryKey: ["themes"] });
    themeStore.fetchActivatedTheme();

    handleVisibleChange(false);

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

// handle remote download url from route
const routeRemoteDownloadUrl = useRouteQuery<string | null>(
  "remote-download-url"
);
watch(
  () => props.visible,
  (visible) => {
    if (routeRemoteDownloadUrl.value && visible) {
      activeTabId.value = "remote";
      remoteDownloadUrl.value = routeRemoteDownloadUrl.value as string;
      nextTick(() => {
        submitForm("theme-remote-download-form");
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
      <VTabItem id="local" :label="$t('core.theme.upload_modal.tabs.local')">
        <div class="pb-3">
          <AppDownloadAlert />
        </div>
        <UppyUpload
          v-if="uploadVisible"
          :restrictions="{
            maxNumberOfFiles: 1,
            allowedFileTypes: ['.zip'],
          }"
          :endpoint="endpoint"
          auto-proceed
          @uploaded="onUploaded"
          @error="onError"
        />
      </VTabItem>
      <VTabItem
        id="remote"
        :label="$t('core.theme.upload_modal.tabs.remote.title')"
      >
        <FormKit
          id="theme-remote-download-form"
          name="theme-remote-download-form"
          type="form"
          :preserve="true"
          @submit="handleDownloadTheme"
        >
          <FormKit
            v-model="remoteDownloadUrl"
            :label="$t('core.theme.upload_modal.tabs.remote.fields.url')"
            type="text"
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
      </VTabItem>
    </VTabs>
  </VModal>
</template>
