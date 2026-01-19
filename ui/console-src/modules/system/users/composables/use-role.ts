import { roleLabels } from "@/constants/labels";
import { paginate } from "@/utils/paginate";
import {
  coreApiClient,
  type Role,
  type RoleV1alpha1ApiListRoleRequest,
} from "@halo-dev/api-client";
import { useQuery } from "@tanstack/vue-query";

export function useFetchRoles() {
  return useQuery({
    queryKey: ["core:roles"],
    queryFn: async () => {
      return await paginate<RoleV1alpha1ApiListRoleRequest, Role>(
        (params) => coreApiClient.role.listRole(params),
        {
          size: 1000,
          labelSelector: [`!${roleLabels.TEMPLATE}`],
        }
      );
    },
    refetchInterval(data) {
      const hasDeletingRole = data?.some(
        (item) => !!item.metadata.deletionTimestamp
      );
      return hasDeletingRole ? 1000 : false;
    },
  });
}

export function useFetchRoleTemplates() {
  return useQuery({
    queryKey: ["core:role-templates"],
    queryFn: async () => {
      return await paginate<RoleV1alpha1ApiListRoleRequest, Role>(
        (params) => coreApiClient.role.listRole(params),
        {
          size: 1000,
          labelSelector: [`${roleLabels.TEMPLATE}=true`, "!halo.run/hidden"],
        }
      );
    },
  });
}
