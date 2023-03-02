import { useRoleStore } from "@/stores/role";
import isEqual from "lodash.isequal";

/**
 * It returns true if the user has all the permissions required to access a resource
 *
 * @param uiPermissions - The permissions that the user has.
 * @param targetPermissions - The permissions that the user needs to have in order to access the
 * resource.
 * @param {boolean} any - boolean - if true, the user only needs to have one of the targetPermissions
 * to pass the check.
 */
export function hasPermission(
  uiPermissions: Array<string>,
  targetPermissions: Array<string>,
  any: boolean
): boolean {
  // super admin has all permissions
  if (uiPermissions.includes("*")) {
    return true;
  }

  if (!targetPermissions || !targetPermissions.length) {
    return true;
  }

  const intersection = uiPermissions.filter((p) =>
    targetPermissions.includes(p)
  );

  if (any && intersection.length) {
    return true;
  }

  return !!(!any && isEqual(intersection, targetPermissions));
}

interface usePermissionReturn {
  currentUserHasPermission: (targetPermissions: Array<string>) => boolean;
}

/**
 * It returns a function that checks if the current user has a permission
 *
 * @returns An object with a function called currentUserHasPermission
 */
export function usePermission(): usePermissionReturn {
  const roleStore = useRoleStore();
  const { uiPermissions } = roleStore.permissions;

  const currentUserHasPermission = (
    targetPermissions: Array<string>
  ): boolean => {
    return hasPermission(uiPermissions, targetPermissions, true);
  };

  return {
    currentUserHasPermission,
  };
}
