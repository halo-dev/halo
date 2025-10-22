/**
 * Utility class for checking user permissions.
 */
export class PermissionUtils {
  private userPermissions?: Array<string>;

  /**
   * Creates a new PermissionUtils instance.
   * @param userPermissions - Array of permissions that the user has
   */
  constructor(userPermissions?: Array<string>) {
    this.userPermissions = userPermissions;
  }

  /**
   * Checks if the user has the required permissions.
   *
   * @param permissions - Array of permissions to check against user's permissions
   * @param any - If true, returns true when ANY of the required permissions match.
   *              If false, returns true only when ALL required permissions match.
   *              Defaults to true.
   * @returns true if the permission check passes, false otherwise
   *
   * @throws Error if user permissions are not set
   *
   * @example
   * ```ts
   * import { utils } from "@halo-dev/console-shared"
   *
   * // Check if user has any of the permissions
   * utils.permission.has(['core:posts:manage'], true);
   *
   * // Check if user has all of the permissions
   * utils.permission.has(['core:posts:view', 'core:attachments:view'], false);
   * ```
   */
  has(permissions: Array<string>, any: boolean = true): boolean {
    if (!this.userPermissions) {
      throw new Error("User permissions not set in PermissionUtils");
    }

    // Super user with wildcard permission has all permissions
    if (this.userPermissions.includes("*")) {
      return true;
    }

    // If no permissions are required, access is granted
    if (!permissions.length) {
      return true;
    }

    // If user has no permissions, access is denied
    if (!this.userPermissions.length) {
      return false;
    }

    if (any) {
      // Any match: at least one of the required permissions must be present
      return permissions.some((p) => this.userPermissions!.includes(p));
    }

    // All match: all required permissions must be present
    return permissions.every((p) => this.userPermissions!.includes(p));
  }

  /**
   * Retrieves the current user permissions.
   * @returns Array of user permissions or undefined if not set
   */
  getUserPermissions(): Array<string> | undefined {
    return this.userPermissions;
  }

  /**
   * Updates the user permissions.
   * @param userPermissions - Array of permissions to set for the user
   */
  setUserPermissions(userPermissions: Array<string>) {
    this.userPermissions = userPermissions;
  }
}
