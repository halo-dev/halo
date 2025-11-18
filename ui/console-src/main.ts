import { setLanguage, setupI18n } from "@/locales";
import { setupApiClient } from "@/setup/setupApiClient";
import { setupComponents } from "@/setup/setupComponents";
import "@/setup/setupStyles";
import { setupVueQuery } from "@/setup/setupVueQuery";
import { useRoleStore } from "@/stores/role";
import {
  setupCoreModules,
  setupPluginModules,
} from "@console/setup/setupModules";
import { useThemeStore } from "@console/stores/theme";
import { consoleApiClient } from "@halo-dev/api-client";
import { stores, utils } from "@halo-dev/ui-shared";
import "core-js/es/object/has-own";
import { createPinia } from "pinia";
import type { DirectiveBinding } from "vue";
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";

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

async function loadActivatedTheme() {
  const themeStore = useThemeStore();
  await themeStore.fetchActivatedTheme();
}

(async function () {
  await initApp();
})();

async function initApp() {
  try {
    setupCoreModules(app);

    const currentUserStore = stores.currentUser();
    await currentUserStore.fetchCurrentUser();

    const globalInfoStore = stores.globalInfo();
    await globalInfoStore.fetchGlobalInfo();

    await setLanguage();

    if (currentUserStore.isAnonymous) {
      return;
    }

    await loadUserPermissions();

    try {
      await setupPluginModules(app);
    } catch (e) {
      console.error("Failed to load plugins", e);
    }

    if (globalInfoStore.globalInfo?.userInitialized) {
      await loadActivatedTheme();
    }
  } catch (e) {
    console.error(e);
  } finally {
    app.use(router);
    app.mount("#app");
  }
}
