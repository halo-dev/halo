import { storageAnnotations } from "@/constants/annotations";
import {
  coreApiClient,
  LocalThumbnailStatusPhaseEnum,
  type LocalThumbnail,
} from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
import { useQueryClient } from "@tanstack/vue-query";
import type { Ref } from "vue";
import { useI18n } from "vue-i18n";

export function useThumbnailControl(thumbnail: Ref<LocalThumbnail>) {
  const queryClient = useQueryClient();
  const { t } = useI18n();

  async function handleRetry() {
    await coreApiClient.storage.localThumbnail.patchLocalThumbnail({
      name: thumbnail.value.metadata.name,
      jsonPatchInner: [
        {
          op: "add",
          path: "/status/phase",
          value: LocalThumbnailStatusPhaseEnum.Pending,
        },
        {
          op: "add",
          path: "/metadata/annotations",
          value: {
            ...thumbnail.value.metadata.annotations,
            [storageAnnotations.RETRY_TIMESTAMP]: Date.now().toString(),
          },
        },
      ],
    });

    await queryClient.invalidateQueries({
      queryKey: ["core:attachments:thumbnails"],
    });

    Toast.success(t("core.common.toast.operation_success"));
  }

  return {
    handleRetry,
  };
}
