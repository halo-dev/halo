import { defineStore } from "pinia";
import type { UserPermission } from "@halo-dev/api-client";
import { ref } from "vue";

export const useRoleStore = defineStore("role", () => {
  const permissions = ref<UserPermission>({
    roles: [],
    uiPermissions: [],
  });

  return { permissions };
});
