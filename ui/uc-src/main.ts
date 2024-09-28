import { getBrowserLanguage, i18n, setupI18n } from "@/locales";
import { setupApiClient } from "@/setup/setupApiClient";
import { setupComponents } from "@/setup/setupComponents";
import "@/setup/setupStyles";
import { setupVueQuery } from "@/setup/setupVueQuery";
import { useGlobalInfoStore } from "@/stores/global-info";
import { useRoleStore } from "@/stores/role";
import { useUserStore } from "@/stores/user";
import { getCookie } from "@/utils/cookie";
import { hasPermission } from "@/utils/permission";
import { consoleApiClient } from "@halo-dev/api-client";
import router from "@uc/router";
import { setupCoreModules, setupPluginModules } from "@uc/setup/setupModules";
import { createPinia } from "pinia";
import { createApp, type DirectiveBinding } from "vue";
import App from "./App.vue";

const app = createApp(App);

setupComponents(app);
setupI18n(app);
setupVueQuery(app);
setupApiClient();

app.use(createPinia());

async function loadUserPermissions() {
  const { data: currentPermissions } =
    await consoleApiClient.user.getPermissions({
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
  try {
    setupCoreModules(app);

    const userStore = useUserStore();
    await userStore.fetchCurrentUser();

    // set locale
    i18n.global.locale.value = getCookie("language") || getBrowserLanguage();

    const globalInfoStore = useGlobalInfoStore();
    await globalInfoStore.fetchGlobalInfo();

    if (userStore.isAnonymous) {
      return;
    }

    await loadUserPermissions();

    try {
      await setupPluginModules(app);
    } catch (e) {
      console.error("Failed to load plugins", e);
    }
  } catch (error) {
    console.error("Failed to init app", error);
  } finally {
    app.use(router);
    app.mount("#app");
  }
}
