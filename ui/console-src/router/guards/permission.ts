import { rbacAnnotations } from "@/constants/annotations";
import { SUPER_ROLE_NAME } from "@/constants/constants";
import type { Role } from "@halo-dev/api-client";
import { stores, utils } from "@halo-dev/console-shared";
import type { RouteLocationNormalized, Router } from "vue-router";

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

function isConsoleAccessDisallowed(currentRoles?: Role[]): boolean {
  if (currentRoles?.some((role) => role.metadata.name === SUPER_ROLE_NAME)) {
    return false;
  }

  return (
    currentRoles?.some(
      (role) =>
        role.metadata.annotations?.[rbacAnnotations.DISALLOW_ACCESS_CONSOLE] ===
        "true"
    ) || false
  );
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
