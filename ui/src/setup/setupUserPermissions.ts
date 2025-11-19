import { useRoleStore } from "@/stores/role";
import { consoleApiClient } from "@halo-dev/api-client";
import { utils } from "@halo-dev/ui-shared";
import type { App, DirectiveBinding } from "vue";

export async function setupUserPermissions(app: App) {
  const { data: currentPermissions } =
    await consoleApiClient.user.getPermissions({
      name: "-",
    });
  const roleStore = useRoleStore();
  roleStore.$patch({
    permissions: currentPermissions,
  });

  // Set permissions in shared utils
  utils.permission.setUserPermissions(currentPermissions.uiPermissions);

  app.directive(
    "permission",
    (el: HTMLElement, binding: DirectiveBinding<string[]>) => {
      const { value } = binding;
      const { any } = binding.modifiers;

      if (utils.permission.has(value, any ?? false)) {
        return;
      }

      el?.remove?.();
    }
  );
}
