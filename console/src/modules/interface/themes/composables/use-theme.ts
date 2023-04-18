import type { ComputedRef, Ref } from "vue";
import { computed, ref } from "vue";
import type { Theme } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import { useThemeStore } from "@/stores/theme";
import { storeToRefs } from "pinia";
import { useI18n } from "vue-i18n";

interface useThemeLifeCycleReturn {
  loading: Ref<boolean>;
  isActivated: ComputedRef<boolean>;
  getFailedMessage: () => string | undefined;
  handleActiveTheme: (reload?: boolean) => void;
  handleResetSettingConfig: () => void;
}

export function useThemeLifeCycle(
  theme: Ref<Theme | undefined>
): useThemeLifeCycleReturn {
  const { t } = useI18n();

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

  const handleActiveTheme = async (reload?: boolean) => {
    Dialog.info({
      title: t("core.theme.operations.active.title"),
      description: theme.value?.spec.displayName,
      confirmText: t("core.common.buttons.confirm"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: async () => {
        try {
          if (!theme.value) return;

          await apiClient.theme.activateTheme({
            name: theme.value?.metadata.name,
          });

          Toast.success(t("core.theme.operations.active.toast_success"));

          if (reload) {
            window.location.reload();
          }
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
      title: t("core.theme.operations.reset.title"),
      description: t("core.theme.operations.reset.description"),
      confirmType: "danger",
      confirmText: t("core.common.buttons.confirm"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: async () => {
        try {
          if (!theme?.value) {
            return;
          }

          await apiClient.theme.resetThemeConfig({
            name: theme.value.metadata.name as string,
          });

          Toast.success(t("core.theme.operations.reset.toast_success"));
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
  const { t } = useI18n();

  const templates = computed(() => {
    const defaultTemplate = [
      {
        label: t("core.theme.custom_templates.default"),
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
