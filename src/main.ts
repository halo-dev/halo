import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import type { Plugin } from "@halo-dev/admin-shared";
// setup
import "./setup/setupStyles";
import { setupComponents } from "./setup/setupComponents";
import { registerMenu } from "@/router/menus.config";

// core modules
import { coreModules } from "./modules";

const app = createApp(App);

setupComponents(app);

app.use(createPinia());

function registerModule(pluginModule: Plugin) {
  if (pluginModule.components) {
    if (!Array.isArray(pluginModule.components)) {
      console.error(`${pluginModule.name}: Plugin components must be an array`);
      return;
    }

    for (const component of pluginModule.components) {
      component.name && app.component(component.name, component);
    }
  }

  if (pluginModule.routes) {
    if (!Array.isArray(pluginModule.routes)) {
      console.error(`${pluginModule.name}: Plugin routes must be an array`);
      return;
    }

    for (const route of pluginModule.routes) {
      router.addRoute(route);
    }
  }

  if (pluginModule.menus) {
    if (!Array.isArray(pluginModule.menus)) {
      console.error(`${pluginModule.name}: Plugin menus must be an array`);
      return;
    }

    for (const group of pluginModule.menus) {
      for (const menu of group.items) {
        registerMenu(group.name, menu);
      }
    }
  }
}

function loadCoreModules() {
  coreModules.forEach(registerModule);
}

async function loadPluginModules() {
  // TODO: load plugin modules
}

(async function () {
  await initApp();
})();

async function initApp() {
  try {
    loadCoreModules();
    await loadPluginModules();
  } catch (e) {
    console.error(e);
  }
  app.use(router);
  app.mount("#app");
}
