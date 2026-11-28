import {
  BackupStatusPhaseEnum,
  coreApiClient,
  paginate,
  type Backup,
  type BackupV1alpha1ApiListBackupRequest,
} from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
import { utils } from "@halo-dev/ui-shared";
import { useQuery, useQueryClient } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

const BACKUP_PASSWORD_ANNOTATION = "migration.halo.run/backup-password";

export function useBackupFetch() {
  return useQuery({
    queryKey: ["backups"],
    queryFn: async () => {
      return await paginate<BackupV1alpha1ApiListBackupRequest, Backup>(
        (params) => coreApiClient.migration.backup.listBackup(params),
        {
          size: 1000,
        }
      );
    },
    refetchInterval(data) {
      const deletingBackups = data?.filter((backup) => {
        return !!backup.metadata.deletionTimestamp;
      });

      if (deletingBackups?.length) {
        return 1000;
      }

      const pendingBackups = data?.filter((backup) => {
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

  const createBackup = async (password?: string) => {
    await coreApiClient.migration.backup.createBackup({
      backup: {
        apiVersion: "migration.halo.run/v1alpha1",
        kind: "Backup",
        metadata: {
          generateName: "backup-",
          name: "",
          annotations: password
            ? { [BACKUP_PASSWORD_ANNOTATION]: password }
            : undefined,
        },
        spec: {
          expiresAt: utils.date.dayjs().add(7, "day").toISOString(),
        },
      },
    });

    queryClient.invalidateQueries({ queryKey: ["backups"] });

    Toast.success(t("core.backup.operations.create.toast_success"));
  };

  return { createBackup };
}
