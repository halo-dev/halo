import type { ComputedRef, Ref } from "vue";
import { computed } from "vue";
import type { Plugin } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import { useI18n } from "vue-i18n";

interface usePluginLifeCycleReturn {
  isStarted: ComputedRef<boolean | undefined>;
  changeStatus: () => void;
  uninstall: (deleteExtensions?: boolean) => void;
}

export function usePluginLifeCycle(
  plugin?: Ref<Plugin | undefined>
): usePluginLifeCycleReturn {
  const { t } = useI18n();

  const isStarted = computed(() => {
    return (
      plugin?.value?.status?.phase === "STARTED" && plugin.value?.spec.enabled
    );
  });

  const changeStatus = () => {
    if (!plugin?.value) return;

    const pluginToUpdate = cloneDeep(plugin.value);

    Dialog.info({
      title: pluginToUpdate.spec.enabled
        ? t("core.plugin.operations.change_status.inactive_title")
        : t("core.plugin.operations.change_status.active_title"),
      confirmText: t("core.common.buttons.confirm"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: async () => {
        try {
          pluginToUpdate.spec.enabled = !pluginToUpdate.spec.enabled;
          await apiClient.extension.plugin.updatepluginHaloRunV1alpha1Plugin({
            name: pluginToUpdate.metadata.name,
            plugin: pluginToUpdate,
          });

          Toast.success(
            pluginToUpdate.spec.enabled
              ? t("core.common.toast.active_success")
              : t("core.common.toast.inactive_success")
          );
        } catch (e) {
          console.error(e);
        } finally {
          window.location.reload();
        }
      },
    });
  };

  const uninstall = (deleteExtensions?: boolean) => {
    if (!plugin?.value) return;

    const { enabled } = plugin.value.spec;

    Dialog.warning({
      title: `${
        deleteExtensions
          ? t("core.plugin.operations.uninstall_and_delete_config.title")
          : t("core.plugin.operations.uninstall.title")
      }`,
      description: `${
        enabled
          ? t("core.plugin.operations.uninstall_when_enabled.description")
          : t("core.common.dialog.descriptions.cannot_be_recovered")
      }`,
      confirmType: "danger",
      confirmText: `${
        enabled
          ? t("core.plugin.operations.uninstall_when_enabled.confirm_text")
          : t("core.common.buttons.uninstall")
      }`,
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: async () => {
        if (!plugin.value) return;

        try {
          if (enabled) {
            const pluginToUpdate = cloneDeep(plugin.value);
            pluginToUpdate.spec.enabled = false;
            await apiClient.extension.plugin.updatepluginHaloRunV1alpha1Plugin({
              name: pluginToUpdate.metadata.name,
              plugin: pluginToUpdate,
            });
          }

          await apiClient.extension.plugin.deletepluginHaloRunV1alpha1Plugin({
            name: plugin.value.metadata.name,
          });

          // delete plugin setting and configMap
          if (deleteExtensions) {
            const { settingName, configMapName } = plugin.value.spec;

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
          console.error("Failed to uninstall plugin", e);
        } finally {
          window.location.reload();
        }
      },
    });
  };

  return {
    isStarted,
    changeStatus,
    uninstall,
  };
}
