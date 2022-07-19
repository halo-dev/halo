import isEqual from "lodash.isequal";

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
