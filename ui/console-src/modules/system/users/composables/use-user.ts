import { consoleApiClient } from "@halo-dev/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import { useI18n } from "vue-i18n";

export function useUserEnableDisable() {
  const { t } = useI18n();

  const handleEnableOrDisableUser = ({
    name,
    operation,
    onSuccess,
  }: {
    name: string;
    operation: "enable" | "disable";
    onSuccess?: () => void;
  }) => {
    const operations = {
      enable: {
        title: t("core.user.operations.enable.title"),
        description: t("core.user.operations.enable.description"),
        request: (name: string) =>
          consoleApiClient.user.enableUser({ username: name }),
        message: t("core.common.toast.enable_success"),
      },
      disable: {
        title: t("core.user.operations.disable.title"),
        description: t("core.user.operations.disable.description"),
        request: (name: string) =>
          consoleApiClient.user.disableUser({ username: name }),
        message: t("core.common.toast.disable_success"),
      },
    };

    const operationConfig = operations[operation];

    Dialog.warning({
      title: operationConfig.title,
      description: operationConfig.description,
      confirmType: "danger",
      confirmText: t("core.common.buttons.confirm"),
      cancelText: t("core.common.buttons.cancel"),
      onConfirm: async () => {
        try {
          await operationConfig.request(name);

          Toast.success(operationConfig.message);
          onSuccess?.();
        } catch (e) {
          console.error("Failed to enable or disable user", e);
        }
      },
    });
  };

  return {
    handleEnableOrDisableUser,
  };
}
