import type { ComputedRef, Ref } from "vue";
import { computed } from "vue";
import type { Plugin } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import { Dialog, Toast } from "@halo-dev/components";

interface usePluginLifeCycleReturn {
  isStarted: ComputedRef<boolean | undefined>;
  changeStatus: () => void;
  uninstall: (deleteExtensions?: boolean) => void;
}

export function usePluginLifeCycle(
  plugin?: Ref<Plugin | undefined>
): usePluginLifeCycleReturn {
  const isStarted = computed(() => {
    return (
      plugin?.value?.status?.phase === "STARTED" && plugin.value?.spec.enabled
    );
  });

  const changeStatus = () => {
    if (!plugin?.value) return;

    const pluginToUpdate = cloneDeep(plugin.value);

    Dialog.info({
      title: `确定要${pluginToUpdate.spec.enabled ? "停止" : "启动"}该插件吗？`,
      onConfirm: async () => {
        try {
          pluginToUpdate.spec.enabled = !pluginToUpdate.spec.enabled;
          await apiClient.extension.plugin.updatepluginHaloRunV1alpha1Plugin({
            name: pluginToUpdate.metadata.name,
            plugin: pluginToUpdate,
          });

          Toast.success(`${pluginToUpdate.spec.enabled ? "启动" : "停止"}成功`);
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
          ? "确定要卸载该插件以及对应的配置吗？"
          : "确定要卸载该插件吗？"
      }`,
      description: `${
        enabled
          ? "当前插件还在启用状态，将在停止运行后卸载，该操作不可恢复。"
          : "该操作不可恢复。"
      }`,
      confirmType: "danger",
      confirmText: `${enabled ? "停止运行并卸载" : "卸载"}`,
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

          Toast.success("卸载成功");
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
