import { utils } from "@halo-dev/console-shared";
import type { RouteLocationNormalized, Router } from "vue-router";

export function setupPermissionGuard(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    if (
      await checkRoutePermissions(
        to,
        utils.permission.getUserPermissions() || []
      )
    ) {
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

  return utils.permission.has(meta.permissions as string[]);
}
