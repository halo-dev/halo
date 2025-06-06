import { coreApiClient, type ListedComment } from "@halo-dev/api-client";
import { useMutation, useQueryClient } from "@tanstack/vue-query";

export const useCommentLastReadTimeMutate = (comment: ListedComment) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ["update-comment-last-read-time"],
    mutationFn: async () => {
      const { data } = await coreApiClient.content.comment.patchComment(
        {
          name: comment.comment.metadata.name,
          jsonPatchInner: [
            {
              op: "add",
              path: "/spec/lastReadTime",
              value: new Date().toISOString(),
            },
          ],
        },
        {
          mute: true,
        }
      );
      if (data.status?.unreadReplyCount) {
        throw new Error("Unread reply count is not 0, retry");
      }
      return data;
    },
    retry: 5,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["core:comments"] });
    },
  });
};
