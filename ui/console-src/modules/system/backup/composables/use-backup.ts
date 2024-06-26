import { BackupStatusPhaseEnum, coreApiClient } from "@halo-dev/api-client";
import { Dialog, Toast } from "@halo-dev/components";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import dayjs from "dayjs";
import { useI18n } from "vue-i18n";

export function useBackupFetch() {
  return useQuery({
    queryKey: ["backups"],
    queryFn: async () => {
      const { data } = await coreApiClient.migration.backup.listBackup({
        sort: ["metadata.creationTimestamp,desc"],
      });
      return data;
    },
    refetchInterval(data) {
      const deletingBackups = data?.items.filter((backup) => {
        return !!backup.metadata.deletionTimestamp;
      });

      if (deletingBackups?.length) {
        return 1000;
      }

      const pendingBackups = data?.items.filter((backup) => {
        return (
          backup.status?.phase === BackupStatusPhaseEnum.Pending ||
          backup.status?.phase === BackupStatusPhaseEnum.Running
        );
      });

      if (pendingBackups?.length) {
        return 3000;
      }

      return false;
    },
  });
}

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
        await coreApiClient.migration.backup.createBackup({
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
