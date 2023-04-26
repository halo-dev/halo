import { createApp } from "vue";
import type { DirectiveBinding } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import type { PluginModule, RouteRecordAppend } from "@halo-dev/console-shared";
import { Toast } from "@halo-dev/components";
import { apiClient } from "@/utils/api-client";
// setup
import "./setup/setupStyles";
import { setupComponents } from "./setup/setupComponents";
import { setupI18n, i18n, getBrowserLanguage } from "./locales";
// core modules
import { coreModules } from "./modules";
import { useScriptTag } from "@vueuse/core";
import { usePluginModuleStore } from "@/stores/plugin";
import { hasPermission } from "@/utils/permission";
import { useRoleStore } from "@/stores/role";
import type { RouteRecordRaw } from "vue-router";
import { useThemeStore } from "./stores/theme";
import { useSystemStatesStore } from "./stores/system-states";
import { useUserStore } from "./stores/user";
import { useSystemConfigMapStore } from "./stores/system-configmap";
import { setupVueQuery } from "./setup/setupVueQuery";

const app = createApp(App);

setupComponents(app);
setupI18n(app);
setupVueQuery(app);

app.use(createPinia());

function registerModule(pluginModule: PluginModule, core: boolean) {
  if (pluginModule.components) {
    Object.keys(pluginModule.components).forEach((key) => {
      const component = pluginModule.components?.[key];
      if (component) {
        app.component(key, component);
      }
    });
  }

  if (pluginModule.routes) {
    if (!Array.isArray(pluginModule.routes)) {
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

const pluginModuleStore = usePluginModuleStore();

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
  const { data } = await apiClient.plugin.listPlugins(
    {
      enabled: true,
      page: 0,
      size: 0,
    },
    { mute: true }
  );

  // Get all started plugins
  const plugins = data.items.filter((plugin) => {
    const { entry, stylesheet } = plugin.status || {};
    return plugin.status?.phase === "STARTED" && (!!entry || !!stylesheet);
  });

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
          registerModule(pluginModule, false);
          pluginModuleStore.registerPluginModule({
            ...pluginModule,
            extension: plugin,
          });
        }
      } catch (e) {
        const message = i18n.global.t(
          "core.plugin.loader.toast.entry_load_failed",
          { name: plugin.spec.displayName }
        );
        console.error(message, e);
        pluginErrorMessages.push(message);
      }
    }

    if (stylesheet) {
      try {
        await loadStyle(`${import.meta.env.VITE_API_URL}${stylesheet}`);
      } catch (e) {
        const message = i18n.global.t(
          "core.plugin.loader.toast.style_load_failed",
          { name: plugin.spec.displayName }
        );
        console.error(message, e);
        pluginErrorMessages.push(message);
      }
    }
  }

  if (pluginErrorMessages.length > 0) {
    pluginErrorMessages.forEach((message) => {
      Toast.error(message);
    });
  }
}

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
    loadCoreModules();

    const userStore = useUserStore();
    await userStore.fetchCurrentUser();

    // set locale
    i18n.global.locale.value =
      localStorage.getItem("locale") || getBrowserLanguage();

    if (userStore.isAnonymous) {
      return;
    }

    await loadUserPermissions();

    try {
      await loadPluginModules();
    } catch (e) {
      console.error("Failed to load plugins", e);
    }

    // load system setup state
    const systemStateStore = useSystemStatesStore();
    await systemStateStore.fetchSystemStates();

    // load system configMap
    const systemConfigMapStore = useSystemConfigMapStore();
    await systemConfigMapStore.fetchSystemConfigMap();

    if (systemStateStore.states.isSetup) {
      await loadActivatedTheme();
    }
  } catch (e) {
    console.error(e);
  } finally {
    app.use(router);
    app.mount("#app");
  }
}
