import type { ComputedRef, Ref } from "vue";
import { computed, onMounted, ref } from "vue";
import type { Theme } from "@halo-dev/api-client";
import { apiClient } from "@halo-dev/admin-shared";
import { useDialog } from "@halo-dev/components";

interface useThemeLifeCycleReturn {
  loading: Ref<boolean>;
  activatedTheme: Ref<Theme | undefined>;
  isActivated: ComputedRef<boolean>;
  handleActiveTheme: () => void;
}

export function useThemeLifeCycle(
  theme: Ref<Theme | undefined>
): useThemeLifeCycleReturn {
  const activatedTheme = ref<Theme | undefined>();
  const loading = ref(false);

  const isActivated = computed(() => {
    return activatedTheme.value?.metadata.name === theme.value?.metadata.name;
  });

  const dialog = useDialog();

  const handleFetchActivatedTheme = async () => {
    try {
      loading.value = true;

      const { data } = await apiClient.extension.configMap.getv1alpha1ConfigMap(
        {
          name: "system",
        }
      );

      if (!data.data?.theme) {
        // Todo: show error
        return;
      }
      const themeConfig = JSON.parse(data.data.theme);

      const { data: themeData } =
        await apiClient.extension.theme.getthemeHaloRunV1alpha1Theme({
          name: themeConfig.active,
        });

      theme.value = themeData;
      activatedTheme.value = themeData;
    } catch (e) {
      console.error("Failed to fetch active theme", e);
    } finally {
      loading.value = false;
    }
  };

  const handleActiveTheme = async () => {
    dialog.info({
      title: "是否确认启用当前主题",
      description: theme.value?.spec.displayName,
      onConfirm: async () => {
        try {
          const { data: systemConfigMap } =
            await apiClient.extension.configMap.getv1alpha1ConfigMap({
              name: "system",
            });

          if (systemConfigMap.data) {
            const themeConfigToUpdate = JSON.parse(
              systemConfigMap.data?.theme || "{}"
            );
            themeConfigToUpdate.active = theme.value?.metadata.name;
            systemConfigMap.data["theme"] = JSON.stringify(themeConfigToUpdate);

            await apiClient.extension.configMap.updatev1alpha1ConfigMap({
              name: "system",
              configMap: systemConfigMap,
            });
          }
        } catch (e) {
          console.error("Failed to active theme", e);
        } finally {
          await handleFetchActivatedTheme();
        }
      },
    });
  };

  onMounted(handleFetchActivatedTheme);

  return {
    loading,
    activatedTheme,
    isActivated,
    handleActiveTheme,
  };
}
