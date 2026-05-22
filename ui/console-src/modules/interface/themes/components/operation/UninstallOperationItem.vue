<script lang="ts" setup>
import type { Theme } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { Dialog, Toast, VDropdown, VDropdownItem } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

const { t } = useI18n();
const queryClient = useQueryClient();

const props = withDefaults(
  defineProps<{
    theme: Theme;
  }>(),
  {}
);

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

const uninstallTheme = async (deleteExtensions?: boolean) => {
  try {
    await coreApiClient.theme.theme.deleteTheme(
      {
        name: props.theme.metadata.name,
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

const confirmDevelopmentThemeUninstall = (deleteExtensions?: boolean) => {
  Dialog.warning({
    title: t("core.theme.operations.uninstall.possible_development_title"),
    description: t(
      "core.theme.operations.uninstall.possible_development_description"
    ),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: async () => {
      try {
        await uninstallTheme(deleteExtensions);
      } catch (e) {
        Toast.error(t("core.common.toast.operation_failed"));
        console.error("Failed to uninstall development theme", e);
      }
    },
  });
};

const handleUninstall = async (deleteExtensions?: boolean) => {
  const isDevelopmentTheme = props.theme.status?.inDevelopment === true;

  Dialog.warning({
    title: deleteExtensions
      ? t("core.theme.operations.uninstall_and_delete_config.title")
      : t("core.theme.operations.uninstall.title"),
    description: t("core.common.dialog.descriptions.cannot_be_recovered"),
    confirmText: t("core.common.buttons.confirm"),
    cancelText: t("core.common.buttons.cancel"),
    confirmType: "danger",
    onConfirm: async () => {
      if (isDevelopmentTheme) {
        confirmDevelopmentThemeUninstall(deleteExtensions);
        return;
      }

      try {
        await uninstallTheme(deleteExtensions);
      } catch (e) {
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
