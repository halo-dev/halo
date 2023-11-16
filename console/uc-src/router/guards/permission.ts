import { useRoleStore } from "@/stores/role";
import { hasPermission } from "@/utils/permission";
import type { Router } from "vue-router";

export function setupPermissionGuard(router: Router) {
  router.beforeEach((to, from, next) => {
    const roleStore = useRoleStore();
    const { uiPermissions } = roleStore.permissions;
    const { meta } = to;
    if (meta && meta.permissions) {
      const flag = hasPermission(
        Array.from(uiPermissions),
        meta.permissions as string[],
        true
      );
      if (!flag) {
        next({ name: "Forbidden" });
      }
    }
    next();
  });
}
