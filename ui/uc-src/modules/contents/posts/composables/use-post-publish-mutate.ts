import { ucApiClient } from "@halo-dev/api-client";
import { useMutation } from "@tanstack/vue-query";

export function usePostPublishMutate() {
  return useMutation({
    mutationKey: ["uc:publish-post"],
    mutationFn: async ({ name }: { name: string }) => {
      return await ucApiClient.content.post.publishMyPost(
        {
          name: name,
        },
        {
          mute: true,
        }
      );
    },
    retry: 3,
  });
}
