import { apiClient } from "@/utils/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import dayjs from "dayjs";
import { useI18n } from "vue-i18n";

export function useBackup() {
  const { t } = useI18n();
  const queryClient = useQueryClient();

  const handleCreate = async () => {
    Dialog.info({
      title: t("core.backup.operations.create.title"),
      description: t("core.backup.operations.create.description"),
      confirmText: t("core.common.buttons.confirm"),
      cancelText: t("core.common.buttons.cancel"),
      async onConfirm() {
        await apiClient.extension.backup.createmigrationHaloRunV1alpha1Backup({
          backup: {
            apiVersion: "migration.halo.run/v1alpha1",
            kind: "Backup",
            metadata: {
              generateName: "backup-",
              name: "",
            },
            spec: {
              expiresAt: dayjs().add(7, "day").toISOString(),
            },
          },
        });

        queryClient.invalidateQueries({ queryKey: ["backups"] });

        Toast.success(t("core.backup.operations.create.toast_success"));
      },
    });
  };

  return { handleCreate };
}
