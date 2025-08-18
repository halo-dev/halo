import { useRoleStore } from "@/stores/role";
import { hasPermission } from "@/utils/permission";
import type { RouteLocationNormalized, Router } from "vue-router";

export function setupPermissionGuard(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    const roleStore = useRoleStore();
    const { uiPermissions } = roleStore.permissions;
    if (await checkRoutePermissions(to, uiPermissions)) {
      next();
    } else {
      next({ name: "Forbidden" });
    }
  });
}

async function checkRoutePermissions(
  to: RouteLocationNormalized,
  uiPermissions: string[]
): Promise<boolean> {
  const { meta } = to;

  if (!meta?.permissions) {
    return true;
  }

  if (typeof meta.permissions === "function") {
    try {
      return await meta.permissions(uiPermissions);
    } catch (e) {
      console.error(
        `Error checking permissions for route ${String(to.name)}:`,
        e
      );
      return false;
    }
  }

  return hasPermission(
    Array.from(uiPermissions),
    meta.permissions as string[],
    true
  );
}
