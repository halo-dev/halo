import { rbacAnnotations } from "@/constants/annotations";
import { SUPER_ROLE_NAME } from "@/constants/constants";
import { useRoleStore } from "@/stores/role";
import { useUserStore } from "@/stores/user";
import { hasPermission } from "@/utils/permission";
import type { Role } from "@halo-dev/api-client";
import type { RouteLocationNormalized, Router } from "vue-router";

export function setupPermissionGuard(router: Router) {
  router.beforeEach((to, _, next) => {
    const userStore = useUserStore();
    const roleStore = useRoleStore();

    if (isConsoleAccessDisallowed(userStore.currentRoles)) {
      window.location.href = "/uc";
      return;
    }

    if (checkRoutePermissions(to, roleStore.permissions.uiPermissions)) {
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

function checkRoutePermissions(
  to: RouteLocationNormalized,
  uiPermissions: string[]
): boolean {
  const { meta } = to;
  if (meta?.permissions) {
    return hasPermission(
      Array.from(uiPermissions),
      meta.permissions as string[],
      true
    );
  }
  return true;
}
