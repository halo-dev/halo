import { consoleApiClient, type ListedCommentList } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";
import type { Ref } from "vue";

export default function useCommentsFetch(
  queryKey: string,
  page: Ref<number>,
  size: Ref<number>,
  approved: Ref<boolean | undefined>,
  sort: Ref<string | undefined>,
  user: Ref<string | undefined>,
  keyword: Ref<string | undefined>,
  subjectRefKey?: Ref<string | undefined>
) {
  return useQuery<ListedCommentList>({
    queryKey: [
      queryKey,
      page,
      size,
      approved,
      sort,
      user,
      keyword,
      subjectRefKey,
    ],
    queryFn: async () => {
      const fieldSelectorMap: Record<string, string | boolean | undefined> = {
        "spec.approved": approved.value,
        "spec.subjectRef": subjectRefKey?.value,
      };

      const fieldSelector = Object.entries(fieldSelectorMap)
        .map(([key, value]) => {
          if (value !== undefined) {
            return `${key}=${value}`;
          }
        })
        .filter(Boolean) as string[];

      const { data } = await consoleApiClient.content.comment.listComments({
        fieldSelector,
        page: page.value,
        size: size.value,
        sort: [sort.value].filter(Boolean) as string[],
        keyword: keyword.value,
        ownerName: user.value,
        ownerKind: user.value ? "User" : undefined,
      });

      return data;
    },
    refetchInterval(data) {
      const hasDeletingData = data?.items.some(
        (comment) => !!comment.comment.metadata.deletionTimestamp
      );
      return hasDeletingData ? 1000 : false;
    },
  });
}
