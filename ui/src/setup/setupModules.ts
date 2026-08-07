import type { FormKitLibrary } from "@formkit/core";
import {
  consoleApiClient,
  type UiPluginProvider,
  type UiPluginProviderDescriptor,
} from "@halo-dev/api-client";
import { Toast } from "@halo-dev/components";
import {
  stores,
  type PluginModule,
  type RouteRecordAppend,
  type UiPluginsHostStore,
} from "@halo-dev/ui-shared";
import type { App } from "vue";
import type { Router, RouteRecordRaw } from "vue-router";
import { collectPluginFormKitInputs } from "@/formkit/plugin-inputs";
import { i18n } from "@/locales";
import {
  usePluginModuleStore,
  type UiPluginDiagnostic,
  type UiPluginFailureStage,
} from "@/stores/plugin";
import { loadStyle } from "@/utils/load-style";
import type { SetupComponentsOptions } from "./setupComponents";

export type Platform = "console" | "uc";

type UiProviderRegistration = Pick<
  UiPluginProvider,
  "name" | "type" | "version"
>;
type UiProviderType = UiProviderRegistration["type"];

export interface LoadedPluginModule {
  name: string;
  type: UiProviderType;
  module: PluginModule;
}

interface UiPluginRuntimeDependencies {
  fetchProviders: () => Promise<UiPluginProviderDescriptor>;
  importModule: (url: string) => Promise<unknown>;
  loadScript: (url: string) => Promise<void>;
  loadStyle: (url: string, before: ChildNode | null) => Promise<unknown>;
}

interface RollbackHandle {
  label: string;
  rollback: () => void;
}

interface RegistrationFailure {
  error: unknown;
  incompleteRollback: string[];
}

interface ReplacedRoute {
  route: RouteRecordRaw;
  parentName?: string | symbol;
  nestedUnderAnonymousParent: boolean;
  owner?: UiProviderRegistration;
}

export async function setupUiPluginRuntime({
  app,
  router,
  platform,
  setupComponents,
  registeredFormKitInputs,
  runtime: runtimeOverrides,
}: {
  app: App;
  router: Router;
  platform: Platform;
  setupComponents: (options?: SetupComponentsOptions) => void;
  registeredFormKitInputs: FormKitLibrary;
  runtime?: Partial<UiPluginRuntimeDependencies>;
}) {
  const runtime = createRuntimeDependencies(runtimeOverrides);
  const pluginModuleStore = usePluginModuleStore();
  const registrationStore = stores.uiPlugins() as unknown as UiPluginsHostStore;
  pluginModuleStore.clearDiagnostics();

  let descriptor: UiPluginProviderDescriptor;
  try {
    descriptor = await runtime.fetchProviders();
  } catch (error) {
    setupComponents();
    notifyPluginLoadError(error);
    return [];
  }

  const providers = descriptor.providers;
  const registrationsByName = new Map(
    providers.map((provider) => [provider.name, provider])
  );
  const invalidProviders = providers.filter(
    (provider) => provider.kind === "invalid"
  );
  const legacyProviders = providers.filter(
    (provider) => provider.kind === "legacy"
  );
  const esmProviders = providers.filter((provider) => provider.kind === "esm");
  const styledProviders = providers.filter(
    (
      provider
    ): provider is UiPluginProvider & {
      style: string;
    } => Boolean(provider.style)
  );

  registrationStore._seed(
    providers.map((provider) => ({
      name: provider.name,
      type: provider.type,
      version: provider.version,
      status: provider.kind === "invalid" ? "failed" : "pending",
    }))
  );

  const report = (
    registration: UiProviderRegistration,
    stage: UiPluginFailureStage,
    error: unknown,
    details: Pick<UiPluginDiagnostic, "incompleteRollback"> = {}
  ) => {
    const diagnostic: UiPluginDiagnostic = {
      name: registration.name,
      type: registration.type,
      stage,
      message: errorMessage(error),
      ...details,
    };
    registrationStore._setStatus(registration.name, "failed");
    pluginModuleStore.recordDiagnostic(diagnostic);
    console.error("[Halo UI provider]", diagnostic, error);
  };

  for (const invalid of invalidProviders) {
    report(
      invalid,
      "discovery",
      invalid.reason || `Provider "${invalid.name}" is invalid.`
    );
  }

  const styleInsertionPoint = document.head.firstChild;
  const styleLoads = styledProviders.map((provider) =>
    runtime.loadStyle(provider.style, styleInsertionPoint)
  );
  const legacyScriptLoad =
    legacyProviders.length > 0
      ? descriptor.legacyScript
        ? runtime.loadScript(descriptor.legacyScript)
        : Promise.reject(
            new Error("The UI provider descriptor has no legacy script.")
          )
      : Promise.resolve();
  const esmImports = esmProviders.map((provider) =>
    provider.entry
      ? runtime.importModule(provider.entry)
      : Promise.reject(
          new Error(`ESM provider "${provider.name}" has no entry.`)
        )
  );

  const [styleResults, legacyScriptResult, esmImportResults] =
    await Promise.all([
      Promise.allSettled(styleLoads),
      settled(legacyScriptLoad),
      Promise.allSettled(esmImports),
    ]);

  const failedNames = new Set(
    invalidProviders.map((provider) => provider.name)
  );
  const styleFailed = styleResults.some(
    (result) => result.status === "rejected"
  );
  for (const [index, result] of styleResults.entries()) {
    if (result.status === "fulfilled") {
      continue;
    }
    const provider = styledProviders[index];
    failedNames.add(provider.name);
    report(provider, "style", result.reason);
  }

  if (legacyScriptResult.status === "rejected") {
    for (const provider of legacyProviders) {
      failedNames.add(provider.name);
      report(provider, "entry", legacyScriptResult.reason);
    }
  }

  const loadedByName = new Map<string, LoadedPluginModule>();
  const legacyProvidersWithoutModule = new Set<string>();
  if (legacyScriptResult.status === "fulfilled") {
    for (const provider of legacyProviders) {
      const module = window[provider.name] as unknown;
      if (module === undefined || module === null) {
        legacyProvidersWithoutModule.add(provider.name);
        continue;
      }
      if (!isPluginModule(module)) {
        failedNames.add(provider.name);
        report(
          provider,
          "export",
          new Error(
            `Legacy provider "${provider.name}" did not expose a PluginModule.`
          )
        );
        continue;
      }
      loadedByName.set(provider.name, {
        name: provider.name,
        type: provider.type,
        module,
      });
    }
  }

  for (const [index, result] of esmImportResults.entries()) {
    const provider = esmProviders[index];
    if (result.status === "rejected") {
      failedNames.add(provider.name);
      report(provider, "entry", result.reason);
      continue;
    }
    const module = (result.value as { default?: unknown })?.default;
    if (!isPluginModule(module)) {
      failedNames.add(provider.name);
      report(
        provider,
        "export",
        new Error(
          `ESM provider "${provider.name}" must default-export a PluginModule object.`
        )
      );
      continue;
    }
    loadedByName.set(provider.name, {
      name: provider.name,
      type: provider.type,
      module,
    });
  }

  const loadableModules = providers
    .filter((provider) => !failedNames.has(provider.name))
    .map((provider) => loadedByName.get(provider.name))
    .filter((module): module is LoadedPluginModule => Boolean(module));

  setupComponents({
    formkitInputs: collectPluginFormKitInputs(
      loadableModules.filter((module) => module.type === "plugin"),
      registeredFormKitInputs
    ),
  });

  const routeOwners = new Map<string | symbol, UiProviderRegistration>();
  const componentOwners = new WeakMap<object, UiProviderRegistration>();
  installProviderErrorHooks({
    app,
    router,
    registrationsByName,
    routeOwners,
    componentOwners,
    report,
  });

  const registeredModules: LoadedPluginModule[] = [];
  for (const provider of providers) {
    if (failedNames.has(provider.name)) {
      continue;
    }
    const loaded = loadedByName.get(provider.name);
    if (!loaded) {
      if (legacyProvidersWithoutModule.has(provider.name)) {
        registrationStore._setStatus(provider.name, "registered");
        continue;
      }
      failedNames.add(provider.name);
      report(
        provider,
        "export",
        new Error(`Provider "${provider.name}" has no loadable module.`)
      );
      continue;
    }
    const failure = registerPluginModule({
      app,
      router,
      platform,
      registration: provider,
      module: loaded.module,
      routeOwners,
      componentOwners,
      report,
    });
    if (failure) {
      failedNames.add(provider.name);
      report(provider, "registration", failure.error, {
        incompleteRollback: failure.incompleteRollback,
      });
      continue;
    }
    registrationStore._setStatus(provider.name, "registered");
    registeredModules.push(loaded);
  }

  if (styleFailed || pluginModuleStore.diagnostics.length > 0) {
    Toast.error(
      i18n.global.t(
        styleFailed
          ? "core.plugin.loader.toast.style_load_failed"
          : "core.plugin.loader.toast.entry_load_failed"
      )
    );
  }
  return registeredModules;
}

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
  for (const [name, module] of Object.entries(modules)) {
    resetRouteMetaAndWrapChunks(module.routes, true);
    resetRouteMetaAndWrapChunks(module.ucRoutes, true);
    commitJsModule({
      app,
      router,
      platform,
      name,
      module,
      core: true,
      rollbackHandles: [],
    });
  }
}

export function notifyPluginLoadError(error: unknown) {
  const message =
    error instanceof Error && error.message.includes("style")
      ? i18n.global.t("core.plugin.loader.toast.style_load_failed")
      : i18n.global.t("core.plugin.loader.toast.entry_load_failed");

  console.error(message, error);
  Toast.error(message);
}

function registerPluginModule({
  app,
  router,
  platform,
  registration,
  module,
  routeOwners,
  componentOwners,
  report,
}: {
  app: App;
  router: Router;
  platform: Platform;
  registration: UiProviderRegistration;
  module: PluginModule;
  routeOwners: Map<string | symbol, UiProviderRegistration>;
  componentOwners: WeakMap<object, UiProviderRegistration>;
  report: (
    registration: UiProviderRegistration,
    stage: UiPluginFailureStage,
    error: unknown
  ) => void;
}): RegistrationFailure | undefined {
  const rollbackHandles: RollbackHandle[] = [];
  const incompleteRollback = module.formkit?.inputs ? ["formkit"] : [];
  try {
    preparePluginModule(module, registration, report);
    commitJsModule({
      app,
      router,
      platform,
      name: registration.name,
      module,
      core: false,
      rollbackHandles,
      registration,
      routeOwners,
      componentOwners,
    });
    return undefined;
  } catch (error) {
    for (const handle of rollbackHandles.reverse()) {
      try {
        handle.rollback();
      } catch (rollbackError) {
        incompleteRollback.push(handle.label);
        console.error(
          `[Halo UI provider] Failed to roll back ${handle.label} for ${registration.name}.`,
          rollbackError
        );
      }
    }
    return { error, incompleteRollback: [...new Set(incompleteRollback)] };
  }
}

function commitJsModule({
  app,
  router,
  platform,
  name,
  module,
  core,
  rollbackHandles,
  registration,
  routeOwners,
  componentOwners,
}: {
  app: App;
  router: Router;
  platform: Platform;
  name: string;
  module: PluginModule;
  core: boolean;
  rollbackHandles: RollbackHandle[];
  registration?: UiProviderRegistration;
  routeOwners?: Map<string | symbol, UiProviderRegistration>;
  componentOwners?: WeakMap<object, UiProviderRegistration>;
}) {
  const pluginModuleStore = usePluginModuleStore();
  const routes = platform === "console" ? module.routes : module.ucRoutes;
  assignInternalRouteParentNames(routes);
  const previousModule = pluginModuleStore.pluginModuleMap[name];
  pluginModuleStore.registerPluginModule(name, module);
  rollbackHandles.push({
    label: "plugin module store",
    rollback: () => {
      if (previousModule) {
        pluginModuleStore.registerPluginModule(name, previousModule);
      } else {
        pluginModuleStore.unregisterPluginModule(name);
      }
    },
  });

  if (module.components) {
    for (const [componentName, component] of Object.entries(
      module.components
    )) {
      const components = app._context.components;
      const hadPrevious = Object.prototype.hasOwnProperty.call(
        components,
        componentName
      );
      const previousComponent = components[componentName];
      app.component(componentName, component);
      if (registration && isWeakMapKey(component)) {
        componentOwners?.set(component, registration);
      }
      rollbackHandles.push({
        label: `component ${componentName}`,
        rollback: () => {
          if (hadPrevious && previousComponent) {
            components[componentName] = previousComponent;
          } else {
            delete components[componentName];
          }
          if (isWeakMapKey(component)) {
            componentOwners?.delete(component);
          }
        },
      });
    }
  }

  if (!routes) {
    return;
  }
  for (const route of routes) {
    if ("parentName" in route) {
      appendChildRoute({
        router,
        route,
        rollbackHandles,
        registration,
        routeOwners,
      });
    } else {
      const replacedRoute = findReplacedRoute(router, route, routeOwners);
      const removeRoute = router.addRoute(route);
      if (replacedRoute) {
        unregisterRouteOwners(replacedRoute.route, routeOwners);
      }
      if (registration) {
        registerRouteOwners(route, registration, routeOwners);
      }
      rollbackHandles.push({
        label: `route ${String(route.name || route.path)}`,
        rollback: () => {
          removeRoute();
          unregisterRouteOwners(route, routeOwners);
          restoreReplacedRoute(router, replacedRoute, routeOwners);
        },
      });
    }
  }

  function appendChildRoute({
    router,
    route,
    rollbackHandles,
    registration,
    routeOwners,
  }: {
    router: Router;
    route: RouteRecordAppend;
    rollbackHandles: RollbackHandle[];
    registration?: UiProviderRegistration;
    routeOwners?: Map<string | symbol, UiProviderRegistration>;
  }) {
    const parentRoute = router
      .getRoutes()
      .find((item) => item.name === route.parentName);
    if (!parentRoute) {
      return;
    }
    const previousChildren = [...parentRoute.children];
    rollbackHandles.push({
      label: `child route ${String(route.route.name || route.route.path)}`,
      rollback: () => {
        router.removeRoute(route.parentName);
        parentRoute.children = previousChildren;
        router.addRoute(parentRoute as unknown as RouteRecordRaw);
        unregisterRouteOwners(route.route, routeOwners);
      },
    });
    router.removeRoute(route.parentName);
    parentRoute.children = [...parentRoute.children, route.route];
    router.addRoute(parentRoute as unknown as RouteRecordRaw);
    if (registration) {
      registerRouteOwners(route.route, registration, routeOwners);
    }
  }

  if (core) {
    rollbackHandles.length = 0;
  }
}

function assignInternalRouteParentNames(
  routes: RouteRecordRaw[] | RouteRecordAppend[] | undefined
) {
  if (!routes) {
    return;
  }
  for (const routeItem of routes) {
    const route = "parentName" in routeItem ? routeItem.route : routeItem;
    if (!route.name && route.children?.length) {
      route.name = Symbol(`Halo route parent: ${route.path}`);
    }
    assignInternalRouteParentNames(route.children);
  }
}

function findReplacedRoute(
  router: Router,
  route: RouteRecordRaw,
  routeOwners?: Map<string | symbol, UiProviderRegistration>
): ReplacedRoute | undefined {
  if (!route.name) {
    return undefined;
  }
  const currentRoutes = router.getRoutes();
  const existing = currentRoutes.find((item) => item.name === route.name);
  if (!existing || existing.name === undefined) {
    return undefined;
  }
  const parent = findRouteParent(currentRoutes, existing.name);
  return {
    route: existing as unknown as RouteRecordRaw,
    parentName: parent?.name,
    nestedUnderAnonymousParent: Boolean(parent && !parent.name),
    owner: routeOwners?.get(route.name),
  };
}

function findRouteParent(
  routes: ReturnType<Router["getRoutes"]>,
  name: string | symbol
) {
  return routes.find((route) =>
    route.children?.some((child) => child.name === name)
  );
}

function restoreReplacedRoute(
  router: Router,
  replacedRoute: ReplacedRoute | undefined,
  routeOwners?: Map<string | symbol, UiProviderRegistration>
) {
  if (!replacedRoute) {
    return;
  }
  if (replacedRoute.nestedUnderAnonymousParent) {
    throw new Error(
      "Cannot restore a replaced route under an anonymous parent registered outside Halo's managed route setup."
    );
  }
  if (replacedRoute.parentName) {
    router.addRoute(replacedRoute.parentName, replacedRoute.route);
  } else {
    router.addRoute(replacedRoute.route);
  }
  if (replacedRoute.owner) {
    registerRouteOwners(replacedRoute.route, replacedRoute.owner, routeOwners);
  }
}

function preparePluginModule(
  module: PluginModule,
  registration: UiProviderRegistration,
  report: (
    registration: UiProviderRegistration,
    stage: UiPluginFailureStage,
    error: unknown
  ) => void
) {
  if (module.components && !isRecord(module.components)) {
    throw new TypeError("PluginModule.components must be an object.");
  }
  if (module.routes && !Array.isArray(module.routes)) {
    throw new TypeError("PluginModule.routes must be an array.");
  }
  if (module.ucRoutes && !Array.isArray(module.ucRoutes)) {
    throw new TypeError("PluginModule.ucRoutes must be an array.");
  }
  if (module.formkit && !isRecord(module.formkit)) {
    throw new TypeError("PluginModule.formkit must be an object.");
  }
  if (module.extensionPoints && !isRecord(module.extensionPoints)) {
    throw new TypeError("PluginModule.extensionPoints must be an object.");
  }
  resetRouteMetaAndWrapChunks(module.routes, false, registration, report);
  resetRouteMetaAndWrapChunks(module.ucRoutes, false, registration, report);
}

function resetRouteMetaAndWrapChunks(
  routes: RouteRecordRaw[] | RouteRecordAppend[] | undefined,
  core: boolean,
  registration?: UiProviderRegistration,
  report?: (
    registration: UiProviderRegistration,
    stage: UiPluginFailureStage,
    error: unknown
  ) => void
) {
  if (!routes) {
    return;
  }
  for (const routeItem of routes) {
    const route = "parentName" in routeItem ? routeItem.route : routeItem;
    if (route.meta?.menu) {
      route.meta = { ...route.meta, core };
    }
    if (registration && report) {
      wrapRouteComponentLoaders(route, registration, report);
    }
    resetRouteMetaAndWrapChunks(route.children, core, registration, report);
  }
}

function wrapRouteComponentLoaders(
  route: RouteRecordRaw,
  registration: UiProviderRegistration,
  report: (
    registration: UiProviderRegistration,
    stage: UiPluginFailureStage,
    error: unknown
  ) => void
) {
  if ("component" in route && typeof route.component === "function") {
    route.component = wrapChunkLoader(
      route.component as unknown as ComponentLoader,
      registration,
      report
    ) as typeof route.component;
  }
  if ("components" in route && route.components) {
    for (const [viewName, component] of Object.entries(route.components)) {
      if (typeof component === "function") {
        route.components[viewName] = wrapChunkLoader(
          component as unknown as ComponentLoader,
          registration,
          report
        ) as typeof component;
      }
    }
  }
}

type ComponentLoader = (...args: unknown[]) => unknown;

function wrapChunkLoader<T extends ComponentLoader>(
  loader: T,
  registration: UiProviderRegistration,
  report: (
    registration: UiProviderRegistration,
    stage: UiPluginFailureStage,
    error: unknown
  ) => void
) {
  return ((...args: Parameters<T>) => {
    try {
      const result = loader(...args);
      if (isPromiseLike(result)) {
        return result.catch((error: unknown) => {
          report(registration, "chunk", error);
          throw error;
        });
      }
      return result;
    } catch (error) {
      report(registration, "chunk", error);
      throw error;
    }
  }) as T;
}

function installProviderErrorHooks({
  app,
  router,
  registrationsByName,
  routeOwners,
  componentOwners,
  report,
}: {
  app: App;
  router: Router;
  registrationsByName: Map<string, UiProviderRegistration>;
  routeOwners: Map<string | symbol, UiProviderRegistration>;
  componentOwners: WeakMap<object, UiProviderRegistration>;
  report: (
    registration: UiProviderRegistration,
    stage: UiPluginFailureStage,
    error: unknown
  ) => void;
}) {
  router.onError((error, to) => {
    const owner = [...to.matched]
      .reverse()
      .map((route) => route.name && routeOwners.get(route.name))
      .find(Boolean);
    if (owner) {
      report(owner, "chunk", error);
    }
  });

  const previousErrorHandler = app.config.errorHandler;
  app.config.errorHandler = (error, instance, info) => {
    const component = instance?.$?.type;
    const owner = isWeakMapKey(component)
      ? componentOwners.get(component)
      : undefined;
    if (
      owner ||
      (typeof info === "string" && info.includes("async component"))
    ) {
      const fallbackOwner =
        owner ||
        [...registrationsByName.values()].find(
          (registration) => registration.name === componentName(component)
        );
      if (fallbackOwner) {
        report(fallbackOwner, "chunk", error);
      }
    }
    if (previousErrorHandler) {
      previousErrorHandler(error, instance, info);
    } else if (!owner) {
      console.error("[Vue]", info, error);
    }
  };
}

function componentName(component: unknown) {
  if (isWeakMapKey(component) && "name" in component) {
    return typeof component.name === "string" ? component.name : undefined;
  }
  return undefined;
}

function registerRouteOwners(
  route: RouteRecordRaw,
  registration: UiProviderRegistration,
  routeOwners?: Map<string | symbol, UiProviderRegistration>
) {
  if (route.name) {
    routeOwners?.set(route.name, registration);
  }
  for (const child of route.children || []) {
    registerRouteOwners(child, registration, routeOwners);
  }
}

function unregisterRouteOwners(
  route: RouteRecordRaw,
  routeOwners?: Map<string | symbol, UiProviderRegistration>
) {
  if (route.name) {
    routeOwners?.delete(route.name);
  }
  for (const child of route.children || []) {
    unregisterRouteOwners(child, routeOwners);
  }
}

function isPluginModule(value: unknown): value is PluginModule {
  return isRecord(value);
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}

function isWeakMapKey(value: unknown): value is object {
  return (
    (typeof value === "object" && value !== null) || typeof value === "function"
  );
}

function isPromiseLike(value: unknown): value is Promise<unknown> {
  return (
    (typeof value === "object" || typeof value === "function") &&
    value !== null &&
    "then" in value &&
    typeof value.then === "function"
  );
}

function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : String(error);
}

async function settled<T>(
  promise: Promise<T>
): Promise<PromiseSettledResult<T>> {
  const [result] = await Promise.allSettled([promise]);
  return result;
}

function createRuntimeDependencies(
  overrides: Partial<UiPluginRuntimeDependencies> = {}
): UiPluginRuntimeDependencies {
  return {
    fetchProviders: async () => {
      const { data } = await consoleApiClient.uiPlugin.fetchUiPluginProviders({
        mute: true,
      });
      return data;
    },
    importModule: (url) =>
      import(/* @vite-ignore */ resolveProviderEntryUrl(url)),
    loadScript: loadLegacyScript,
    loadStyle,
    ...overrides,
  };
}

export function resolveProviderEntryUrl(
  url: string,
  pageOrigin = window.location.origin
) {
  return new URL(url, pageOrigin).href;
}

function loadLegacyScript(src: string) {
  return new Promise<void>((resolve, reject) => {
    const script = document.createElement("script");
    script.src = src;
    script.addEventListener("load", () => resolve(), { once: true });
    script.addEventListener("error", reject, { once: true });
    // TODO(Halo 3): Remove after legacy IIFE UI provider support ends.
    document.head.appendChild(script);
  });
}
