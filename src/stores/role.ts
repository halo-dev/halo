import { defineStore } from "pinia";
import type { Role, UserPermission } from "@halo-dev/api-client";
import { ref } from "vue";
import { apiClient } from "@/utils/api-client";
import { roleLabels } from "@/constants/labels";
import { rbacAnnotations } from "@/constants/annotations";

export const useRoleStore = defineStore("role", () => {
  const roles = ref<Role[]>([]);
  const permissions = ref<UserPermission>({
    roles: [],
    uiPermissions: [],
  });

  async function fetchRoles() {
    try {
      const { data } = await apiClient.extension.role.listv1alpha1Role({
        page: 0,
        size: 0,
        labelSelector: [`!${roleLabels.TEMPLATE}`],
      });
      roles.value = data.items;
    } catch (error) {
      console.error("Failed to fetch roles", error);
    }
  }

  function getRoleDisplayName(name: string) {
    const role = roles.value.find((role) => role.metadata.name === name);
    return role?.metadata.annotations?.[rbacAnnotations.DISPLAY_NAME] || name;
  }

  return { roles, permissions, fetchRoles, getRoleDisplayName };
});
