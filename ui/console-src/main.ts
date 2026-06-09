import modules from "@console/modules";
import { useThemeStore } from "@console/stores/theme";
import { stores } from "@halo-dev/ui-shared";
import { createPinia } from "pinia";
import "@/setup/setupStyles";
import { createApp } from "vue";
import { builtinFormKitInputs } from "@/formkit/inputs";
import { setLanguage, setupI18n } from "@/locales";
import { setupApiClient } from "@/setup/setupApiClient";
import {
  setupComponents,
  type SetupComponentsOptions,
} from "@/setup/setupComponents";
import { setupCoreModules, setupUiPluginRuntime } from "@/setup/setupModules";
import "core-js/es/object/has-own";
import { setupUserPermissions } from "@/setup/setupUserPermissions";
import { setupVueQuery } from "@/setup/setupVueQuery";
import App from "./App.vue";
import router from "./router";

const app = createApp(App);
let componentsReady = false;

setupI18n(app);
setupVueQuery(app);
setupApiClient();

app.use(createPinia());

async function loadActivatedTheme() {
  const themeStore = useThemeStore();
  await themeStore.fetchActivatedTheme();
}

function setupAppComponents(options?: SetupComponentsOptions) {
  if (componentsReady) {
    return;
  }
  setupComponents(app, options);
  componentsReady = true;
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
      setupAppComponents();
      return;
    }

    await setupUserPermissions(app);

    await setupUiPluginRuntime({
      app,
      router,
      platform: "console",
      setupComponents: setupAppComponents,
      registeredFormKitInputs: builtinFormKitInputs,
    });

    await loadActivatedTheme();
  } catch (e) {
    console.error(e);
  } finally {
    setupAppComponents();
    app.use(router);
    app.mount("#app");
  }
}
