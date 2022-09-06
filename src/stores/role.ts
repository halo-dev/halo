import { defineStore } from "pinia";
import type { Role, UserPermission } from "@halo-dev/api-client";

interface RoleStoreState {
  roles: Role[]; // all roles
  permissions: UserPermission; // current user's permissions
}

export const useRoleStore = defineStore({
  id: "role",
  state: (): RoleStoreState => ({
    roles: [],
    permissions: {
      roles: [],
      uiPermissions: [],
    },
  }),
});
