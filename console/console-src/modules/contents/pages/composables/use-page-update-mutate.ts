import { apiClient } from "@/utils/api-client";
import { useMutation } from "@tanstack/vue-query";
import type { SinglePage } from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
import { useI18n } from "vue-i18n";

export function usePageUpdateMutate() {
  const { t } = useI18n();
  return useMutation({
    mutationKey: ["singlePage-update"],
    mutationFn: async (page: SinglePage) => {
      const { data: latestSinglePage } =
        await apiClient.extension.singlePage.getcontentHaloRunV1alpha1SinglePage(
          {
            name: page.metadata.name,
          }
        );

      return apiClient.extension.singlePage.updatecontentHaloRunV1alpha1SinglePage(
        {
          name: page.metadata.name,
          singlePage: {
            ...latestSinglePage,
            spec: page.spec,
            metadata: {
              ...latestSinglePage.metadata,
              annotations: page.metadata.annotations,
            },
          },
        },
        {
          mute: true,
        }
      );
    },
    retry: 3,
    onError: (error) => {
      console.error("Failed to update post", error);
      Toast.error(t("core.common.toast.server_internal_error"));
    },
  });
}
