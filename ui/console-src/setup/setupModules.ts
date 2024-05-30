import { i18n } from "@/locales";
import modules from "@console/modules";
import router from "@console/router";
import { usePluginModuleStore } from "@/stores/plugin";
import type { PluginModule, RouteRecordAppend } from "@halo-dev/console-shared";
import { useScriptTag } from "@vueuse/core";
import { Toast } from "@halo-dev/components";
import type { App } from "vue";
import type { RouteRecordRaw } from "vue-router";
import { loadStyle } from "@/utils/load-style";

export function setupCoreModules(app: App) {
  modules.forEach((module) => {
    registerModule(app, module, true);
  });
}

export async function setupPluginModules(app: App) {
  const pluginModuleStore = usePluginModuleStore();
  try {
    const { load } = useScriptTag(
      `/apis/api.console.halo.run/v1alpha1/plugins/-/bundle.js?t=${Date.now()}`
    );

    await load();

    const enabledPluginNames = window["enabledPluginNames"] as string[];

    enabledPluginNames.forEach((name) => {
      const module = window[name];
      if (module) {
        registerModule(app, module, false);
        pluginModuleStore.registerPluginModule(name, module);
      }
    });
  } catch (e) {
    const message = i18n.global.t("core.plugin.loader.toast.entry_load_failed");
    console.error(message, e);
    Toast.error(message);
  }

  try {
    await loadStyle(
      `/apis/api.console.halo.run/v1alpha1/plugins/-/bundle.css?t=${Date.now()}`
    );
  } catch (e) {
    const message = i18n.global.t("core.plugin.loader.toast.style_load_failed");
    console.error(message, e);
    Toast.error(message);
  }
}

function registerModule(app: App, pluginModule: PluginModule, core: boolean) {
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
        const parentRoute = router
          .getRoutes()
          .find((item) => item.name === route.parentName);
        if (parentRoute) {
          router.removeRoute(route.parentName);
          parentRoute.children = [...parentRoute.children, route.route];
          router.addRoute(parentRoute);
        }
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
