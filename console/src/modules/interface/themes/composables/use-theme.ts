import type { ComputedRef, Ref } from "vue";
import { computed, ref } from "vue";
import type { Theme } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import { useThemeStore } from "@/stores/theme";
import { storeToRefs } from "pinia";

interface useThemeLifeCycleReturn {
  loading: Ref<boolean>;
  isActivated: ComputedRef<boolean>;
  getFailedMessage: () => string | undefined;
  handleActiveTheme: () => void;
  handleResetSettingConfig: () => void;
}

export function useThemeLifeCycle(
  theme: Ref<Theme | undefined>
): useThemeLifeCycleReturn {
  const loading = ref(false);

  const themeStore = useThemeStore();

  const { activatedTheme } = storeToRefs(themeStore);

  const isActivated = computed(() => {
    return activatedTheme?.value?.metadata.name === theme.value?.metadata.name;
  });

  const getFailedMessage = (): string | undefined => {
    if (!(theme.value?.status?.phase === "FAILED")) {
      return;
    }

    const condition = theme.value.status.conditions?.[0];

    if (condition) {
      return [condition.type, condition.message].join("：");
    }
  };

  const handleActiveTheme = async () => {
    Dialog.info({
      title: "是否确认启用当前主题",
      description: theme.value?.spec.displayName,
      onConfirm: async () => {
        try {
          if (!theme.value) return;

          await apiClient.theme.activateTheme({
            name: theme.value?.metadata.name,
          });

          Toast.success("启用成功");
        } catch (e) {
          console.error("Failed to active theme", e);
        } finally {
          themeStore.fetchActivatedTheme();
        }
      },
    });
  };

  const handleResetSettingConfig = async () => {
    Dialog.warning({
      title: "确定要重置主题的所有配置吗？",
      description: "该操作会删除已保存的配置，重置为默认配置。",
      confirmType: "danger",
      onConfirm: async () => {
        try {
          if (!theme?.value) {
            return;
          }

          await apiClient.theme.resetThemeConfig({
            name: theme.value.metadata.name as string,
          });

          Toast.success("重置配置成功");
        } catch (e) {
          console.error("Failed to reset theme setting config", e);
        }
      },
    });
  };

  return {
    loading,
    isActivated,
    getFailedMessage,
    handleActiveTheme,
    handleResetSettingConfig,
  };
}

export function useThemeCustomTemplates(type: "post" | "page" | "category") {
  const themeStore = useThemeStore();
  const templates = computed(() => {
    const defaultTemplate = [
      {
        label: "默认模板",
        value: "",
      },
    ];

    if (!themeStore.activatedTheme) {
      return defaultTemplate;
    }
    const { customTemplates } = themeStore.activatedTheme.spec;
    if (!customTemplates?.[type]) {
      return defaultTemplate;
    }
    return [
      ...defaultTemplate,
      ...(customTemplates[type]?.map((template) => {
        return {
          value: template.file,
          label: template.name || template.file,
        };
      }) || []),
    ];
  });

  return {
    templates,
  };
}
