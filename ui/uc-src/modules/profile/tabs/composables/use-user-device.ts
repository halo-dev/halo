import { ucApiClient, type UserDevice } from "@halo-dev/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

export function useUserDevice(device: UserDevice) {
  const { t } = useI18n();
  const queryClient = useQueryClient();

  function handleRevoke() {
    Dialog.warning({
      title: t("core.uc_profile.device.operations.revoke.title"),
      description: t("core.uc_profile.device.operations.revoke.description"),
      confirmType: "danger",
      confirmText: t("core.common.buttons.confirm"),
      cancelText: t("core.common.buttons.cancel"),
      async onConfirm() {
        await ucApiClient.security.device.revokeDevice({
          deviceId: device.device.metadata.name,
        });

        Toast.success(t("core.common.toast.delete_success"));

        queryClient.invalidateQueries({ queryKey: ["uc:devices"] });
      },
    });
  }
  return { handleRevoke };
}
