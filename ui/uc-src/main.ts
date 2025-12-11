import { setLanguage, setupI18n } from "@/locales";
import { setupApiClient } from "@/setup/setupApiClient";
import { setupComponents } from "@/setup/setupComponents";
import { setupCoreModules, setupPluginModules } from "@/setup/setupModules";
import "@/setup/setupStyles";
import { setupUserPermissions } from "@/setup/setupUserPermissions";
import { setupVueQuery } from "@/setup/setupVueQuery";
import { stores } from "@halo-dev/ui-shared";
import modules from "@uc/modules";
import router from "@uc/router";
import "core-js/es/object/has-own";
import { createPinia } from "pinia";
import { createApp } from "vue";
import App from "./App.vue";

const app = createApp(App);

setupComponents(app);
setupI18n(app);
setupVueQuery(app);
setupApiClient();

app.use(createPinia());

await initApp();

async function initApp() {
  try {
    setupCoreModules({ app, router, platform: "uc", modules });

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
      await setupPluginModules({ app, router, platform: "uc" });
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
