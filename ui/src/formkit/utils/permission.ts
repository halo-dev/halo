import type { FormKitNode } from "@formkit/core";
import { utils } from "@halo-dev/ui-shared";
import { i18n } from "@/locales";

/**
 * Checks whether the current user can read the data backing a FormKit input.
 *
 * Permissions may not be initialized yet (e.g. forms rendered outside the
 * console bootstrap). In that case the check is treated as allowed so the
 * previous behavior is preserved.
 */
export function hasDataPermission(permissions: string[]): boolean {
  try {
    return utils.permission.has(permissions);
  } catch {
    return true;
  }
}

/**
 * Guards a FormKit input whose options are loaded from the API.
 *
 * Returns `false` when the current user is not allowed to read that data. The
 * input is then disabled and annotated with a "no permission" message, and the
 * caller must skip the request so the console never triggers a 403.
 */
export function applyDataPermissionGuard(
  node: FormKitNode,
  permissions: string[]
): boolean {
  if (hasDataPermission(permissions)) {
    return true;
  }

  const message = i18n.global.t("core.formkit.permission.no_permission");
  const help = typeof node.props.help === "string" ? node.props.help : "";

  node.props.disabled = true;
  node.props.help = help ? `${help} · ${message}` : message;
  if (!node.props.placeholder) {
    node.props.placeholder = message;
  }

  return false;
}

export function noPermissionMessage(): string {
  return i18n.global.t("core.formkit.permission.no_permission");
}
