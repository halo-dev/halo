import "@/setup/setupStyles";
import { createApp, type DirectiveBinding } from "vue";
import App from "./App.vue";
import { setupVueQuery } from "@/setup/setupVueQuery";
import { setupComponents } from "@/setup/setupComponents";
import { setupI18n } from "@/locales";
import { createPinia } from "pinia";
import { setupCoreModules, setupPluginModules } from "@uc/setup/setupModules";
import router from "@uc/router";
import { useUserStore } from "@/stores/user";
import { apiClient } from "@/utils/api-client";
import { useRoleStore } from "@/stores/role";
import { hasPermission } from "@/utils/permission";

const app = createApp(App);

setupComponents(app);
setupI18n(app);
setupVueQuery(app);

app.use(createPinia());

async function loadUserPermissions() {
  const { data: currentPermissions } = await apiClient.user.getPermissions({
    name: "-",
  });
  const roleStore = useRoleStore();
  roleStore.$patch({
    permissions: currentPermissions,
  });
  app.directive(
    "permission",
    (el: HTMLElement, binding: DirectiveBinding<string[]>) => {
      const uiPermissions = Array.from<string>(
        currentPermissions.uiPermissions
      );
      const { value } = binding;
      const { any, enable } = binding.modifiers;

      if (hasPermission(uiPermissions, value, any)) {
        return;
      }

      if (enable) {
        //TODO
        return;
      }
      el?.remove?.();
    }
  );
}

(async function () {
  await initApp();
})();

async function initApp() {
  setupCoreModules(app);

  const userStore = useUserStore();
  await userStore.fetchCurrentUser();

  loadUserPermissions();

  try {
    await setupPluginModules(app);
  } catch (e) {
    console.error("Failed to load plugins", e);
  }

  app.use(router);
  app.mount("#app");
}
