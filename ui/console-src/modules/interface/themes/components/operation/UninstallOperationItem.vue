<script lang="ts" setup>
import { Dialog, Toast, VDropdown, VDropdownItem } from "@halo-dev/components";
import type { Theme } from "@halo-dev/api-client";
import { useI18n } from "vue-i18n";
import { apiClient } from "@/utils/api-client";
import { useQueryClient } from "@tanstack/vue-query";

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
        await apiClient.extension.theme.deletethemeHaloRunV1alpha1Theme({
          name: props.theme.metadata.name,
        });

        // delete theme setting and configMap
        if (deleteExtensions) {
          const { settingName, configMapName } = props.theme.spec;

          if (settingName) {
            await apiClient.extension.setting.deletev1alpha1Setting(
              {
                name: settingName,
              },
              {
                mute: true,
              }
            );
          }

          if (configMapName) {
            await apiClient.extension.configMap.deletev1alpha1ConfigMap(
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
