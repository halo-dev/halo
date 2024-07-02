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
    onConfirm: async () => {
      try {
        await coreApiClient.theme.theme.deleteTheme({
          name: props.theme.metadata.name,
        });

        // delete theme setting and configMap
        if (deleteExtensions) {
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
        }

        Toast.success(t("core.common.toast.uninstall_success"));
      } catch (e) {
        console.error("Failed to uninstall theme", e);
      } finally {
        queryClient.invalidateQueries({ queryKey: ["installed-themes"] });
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
