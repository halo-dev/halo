import type { DirectiveBinding } from "vue";
import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import type {
  MenuGroupType,
  MenuItemType,
  Plugin,
} from "@halo-dev/admin-shared";
import { apiClient, setApiUrl } from "@halo-dev/admin-shared";
import { menus, minimenus, registerMenu } from "./router/menus.config";
// setup
import "./setup/setupStyles";
import { setupComponents } from "./setup/setupComponents";
// core modules
import { coreModules } from "./modules";
import { useScriptTag } from "@vueuse/core";
import { usePluginStore } from "@/stores/plugin";
import type { User } from "@halo-dev/api-client";
import { hasPermission } from "@/utils/permission";
import { useRoleStore } from "@/stores/role";

const app = createApp(App);

setupComponents(app);
setApiUrl(import.meta.env.VITE_API_URL);

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
      if ("parentName" in route) {
        router.addRoute(route.parentName, route.route);
      } else {
        router.addRoute(route);
      }
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

function loadStyle(href: string) {
  return new Promise(function (resolve, reject) {
    let shouldAppend = false;
    let el: HTMLLinkElement | null = document.querySelector(
      'script[src="' + href + '"]'
    );
    if (!el) {
      el = document.createElement("link");
      el.rel = "stylesheet";
      el.type = "text/css";
      el.href = href;
      shouldAppend = true;
    } else if (el.hasAttribute("data-loaded")) {
      resolve(el);
      return;
    }

    el.addEventListener("error", reject);
    el.addEventListener("abort", reject);
    el.addEventListener("load", function loadStyleHandler() {
      el?.setAttribute("data-loaded", "true");
      resolve(el);
    });

    if (shouldAppend) document.head.prepend(el);
  });
}

const pluginErrorMessages: Array<string> = [];

async function loadPluginModules() {
  const { data } =
    await apiClient.extension.plugin.listpluginHaloRunV1alpha1Plugin();

  // Get all started plugins
  const plugins = data.items.filter(
    (plugin) => plugin.status?.phase === "STARTED" && plugin.spec.enabled
  );

  for (const plugin of plugins) {
    const { entry, stylesheet } = plugin.status || {
      entry: "",
      stylesheet: "",
    };

    if (entry) {
      try {
        const { load } = useScriptTag(
          `${import.meta.env.VITE_API_URL}${plugin.status?.entry}`
        );

        await load();

        const pluginModule = window[plugin.metadata.name];

        if (pluginModule) {
          // @ts-ignore
          plugin.spec.module = pluginModule;
          registerModule(pluginModule);
        }
      } catch (e) {
        const message = `${plugin.metadata.name}: Failed load plugin entry module`;
        console.error(message, e);
        pluginErrorMessages.push(message);
      }
    }

    if (stylesheet) {
      try {
        await loadStyle(`${import.meta.env.VITE_API_URL}${stylesheet}`);
      } catch (e) {
        const message = `${plugin.metadata.name}: Failed load plugin stylesheet`;
        console.error(message, e);
        pluginErrorMessages.push(message);
      }
    }

    pluginStore.registerPlugin(plugin);
  }

  if (pluginErrorMessages.length > 0) {
    alert(pluginErrorMessages.join("\n"));
  }
}

async function loadCurrentUser() {
  const { data: user } = await apiClient.user.getCurrentUserDetail();
  app.provide<User>("currentUser", user);

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

      enable ? (el.style.backgroundColor = "red") : el.remove();
    }
  );
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
    loadCoreModules();
    await loadPluginModules();
    await loadCurrentUser();
    app.provide<MenuGroupType[]>("menus", menus);
    app.provide<MenuItemType[]>("minimenus", minimenus);
  } catch (e) {
    console.error(e);
  } finally {
    app.use(router);
    app.mount("#app");
  }
}
