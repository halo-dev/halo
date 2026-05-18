<script lang="ts" setup>
import type { Theme } from "@halo-dev/api-client";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";
import { Dialog, Toast, VDropdown, VDropdownItem } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import type { AxiosError } from "axios";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    theme: Theme;
  }>(),
  {}
);

const isPossibleDevelopmentThemeConflict = (error: unknown) => {
  return (error as AxiosError).response?.status === 409;
};

const deleteThemeExtensions = async () => {
  const { settingName, configMapName } = props.theme.spec;

  if (settingName) {
    await coreApiClient.setting.deleteSetting(
      {
        name: settingName,
      },
      {
        mute: true,
      }
    );
  }

  if (configMapName) {
    await coreApiClient.configMap.deleteConfigMap(
      {
        name: configMapName,
      },
      {
        mute: true,
      }
    );
  }
};

const uninstallTheme = async (deleteExtensions?: boolean, force?: boolean) => {
  try {
    await consoleApiClient.theme.theme.deleteThemeFromConsole(
      {
        name: props.theme.metadata.name,
        force,
      },
      {
        mute: true,
      }
    );

    if (deleteExtensions) {
      await deleteThemeExtensions();
    }

    Toast.success(t("core.common.toast.uninstall_success"));
  } finally {
    queryClient.invalidateQueries({ queryKey: ["installed-themes"] });
  }
};

const confirmForceUninstall = (deleteExtensions?: boolean) => {
  Dialog.warning({
    title: t("core.theme.operations.uninstall.possible_development_title"),
    description: t(
      "core.theme.operations.uninstall.possible_development_description"
    ),
    confirmText: t("core.theme.operations.uninstall.force_confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: async () => {
      try {
        await uninstallTheme(deleteExtensions, true);
      } catch (e) {
        Toast.error(t("core.common.toast.operation_failed"));
        console.error("Failed to force uninstall theme", e);
      }
    },
  });
};

const handleUninstall = async (deleteExtensions?: boolean) => {
  Dialog.warning({
    title: `${
      deleteExtensions
        ? t("core.theme.operations.uninstall_and_delete_config.title")
        : t("core.theme.operations.uninstall.title")
    }`,
    description: t("core.common.dialog.descriptions.cannot_be_recovered"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: async () => {
      try {
        await uninstallTheme(deleteExtensions);
      } catch (e) {
        if (isPossibleDevelopmentThemeConflict(e)) {
          confirmForceUninstall(deleteExtensions);
          return;
        }

        Toast.error(t("core.common.toast.operation_failed"));
        console.error("Failed to uninstall theme", e);
      }
    },
  });
};
</script>

<template>
  <VDropdown placement="right" :triggers="['click']">
    <VDropdownItem type="danger">
      {{ $t("core.common.buttons.uninstall") }}
    </VDropdownItem>
    <template #popper>
      <VDropdownItem type="danger" @click="handleUninstall()">
        {{ $t("core.common.buttons.uninstall") }}
      </VDropdownItem>
      <VDropdownItem type="danger" @click="handleUninstall(true)">
        {{ $t("core.theme.operations.uninstall_and_delete_config.button") }}
      </VDropdownItem>
    </template>
  </VDropdown>
</template>
