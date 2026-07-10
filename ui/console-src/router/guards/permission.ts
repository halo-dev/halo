import { stores, utils } from "@halo-dev/ui-shared";
import type { RouteLocationNormalized, Router } from "vue-router";
import { isConsoleAccessDisallowed } from "@/utils/role";

export function setupPermissionGuard(router: Router) {
  router.beforeEach(async (to, _, next) => {
    const currentUserStore = stores.currentUser();

    if (isConsoleAccessDisallowed(currentUserStore.currentUser?.roles)) {
      window.location.href = "/uc";
      return;
    }

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
