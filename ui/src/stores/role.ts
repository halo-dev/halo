import type { UserPermission } from "@halo-dev/api-client";
import { defineStore } from "pinia";
import { ref } from "vue";

export const useRoleStore = defineStore("role", () => {
  const permissions = ref<UserPermission>({
    roles: [],
    permissions: [],
    uiPermissions: [],
  });

  return { permissions };
});
