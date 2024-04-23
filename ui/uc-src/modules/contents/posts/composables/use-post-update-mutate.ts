import { useMutation } from "@tanstack/vue-query";
import type { Post } from "@halo-dev/api-client";
import { apiClient } from "@/utils/api-client";
import { Toast } from "@halo-dev/components";
import { useI18n } from "vue-i18n";

export function usePostUpdateMutate() {
  const { t } = useI18n();

  return useMutation({
    mutationKey: ["uc:update-post"],
    mutationFn: async ({ postToUpdate }: { postToUpdate: Post }) => {
      const { data: latestPost } = await apiClient.uc.post.getMyPost({
        name: postToUpdate.metadata.name,
      });

      return await apiClient.uc.post.updateMyPost(
        {
          name: postToUpdate.metadata.name,
          post: {
            ...latestPost,
            spec: {
              ...latestPost.spec,
              ...postToUpdate.spec,
            },
          },
        },
        {
          mute: true,
        }
      );
    },
    retry: 3,
    onError: () => {
      Toast.error(t("core.common.toast.save_failed_and_retry"));
    },
  });
}
