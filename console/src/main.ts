import { createApp } from "vue";
import type { DirectiveBinding } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import { apiClient } from "@/utils/api-client";
// setup
import "./setup/setupStyles";
import { setupComponents } from "./setup/setupComponents";
import { setupI18n, i18n, getBrowserLanguage } from "./locales";
// core modules
import { hasPermission } from "@/utils/permission";
import { useRoleStore } from "@/stores/role";
import { useThemeStore } from "./stores/theme";
import { useUserStore } from "./stores/user";
import { useSystemConfigMapStore } from "./stores/system-configmap";
import { setupVueQuery } from "./setup/setupVueQuery";
import { useGlobalInfoStore } from "./stores/global-info";
import { setupCoreModules, setupPluginModules } from "./setup/setupModules";

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

async function loadActivatedTheme() {
  const themeStore = useThemeStore();
  await themeStore.fetchActivatedTheme();
}

(async function () {
  await initApp();
})();

async function initApp() {
  // TODO 实验性
  const theme = localStorage.getItem("theme");
  if (theme) {
    document.body.classList.add(theme);
  }

  try {
    setupCoreModules(app);

    const userStore = useUserStore();
    await userStore.fetchCurrentUser();

    // set locale
    i18n.global.locale.value =
      localStorage.getItem("locale") || getBrowserLanguage();

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

    // load system configMap
    const systemConfigMapStore = useSystemConfigMapStore();
    await systemConfigMapStore.fetchSystemConfigMap();

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
