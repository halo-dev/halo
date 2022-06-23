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
import { useScriptTag } from "@vueuse/core";
import { usePluginStore } from "@/stores/plugin";
import axiosInstance from "@/utils/api-client";

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

const pluginStore = usePluginStore();

async function loadPluginModules() {
  const response = await axiosInstance.get(
    `/apis/plugin.halo.run/v1alpha1/plugins`
  );

  // Get all started plugins
  const plugins = response.data.filter(
    (plugin) => plugin.status.phase === "STARTED" && plugin.spec.enabled
  );

  for (const plugin of plugins) {
    const { entry } = plugin.status;

    if (entry) {
      const { load } = useScriptTag(
        `http://localhost:8090${plugin.status.entry}`
      );
      await load();
      const pluginModule = window[plugin.metadata.name];

      if (pluginModule) {
        // @ts-ignore
        plugin.spec.module = pluginModule;
        registerModule(pluginModule);
      }
    }

    pluginStore.registerPlugin(plugin);
  }
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
  } finally {
    app.use(router);
    app.mount("#app");
  }
}
