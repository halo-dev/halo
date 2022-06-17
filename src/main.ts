import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import type { Plugin } from "@halo-dev/admin-shared";
// setup
import "./setup/setupStyles";
import { setupComponents } from "./setup/setupComponents";
import { registerMenu } from "@/router/menus.config";

// modules
import dashboardModule from "./modules/dashboard/module";
import postModule from "./modules/contents/posts/module";
import sheetModule from "./modules/contents/sheets/module";
import commentModule from "./modules/contents/comments/module";
import attachmentModule from "./modules/contents/attachments/module";
import themeModule from "./modules/interface/themes/module";
import menuModule from "./modules/interface/menus/module";
import pluginModule from "./modules/system/plugins/module";
import userModule from "./modules/system/users/module";
import roleModule from "./modules/system/roles/module";
import settingModule from "./modules/system/settings/module";

const app = createApp(App);

setupComponents(app);

app.use(createPinia());

async function registerModule(pluginModule: Plugin) {
  if (pluginModule.components) {
    for (const component of pluginModule.components) {
      component.name && app.component(component.name, component);
    }
  }

  if (pluginModule.routes) {
    for (const route of pluginModule.routes) {
      router.addRoute(route);
    }
  }

  if (pluginModule.menus) {
    for (const group of pluginModule.menus) {
      for (const menu of group.items) {
        registerMenu(group.name, menu);
      }
    }
  }
}

function loadCoreModules() {
  Array.from<Plugin>([
    dashboardModule,
    postModule,
    sheetModule,
    commentModule,
    attachmentModule,
    themeModule,
    menuModule,
    pluginModule,
    userModule,
    roleModule,
    settingModule,
  ]).forEach(registerModule);
}

function loadPluginModules() {
  // TODO: load plugin modules
}

initApp();

async function initApp() {
  loadCoreModules();
  loadPluginModules();
  app.use(router);
  app.mount("#app");
}
