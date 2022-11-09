import { createApp } from "vue";
import type { DirectiveBinding } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import type { Plugin, RouteRecordAppend } from "@halo-dev/console-shared";
import { Toast } from "@halo-dev/components";
import { apiClient } from "@/utils/api-client";
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
import type { RouteRecordRaw } from "vue-router";
import { useThemeStore } from "./stores/theme";
import { useSystemStatesStore } from "./stores/system-states";

const app = createApp(App);

setupComponents(app);

app.use(createPinia());

function registerModule(pluginModule: Plugin, core: boolean) {
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

    resetRouteMeta(pluginModule.routes);

    for (const route of pluginModule.routes) {
      if ("parentName" in route) {
        router.addRoute(route.parentName, route.route);
      } else {
        router.addRoute(route);
      }
    }
  }

  function resetRouteMeta(routes: RouteRecordRaw[] | RouteRecordAppend[]) {
    for (const route of routes) {
      if ("parentName" in route) {
        if (route.route.meta?.menu) {
          route.route.meta = {
            ...route.route.meta,
            core,
          };
        }
        if (route.route.children) {
          resetRouteMeta(route.route.children);
        }
      } else {
        if (route.meta?.menu) {
          route.meta = {
            ...route.meta,
            core,
          };
        }
        if (route.children) {
          resetRouteMeta(route.children);
        }
      }
    }
  }
}

function loadCoreModules() {
  coreModules.forEach((module) => {
    registerModule(module, true);
  });
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
          registerModule(pluginModule, false);
        }
      } catch (e) {
        const message = `${plugin.metadata.name}: 加载插件入口文件失败`;
        console.error(message, e);
        pluginErrorMessages.push(message);
      }
    }

    if (stylesheet) {
      try {
        await loadStyle(`${import.meta.env.VITE_API_URL}${stylesheet}`);
      } catch (e) {
        const message = `${plugin.metadata.name}: 加载插件样式文件失败`;
        console.error(message, e);
        pluginErrorMessages.push(message);
      }
    }

    pluginStore.registerPlugin(plugin);
  }

  if (pluginErrorMessages.length > 0) {
    pluginErrorMessages.forEach((message) => {
      Toast.error(message);
    });
  }
}

let currentUser: User | undefined = undefined;

async function loadCurrentUser() {
  const { data: user } = await apiClient.user.getCurrentUserDetail();
  app.provide<User>("currentUser", user);
  currentUser = user;

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
    loadCoreModules();

    await loadCurrentUser();

    if (!currentUser) {
      return;
    }

    await loadActivatedTheme();

    try {
      await loadPluginModules();
    } catch (e) {
      console.error("Failed to load plugins", e);
    }

    const systemStateStore = useSystemStatesStore();
    await systemStateStore.fetchSystemStates();
  } catch (e) {
    console.error(e);
  } finally {
    app.use(router);
    app.mount("#app");
  }
}
