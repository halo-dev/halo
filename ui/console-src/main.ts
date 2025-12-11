import { setLanguage, setupI18n } from "@/locales";
import { setupApiClient } from "@/setup/setupApiClient";
import { setupComponents } from "@/setup/setupComponents";
import { setupCoreModules, setupPluginModules } from "@/setup/setupModules";
import "@/setup/setupStyles";
import { setupUserPermissions } from "@/setup/setupUserPermissions";
import { setupVueQuery } from "@/setup/setupVueQuery";
import modules from "@console/modules";
import { useThemeStore } from "@console/stores/theme";
import { stores } from "@halo-dev/ui-shared";
import "core-js/es/object/has-own";
import { createPinia } from "pinia";
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";

const app = createApp(App);

setupComponents(app);
setupI18n(app);
setupVueQuery(app);
setupApiClient();

app.use(createPinia());

async function loadActivatedTheme() {
  const themeStore = useThemeStore();
  await themeStore.fetchActivatedTheme();
}

await initApp();

async function initApp() {
  try {
    setupCoreModules({ app, router, platform: "console", modules });

    const currentUserStore = stores.currentUser();
    await currentUserStore.fetchCurrentUser();

    const globalInfoStore = stores.globalInfo();
    await globalInfoStore.fetchGlobalInfo();

    await setLanguage();

    if (currentUserStore.isAnonymous) {
      return;
    }

    await setupUserPermissions(app);

    try {
      await setupPluginModules({ app, router, platform: "console" });
    } catch (e) {
      console.error("Failed to load plugins", e);
    }

    await loadActivatedTheme();
  } catch (e) {
    console.error(e);
  } finally {
    app.use(router);
    app.mount("#app");
  }
}
