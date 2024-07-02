import { useThemeStore } from "@console/stores/theme";
import type { Theme } from "@halo-dev/api-client";
import { consoleApiClient } from "@halo-dev/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import { useFileDialog } from "@vueuse/core";
import { storeToRefs } from "pinia";
import type { ComputedRef, Ref } from "vue";
import { computed, ref } from "vue";
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

          await consoleApiClient.theme.theme.activateTheme({
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

          await consoleApiClient.theme.theme.resetThemeConfig({
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

interface ExportData {
  themeName: string;
  version: string;
  settingName: string;
  configMapName: string;
  configs: { [key: string]: string };
}

export function useThemeConfigFile(theme: Ref<Theme | undefined>) {
  const { t } = useI18n();

  const handleExportThemeConfiguration = async () => {
    if (!theme.value) {
      console.error("No selected or activated theme");
      return;
    }

    const { data } = await consoleApiClient.theme.theme.fetchThemeConfig({
      name: theme?.value?.metadata.name as string,
    });
    if (!data) {
      console.error("Failed to fetch theme config");
      return;
    }

    const themeName = theme.value.metadata.name;
    const exportData = {
      themeName: themeName,
      version: theme.value.spec.version,
      settingName: theme.value.spec.settingName,
      configMapName: theme.value.spec.configMapName,
      configs: data.data,
    } as ExportData;
    const exportStr = JSON.stringify(exportData, null, 2);
    const blob = new Blob([exportStr], { type: "application/json" });
    const temporaryExportUrl = URL.createObjectURL(blob);
    const temporaryLinkTag = document.createElement("a");

    temporaryLinkTag.href = temporaryExportUrl;
    temporaryLinkTag.download = `export-${themeName}-config-${Date.now().toString()}.json`;

    document.body.appendChild(temporaryLinkTag);
    temporaryLinkTag.click();

    document.body.removeChild(temporaryLinkTag);
    URL.revokeObjectURL(temporaryExportUrl);
  };

  const {
    open: openSelectImportFileDialog,
    onChange: handleImportThemeConfiguration,
  } = useFileDialog({
    accept: "application/json",
    multiple: false,
    directory: false,
    reset: true,
  });

  handleImportThemeConfiguration(async (files) => {
    if (files === null || files.length === 0) {
      return;
    }
    const configText = await files[0].text();
    const configJson = JSON.parse(configText || "{}");
    if (!configJson.configs) {
      return;
    }
    if (!configJson.themeName || !configJson.version) {
      Toast.error(
        t("core.theme.operations.import_configuration.invalid_format")
      );
      return;
    }
    if (!theme.value) {
      console.error("No selected or activated theme");
      return;
    }
    if (configJson.themeName !== theme.value.metadata.name) {
      Toast.error(
        t("core.theme.operations.import_configuration.mismatched_theme")
      );
      return;
    }

    if (configJson.version !== theme.value.spec.version) {
      Dialog.warning({
        title: t(
          "core.theme.operations.import_configuration.version_mismatch.title"
        ),
        description: t(
          "core.theme.operations.import_configuration.version_mismatch.description"
        ),
        confirmText: t("core.common.buttons.confirm"),
        cancelText: t("core.common.buttons.cancel"),
        onConfirm: () => {
          handleSaveConfigMap(configJson.configs);
        },
        onCancel() {
          return;
        },
      });
      return;
    }
    handleSaveConfigMap(configJson.configs);
  });

  const handleSaveConfigMap = async (importData: Record<string, string>) => {
    if (!theme.value) {
      return;
    }
    const { data } = await consoleApiClient.theme.theme.fetchThemeConfig({
      name: theme.value.metadata.name as string,
    });
    if (!data || !data.data) {
      return;
    }
    const combinedConfigData = combinedConfigMap(data.data, importData);
    await consoleApiClient.theme.theme.updateThemeConfig({
      name: theme.value.metadata.name,
      configMap: {
        ...data,
        data: combinedConfigData,
      },
    });
    Toast.success(t("core.common.toast.save_success"));
  };

  /**
   * combined benchmark configuration and import configuration
   *
   * benchmark: { a: "{\"a\": 1}", b: "{\"b\": 2}" }
   * expand: { a: "{\"c\": 3}", b: "{\"d\": 4}" }
   * => { a: "{\"a\": 1, \"c\": 3}", b: "{\"b\": 2, \"d\": 4}" }
   *
   * benchmark: { a: "{\"a\": 1}", b: "{\"b\": 2}", d: "{\"d\": 4}"
   * expand: { a: "{\"a\": 2}", b: "{\"b\": 3, \"d\": 4}", c: "{\"c\": 5}" }
   * => { a: "{\"a\": 2}", b: "{\"b\": 3, \"d\": 4}", d: "{\"d\": 4}" }
   *
   */
  const combinedConfigMap = (
    benchmarkConfigMap: { [key: string]: string },
    importConfigMap: { [key: string]: string }
  ): { [key: string]: string } => {
    const result = benchmarkConfigMap;

    for (const key in result) {
      const benchmarkValueJson = JSON.parse(benchmarkConfigMap[key] || "{}");
      const expandValueJson = JSON.parse(importConfigMap[key] || "{}");
      const combinedValue = {
        ...benchmarkValueJson,
        ...expandValueJson,
      };
      result[key] = JSON.stringify(combinedValue);
    }
    return result;
  };

  return {
    handleExportThemeConfiguration,
    openSelectImportFileDialog,
  };
}
