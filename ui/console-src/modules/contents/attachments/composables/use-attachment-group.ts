import { paginate } from "@/utils/paginate";
import type {
  Group,
  GroupV1alpha1ApiListGroupRequest,
} from "@halo-dev/api-client";
import { coreApiClient } from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";
import type { Ref } from "vue";

interface useFetchAttachmentGroupReturn {
  groups: Ref<Group[] | undefined>;
  isLoading: Ref<boolean>;
  handleFetchGroups: () => void;
}

export function useFetchAttachmentGroup(): useFetchAttachmentGroupReturn {
  const { data, isLoading, refetch } = useQuery<Group[]>({
    queryKey: ["attachment-groups"],
    queryFn: async () => {
      return await paginate<GroupV1alpha1ApiListGroupRequest, Group>(
        (params) => coreApiClient.storage.group.listGroup(params),
        {
          size: 1000,
          labelSelector: ["!halo.run/hidden"],
          sort: ["metadata.creationTimestamp,asc"],
        }
      );
    },
    refetchInterval(data) {
      const hasDeletingGroup = data?.some(
        (group) => !!group.metadata.deletionTimestamp
      );

      return hasDeletingGroup ? 1000 : false;
    },
  });

  return {
    groups: data,
    isLoading,
    handleFetchGroups: refetch,
  };
}
