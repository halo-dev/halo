import type { SinglePage } from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
import { useMutation } from "@tanstack/vue-query";
import { useI18n } from "vue-i18n";

export function usePageUpdateMutate() {
  const { t } = useI18n();
  return useMutation({
    mutationKey: ["singlePage-update"],
    mutationFn: async (page: SinglePage) => {
      const { data: latestSinglePage } =
        await coreApiClient.content.singlePage.getSinglePage({
          name: page.metadata.name,
        });

      return coreApiClient.content.singlePage.updateSinglePage(
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
      console.error("Failed to update singlePage", error);
      Toast.error(t("core.common.toast.server_internal_error"));
    },
  });
}
