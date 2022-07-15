import isEqual from "lodash.isEqual";

export function hasPermission(
  uiPermissions: Array<string>,
  targetPermissions: Array<string>,
  any: boolean
): boolean {
  if (!targetPermissions || !targetPermissions.length) {
    return true;
  }

  // super admin has all permissions
  if (targetPermissions.includes("*")) {
    return true;
  }

  const intersection = uiPermissions.filter((p) =>
    targetPermissions.includes(p)
  );

  if (any && intersection.length) {
    return true;
  }

  return !!(!any && isEqual(uiPermissions, targetPermissions));
}
