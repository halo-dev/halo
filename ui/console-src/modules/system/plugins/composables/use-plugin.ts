import { usePluginModuleStore } from "@/stores/plugin";
import { usePermission } from "@/utils/permission";
import {
  PluginStatusPhaseEnum,
  consoleApiClient,
  coreApiClient,
  type Plugin,
  type SettingForm,
} from "@halo-dev/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import type { PluginTab } from "@halo-dev/console-shared";
import { useMutation, useQuery } from "@tanstack/vue-query";
import { useRouteQuery } from "@vueuse/router";
import type { ComputedRef, Ref } from "vue";
import { computed, markRaw, ref } from "vue";
import { useI18n } from "vue-i18n";
import DetailTab from "../components/tabs/Detail.vue";
import SettingTab from "../components/tabs/Setting.vue";

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

      return await consoleApiClient.plugin.plugin.changePluginRunningState({
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
          await consoleApiClient.plugin.plugin.changePluginRunningState({
            name: plugin.value.metadata.name,
            pluginRunningStateRequest: {
              enable: false,
            },
          });

          await coreApiClient.plugin.plugin.deletePlugin({
            name: plugin.value.metadata.name,
          });

          // delete plugin setting and configMap
          if (deleteExtensions) {
            const { settingName, configMapName } = plugin.value.spec;

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
            await coreApiClient.plugin.plugin.deletePlugin({
              name: names.value[i],
            });

            if (deleteExtensions) {
              const { data: plugin } =
                await coreApiClient.plugin.plugin.getPlugin({
                  name: names.value[i],
                });

              const { settingName, configMapName } = plugin.spec;

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
            await consoleApiClient.plugin.plugin.changePluginRunningState({
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

export function usePluginDetailTabs(
  pluginName: Ref<string | undefined>,
  recordsActiveTab: boolean
) {
  const { currentUserHasPermission } = usePermission();
  const { t } = useI18n();

  const initialTabs = [
    {
      id: "detail",
      label: t("core.plugin.tabs.detail"),
      component: markRaw(DetailTab),
    },
  ];

  const tabs = ref<PluginTab[]>(initialTabs);
  const activeTab = recordsActiveTab
    ? useRouteQuery<string>("tab", tabs.value[0].id)
    : ref(tabs.value[0].id);

  const { data: plugin } = useQuery({
    queryKey: ["plugin", pluginName],
    queryFn: async () => {
      const { data } = await coreApiClient.plugin.plugin.getPlugin({
        name: pluginName.value as string,
      });
      return data;
    },
    async onSuccess(data) {
      if (
        !data.spec.settingName ||
        !currentUserHasPermission(["system:plugins:manage"])
      ) {
        tabs.value = [...initialTabs, ...(await getTabsFromExtensions())];
      }
    },
  });

  const { data: setting } = useQuery({
    queryKey: ["plugin-setting", plugin],
    queryFn: async () => {
      const { data } = await consoleApiClient.plugin.plugin.fetchPluginSetting({
        name: plugin.value?.metadata.name as string,
      });
      return data;
    },
    enabled: computed(() => {
      return (
        !!plugin.value &&
        !!plugin.value.spec.settingName &&
        currentUserHasPermission(["system:plugins:manage"])
      );
    }),
    async onSuccess(data) {
      if (data) {
        const { forms } = data.spec;
        tabs.value = [
          ...initialTabs,
          ...(await getTabsFromExtensions()),
          ...forms.map((item: SettingForm) => {
            return {
              id: item.group,
              label: item.label || "",
              component: markRaw(SettingTab),
            };
          }),
        ] as PluginTab[];
      }
    },
  });

  async function getTabsFromExtensions() {
    const { pluginModuleMap } = usePluginModuleStore();

    const currentPluginModule = pluginModuleMap[pluginName.value as string];

    if (!currentPluginModule) {
      return [];
    }

    const callbackFunction =
      currentPluginModule?.extensionPoints?.["plugin:self:tabs:create"];

    if (typeof callbackFunction !== "function") {
      return [];
    }

    const pluginTabs = await callbackFunction();

    return pluginTabs.filter((tab) => {
      return currentUserHasPermission(tab.permissions);
    });
  }

  return {
    plugin,
    setting,
    tabs,
    activeTab,
  };
}
