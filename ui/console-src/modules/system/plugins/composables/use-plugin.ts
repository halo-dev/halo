import type { ComputedRef, Ref } from "vue";
import { computed } from "vue";
import { type Plugin, PluginStatusPhaseEnum } from "@halo-dev/api-client";
import { cloneDeep } from "lodash-es";
import { apiClient } from "@/utils/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import { useI18n } from "vue-i18n";
import { useMutation } from "@tanstack/vue-query";

interface usePluginLifeCycleReturn {
  isStarted: ComputedRef<boolean | undefined>;
  getStatusDotState: () => string;
  getStatusMessage: () => string | undefined;
  changeStatus: () => void;
  changingStatus: Ref<boolean>;
  uninstall: (deleteExtensions?: boolean) => void;
}

export function usePluginLifeCycle(
  plugin?: Ref<Plugin | undefined>
): usePluginLifeCycleReturn {
  const { t } = useI18n();

  const isStarted = computed(() => {
    return (
      plugin?.value?.status?.phase === PluginStatusPhaseEnum.Started &&
      plugin.value?.spec.enabled
    );
  });

  const getStatusDotState = () => {
    const { phase } = plugin?.value?.status || {};
    const { enabled } = plugin?.value?.spec || {};

    if (enabled && phase === PluginStatusPhaseEnum.Failed) {
      return "error";
    }

    if (phase === PluginStatusPhaseEnum.Disabling) {
      return "warning";
    }

    return "default";
  };

  const getStatusMessage = () => {
    if (!plugin?.value) return;

    const { phase } = plugin.value.status || {};

    if (
      phase === PluginStatusPhaseEnum.Failed ||
      phase === PluginStatusPhaseEnum.Disabling
    ) {
      const lastCondition = plugin.value.status?.conditions?.[0];

      return (
        [lastCondition?.reason, lastCondition?.message]
          .filter(Boolean)
          .join(": ") || "Unknown"
      );
    }

    // Starting up
    if (
      phase !== (PluginStatusPhaseEnum.Started || PluginStatusPhaseEnum.Failed)
    ) {
      return t("core.common.status.starting_up");
    }
  };

  const { isLoading: changingStatus, mutate: changeStatus } = useMutation({
    mutationKey: ["change-plugin-status"],
    mutationFn: async () => {
      if (!plugin?.value) return;

      const { enabled } = plugin.value.spec;

      return await apiClient.plugin.changePluginRunningState({
        name: plugin.value.metadata.name,
        pluginRunningStateRequest: {
          enable: !enabled,
        },
      });
    },
    retry: 3,
    retryDelay: 1000,
    onSuccess() {
      window.location.reload();
    },
  });

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
            await apiClient.extension.plugin.updatePluginHaloRunV1alpha1Plugin({
              name: pluginToUpdate.metadata.name,
              plugin: pluginToUpdate,
            });
          }

          await apiClient.extension.plugin.deletePluginHaloRunV1alpha1Plugin({
            name: plugin.value.metadata.name,
          });

          // delete plugin setting and configMap
          if (deleteExtensions) {
            const { settingName, configMapName } = plugin.value.spec;

            if (settingName) {
              await apiClient.extension.setting.deleteV1alpha1Setting(
                {
                  name: settingName,
                },
                {
                  mute: true,
                }
              );
            }

            if (configMapName) {
              await apiClient.extension.configMap.deleteV1alpha1ConfigMap(
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
    getStatusDotState,
    getStatusMessage,
    changeStatus,
    changingStatus,
    uninstall,
  };
}

export function usePluginBatchOperations(names: Ref<string[]>) {
  const { t } = useI18n();

  function handleUninstallInBatch(deleteExtensions: boolean) {
    Dialog.warning({
      title: `${
        deleteExtensions
          ? t(
              "core.plugin.operations.uninstall_and_delete_config_in_batch.title"
            )
          : t("core.plugin.operations.uninstall_in_batch.title")
      }`,
      description: t("core.common.dialog.descriptions.cannot_be_recovered"),
      confirmType: "danger",
      confirmText: t("core.common.buttons.uninstall"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: async () => {
        try {
          for (let i = 0; i < names.value.length; i++) {
            await apiClient.extension.plugin.deletePluginHaloRunV1alpha1Plugin({
              name: names.value[i],
            });

            if (deleteExtensions) {
              const { data: plugin } =
                await apiClient.extension.plugin.getPluginHaloRunV1alpha1Plugin(
                  {
                    name: names.value[i],
                  }
                );

              const { settingName, configMapName } = plugin.spec;

              if (settingName) {
                await apiClient.extension.setting.deleteV1alpha1Setting(
                  {
                    name: settingName,
                  },
                  {
                    mute: true,
                  }
                );
              }

              if (configMapName) {
                await apiClient.extension.configMap.deleteV1alpha1ConfigMap(
                  {
                    name: configMapName,
                  },
                  {
                    mute: true,
                  }
                );
              }
            }
          }

          window.location.reload();
        } catch (e) {
          console.error("Failed to uninstall plugin in batch", e);
        }
      },
    });
  }

  function handleChangeStatusInBatch(enabled: boolean) {
    Dialog.info({
      title: enabled
        ? t("core.plugin.operations.change_status_in_batch.activate_title")
        : t("core.plugin.operations.change_status_in_batch.inactivate_title"),
      confirmText: t("core.common.buttons.confirm"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: async () => {
        try {
          for (let i = 0; i < names.value.length; i++) {
            await apiClient.plugin.changePluginRunningState({
              name: names.value[i],
              pluginRunningStateRequest: {
                enable: enabled,
              },
            });
          }

          window.location.reload();
        } catch (e) {
          console.error("Failed to change plugin status in batch", e);
        }
      },
    });
  }

  return { handleUninstallInBatch, handleChangeStatusInBatch };
}
