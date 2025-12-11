import { i18n } from "@/locales";
import { usePluginModuleStore } from "@/stores/plugin";
import { loadStyle } from "@/utils/load-style";
import { Toast } from "@halo-dev/components";
import type { PluginModule, RouteRecordAppend } from "@halo-dev/ui-shared";
import { useScriptTag } from "@vueuse/core";
import type { App } from "vue";
import type { Router, RouteRecordRaw } from "vue-router";

export type Platform = "console" | "uc";

export function setupCoreModules({
  app,
  router,
  platform,
  modules,
}: {
  app: App;
  router: Router;
  platform: Platform;
  modules: Record<string, PluginModule>;
}) {
  for (const [key, module] of Object.entries(modules)) {
    initJsModule({
      app,
      router,
      platform,
      name: key,
      jsModule: module,
      core: true,
    });
  }
}

export async function setupPluginModules({
  app,
  router,
  platform,
}: {
  app: App;
  router: Router;
  platform: Platform;
}) {
  try {
    await loadPluginBundle();

    const enabledPlugins = window["enabledPlugins"] as {
      name: string;
      value: string;
    }[];

    for (const plugin of enabledPlugins || []) {
      const module = window[plugin.name];
      if (!module) {
        continue;
      }
      initJsModule({
        app,
        router,
        platform,
        name: plugin.name,
        jsModule: module,
        core: false,
      });
    }

    await loadPluginStyles();
  } catch (error) {
    const message =
      error instanceof Error && error.message.includes("style")
        ? i18n.global.t("core.plugin.loader.toast.style_load_failed")
        : i18n.global.t("core.plugin.loader.toast.entry_load_failed");

    console.error(message, error);
    Toast.error(message);
  }
}

async function loadPluginBundle() {
  const { load } = useScriptTag(
    `/apis/api.console.halo.run/v1alpha1/plugins/-/bundle.js?t=${Date.now()}`
  );
  await load();
}

async function loadPluginStyles() {
  await loadStyle(
    `/apis/api.console.halo.run/v1alpha1/plugins/-/bundle.css?t=${Date.now()}`
  );
}

function initJsModule(options: {
  app: App;
  router: Router;
  platform: Platform;
  name: string;
  jsModule: PluginModule;
  core: boolean;
}) {
  const { app, router, platform, name, jsModule, core } = options;

  const pluginModuleStore = usePluginModuleStore();

  pluginModuleStore.registerPluginModule(name, jsModule);

  if (jsModule.components) {
    for (const [key, component] of Object.entries(jsModule.components)) {
      if (component) {
        app.component(key, component);
      }
    }
  }

  let routes: RouteRecordRaw[] | RouteRecordAppend[] | undefined;

  switch (platform) {
    case "console":
      routes = jsModule.routes;
      break;
    case "uc":
      routes = jsModule.ucRoutes;
      break;
    default:
      throw new Error(`Invalid platform: ${platform}`);
  }

  if (!routes) {
    return;
  }

  if (!Array.isArray(routes)) {
    return;
  }

  resetRouteMeta(routes);

  for (const route of routes) {
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
