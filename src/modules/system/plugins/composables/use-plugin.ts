import type { ComputedRef, Ref } from "vue";
import { computed } from "vue";
import type { Plugin } from "@halo-dev/api-client";
import cloneDeep from "lodash.clonedeep";
import { apiClient } from "@/utils/api-client";
import { useDialog } from "@halo-dev/components";

interface usePluginLifeCycleReturn {
  isStarted: ComputedRef<boolean | undefined>;
  changeStatus: () => void;
  uninstall: () => void;
}

export function usePluginLifeCycle(
  plugin?: Ref<Plugin | undefined>
): usePluginLifeCycleReturn {
  const dialog = useDialog();

  const isStarted = computed(() => {
    return (
      plugin?.value?.status?.phase === "STARTED" && plugin.value?.spec.enabled
    );
  });

  const changeStatus = () => {
    if (!plugin?.value) return;

    const pluginToUpdate = cloneDeep(plugin.value);

    dialog.info({
      title: `确定要${pluginToUpdate.spec.enabled ? "停止" : "启动"}该插件吗？`,
      onConfirm: async () => {
        try {
          pluginToUpdate.spec.enabled = !pluginToUpdate.spec.enabled;
          await apiClient.extension.plugin.updatepluginHaloRunV1alpha1Plugin({
            name: pluginToUpdate.metadata.name,
            plugin: pluginToUpdate,
          });
        } catch (e) {
          console.error(e);
        } finally {
          window.location.reload();
        }
      },
    });
  };

  const uninstall = () => {
    if (!plugin?.value) return;

    const { enabled } = plugin.value.spec;

    dialog.warning({
      title: `确定要卸载该插件吗？`,
      description: `${
        enabled ? "当前插件还在启用状态，将在停止运行后卸载。" : ""
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
        } catch (e) {
          console.error(e);
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
