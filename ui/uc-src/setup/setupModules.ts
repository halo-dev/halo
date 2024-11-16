import { i18n } from "@/locales";
import { usePluginModuleStore } from "@/stores/plugin";
import { loadStyle } from "@/utils/load-style";
import { Toast } from "@halo-dev/components";
import type { PluginModule, RouteRecordAppend } from "@halo-dev/console-shared";
import modules from "@uc/modules";
import router from "@uc/router";
import { useScriptTag } from "@vueuse/core";
import type { App } from "vue";
import type { RouteRecordRaw } from "vue-router";

export function setupCoreModules(app: App) {
  modules.forEach((module) => registerModule(app, module, true));
}

export async function setupPluginModules(app: App) {
  const pluginModuleStore = usePluginModuleStore();

  try {
    await loadPluginBundle();
    await registerEnabledPlugins(app, pluginModuleStore);
    await loadPluginStyles();
  } catch (error) {
    handleError(error);
  }
}

async function loadPluginBundle() {
  const { load } = useScriptTag(
    `/apis/api.console.halo.run/v1alpha1/plugins/-/bundle.js?t=${Date.now()}`
  );
  await load();
}

async function registerEnabledPlugins(
  app: App,
  pluginModuleStore: ReturnType<typeof usePluginModuleStore>
) {
  const enabledPlugins = window["enabledPlugins"] as {
    name: string;
    value: string;
  }[];

  if (enabledPlugins) {
    enabledPlugins.forEach((plugin) =>
      registerPluginIfAvailable(app, pluginModuleStore, plugin.name)
    );
  }

  // @Deprecated: Compatibility solution, will be removed in the future
  const enabledPluginNames = window["enabledPluginNames"] as string[];
  if (enabledPluginNames) {
    enabledPluginNames.forEach((name) =>
      registerPluginIfAvailable(app, pluginModuleStore, name)
    );
  }
}

function registerPluginIfAvailable(
  app: App,
  pluginModuleStore: ReturnType<typeof usePluginModuleStore>,
  name: string
) {
  const module = window[name];
  if (module) {
    registerModule(app, module, false);
    pluginModuleStore.registerPluginModule(name, module);
  }
}

async function loadPluginStyles() {
  await loadStyle(
    `/apis/api.console.halo.run/v1alpha1/plugins/-/bundle.css?t=${Date.now()}`
  );
}

function handleError(error: unknown) {
  const message =
    error instanceof Error && error.message.includes("style")
      ? i18n.global.t("core.plugin.loader.toast.style_load_failed")
      : i18n.global.t("core.plugin.loader.toast.entry_load_failed");

  console.error(message, error);
  Toast.error(message);
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

  if (pluginModule.ucRoutes) {
    if (!Array.isArray(pluginModule.ucRoutes)) {
      return;
    }

    resetRouteMeta(pluginModule.ucRoutes);

    for (const route of pluginModule.ucRoutes) {
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
