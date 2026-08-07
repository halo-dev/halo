import type { UiPluginProviderDescriptor } from "@halo-dev/api-client";
import type { PluginModule } from "@halo-dev/ui-shared";
import { stores } from "@halo-dev/ui-shared";
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { createApp } from "vue";
import {
  createMemoryHistory,
  createRouter as createVueRouter,
  type Router,
  type RouteRecordRaw,
} from "vue-router";
import { usePluginModuleStore } from "@/stores/plugin";
import { resolveProviderEntryUrl, setupUiPluginRuntime } from "./setupModules";

const RootComponent = { template: "<div />" };

const mocks = vi.hoisted(() => ({
  toastError: vi.fn(),
}));

vi.mock("@halo-dev/components", () => ({
  Toast: {
    error: mocks.toastError,
  },
}));

vi.mock("@/locales", () => ({
  i18n: {
    global: {
      t: (key: string) => key,
    },
  },
}));

describe("resolveProviderEntryUrl", () => {
  it("resolves a provider entry against the Halo page origin", () => {
    expect(
      resolveProviderEntryUrl(
        "/plugins/content-tools/assets/console/main.js?v=version",
        "http://localhost:8090"
      )
    ).toBe(
      "http://localhost:8090/plugins/content-tools/assets/console/main.js?v=version"
    );
  });
});

describe("setupUiPluginRuntime", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
    document.head.innerHTML = "";
    delete window["legacy-plugin"];
  });

  it("seeds metadata before evaluation and registers mixed modules in descriptor order", async () => {
    const legacyModule = pluginModuleWithRoute("LegacyRoute");
    const esmAModule = pluginModuleWithRoute("EsmARoute");
    const esmBModule = pluginModuleWithRoute("EsmBRoute");
    const esmA = deferred<unknown>();
    const esmB = deferred<unknown>();
    const importModule = vi.fn((url: string) => {
      expect(stores.uiPlugins().get("esm-a")?.status).toBe("pending");
      return url.includes("esm-a") ? esmA.promise : esmB.promise;
    });
    const loadStyle = vi.fn().mockResolvedValue(undefined);
    const loadScript = vi.fn(async () => {
      window["legacy-plugin"] = legacyModule;
    });
    const { router, addRoute } = createRouter();
    const setupComponents = vi.fn();

    const setup = setupUiPluginRuntime({
      app: createApp(RootComponent),
      router,
      platform: "console",
      setupComponents,
      registeredFormKitInputs: {},
      runtime: {
        fetchProviders: async () => mixedDescriptor(),
        importModule,
        loadScript,
        loadStyle,
      },
    });

    await vi.waitFor(() => expect(importModule).toHaveBeenCalledTimes(2));
    esmA.resolve({ default: esmAModule });
    await Promise.resolve();
    esmB.resolve({ default: esmBModule });
    const modules = await setup;

    expect(modules.map((module) => module.name)).toEqual([
      "legacy-plugin",
      "esm-b",
      "esm-a",
    ]);
    expect(addRoute.mock.calls.map(([route]) => route.name)).toEqual([
      "LegacyRoute",
      "EsmBRoute",
      "EsmARoute",
    ]);
    expect(loadScript).toHaveBeenCalledWith(
      "/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.js?v=g1"
    );
    expect(loadStyle.mock.calls.map(([url]) => url)).toEqual([
      "/plugins/legacy-plugin/assets/ui/style.css?v=g1",
      "/plugins/esm-b/assets/ui/style.css?v=g1",
    ]);
    expect(stores.uiPlugins().registrations).toEqual([
      expect.objectContaining({ name: "legacy-plugin", status: "registered" }),
      expect.objectContaining({ name: "esm-b", status: "registered" }),
      expect.objectContaining({ name: "invalid", status: "failed" }),
      expect.objectContaining({ name: "esm-a", status: "registered" }),
    ]);
    expect(setupComponents).toHaveBeenCalledTimes(1);
    expect(mocks.toastError).toHaveBeenCalledTimes(1);
    expect(usePluginModuleStore().diagnostics).toEqual([
      expect.objectContaining({ name: "invalid", stage: "discovery" }),
    ]);
  });

  it("isolates a direct stylesheet failure to its provider", async () => {
    const styleA = deferred<unknown>();
    const styleB = deferred<unknown>();
    const loadStyle = vi.fn((url: string) =>
      url.includes("/a/") ? styleA.promise : styleB.promise
    );
    const descriptor = esmDescriptor(["a", "b"]);
    const { router } = createRouter();

    const setup = setupUiPluginRuntime({
      app: createApp(RootComponent),
      router,
      platform: "console",
      setupComponents: vi.fn(),
      registeredFormKitInputs: {},
      runtime: {
        fetchProviders: async () => descriptor,
        importModule: async (url) => ({
          default: pluginModuleWithRoute(
            url.includes("/a/") ? "RouteA" : "RouteB"
          ),
        }),
        loadStyle,
      },
    });

    await vi.waitFor(() => expect(loadStyle).toHaveBeenCalledTimes(2));
    styleA.reject(new Error("provider a style failed"));
    styleB.resolve(undefined);
    const modules = await setup;

    expect(loadStyle.mock.calls.map(([url]) => url)).toEqual([
      "/plugins/a/assets/ui/style.css?v=version",
      "/plugins/b/assets/ui/style.css?v=version",
    ]);
    expect(modules.map((module) => module.name)).toEqual(["b"]);
    expect(stores.uiPlugins().get("a")?.status).toBe("failed");
    expect(stores.uiPlugins().get("b")?.status).toBe("registered");
    expect(usePluginModuleStore().diagnostics).toEqual([
      expect.objectContaining({ name: "a", stage: "style" }),
    ]);
    expect(mocks.toastError).toHaveBeenCalledWith(
      "core.plugin.loader.toast.style_load_failed"
    );
  });

  it("starts 50 provider styles and entries before waiting for settlement", async () => {
    const names = Array.from({ length: 50 }, (_, index) => `provider-${index}`);
    const gate = deferred<void>();
    const loadStyle = vi.fn(() => gate.promise);
    const importModule = vi.fn((url: string) =>
      gate.promise.then(() => ({
        default: pluginModuleWithRoute(
          `Route${url.match(/provider-(\d+)/)?.[1]}`
        ),
      }))
    );
    const { router } = createRouter();

    const setup = setupUiPluginRuntime({
      app: createApp(RootComponent),
      router,
      platform: "console",
      setupComponents: vi.fn(),
      registeredFormKitInputs: {},
      runtime: {
        fetchProviders: async () => esmDescriptor(names),
        importModule,
        loadStyle,
      },
    });

    await vi.waitFor(() => {
      expect(loadStyle).toHaveBeenCalledTimes(50);
      expect(importModule).toHaveBeenCalledTimes(50);
    });
    gate.resolve();

    await expect(setup).resolves.toHaveLength(50);
  });

  it("rolls back supported registrations in reverse and continues", async () => {
    const removedFirstRoute = vi.fn();
    const { router, addRoute } = createRouter((route) => {
      if (route.name === "BadSecond") {
        throw new Error("route commit failed");
      }
      return route.name === "BadFirst" ? removedFirstRoute : vi.fn();
    });
    const badComponent = {};
    const badModule: PluginModule = {
      components: { BadComponent: badComponent },
      formkit: {
        inputs: {
          bad: { type: "input", schema: [] },
        },
      },
      routes: [route("BadFirst"), route("BadSecond")],
    } as PluginModule;
    const goodModule = pluginModuleWithRoute("GoodRoute");
    const app = createApp(RootComponent);

    await setupUiPluginRuntime({
      app,
      router,
      platform: "console",
      setupComponents: vi.fn(),
      registeredFormKitInputs: {},
      runtime: {
        fetchProviders: async () => esmDescriptor(["bad", "good"]),
        importModule: async (url) => ({
          default: url.includes("/bad/") ? badModule : goodModule,
        }),
        loadStyle: async () => undefined,
      },
    });

    expect(addRoute).toHaveBeenCalledWith(
      expect.objectContaining({ name: "GoodRoute" })
    );
    expect(removedFirstRoute).toHaveBeenCalledTimes(1);
    expect(app._context.components["BadComponent"]).toBeUndefined();
    expect(usePluginModuleStore().pluginModuleMap["bad"]).toBeUndefined();
    expect(usePluginModuleStore().pluginModuleMap["good"]).toBe(goodModule);
    expect(stores.uiPlugins().get("bad")?.status).toBe("failed");
    expect(stores.uiPlugins().get("good")?.status).toBe("registered");
    expect(usePluginModuleStore().diagnostics).toEqual([
      expect.objectContaining({
        name: "bad",
        stage: "registration",
        incompleteRollback: ["formkit"],
      }),
    ]);
    expect(mocks.toastError).toHaveBeenCalledTimes(1);
  });

  it("keeps last-registration-wins for successful named route conflicts", async () => {
    const firstRoute = route("SharedRoute", "/first");
    const secondRoute = route("SharedRoute", "/second");
    const { router } = createStatefulRouter();

    await setupUiPluginRuntime({
      app: createApp(RootComponent),
      router,
      platform: "console",
      setupComponents: vi.fn(),
      registeredFormKitInputs: {},
      runtime: {
        fetchProviders: async () => esmDescriptor(["first", "second"]),
        importModule: async (url) => ({
          default: {
            routes: [url.includes("/first/") ? firstRoute : secondRoute],
          },
        }),
        loadStyle: async () => undefined,
      },
    });

    expect(
      router.getRoutes().find((item) => item.name === "SharedRoute")?.path
    ).toBe("/second");
    expect(stores.uiPlugins().get("first")?.status).toBe("registered");
    expect(stores.uiPlugins().get("second")?.status).toBe("registered");
  });

  it("restores a previously replaced named route when registration fails", async () => {
    const firstRoute = route("SharedRoute", "/first");
    const replacementRoute = route("SharedRoute", "/replacement");
    const { router } = createStatefulRouter([], "FailRoute");

    await setupUiPluginRuntime({
      app: createApp(RootComponent),
      router,
      platform: "console",
      setupComponents: vi.fn(),
      registeredFormKitInputs: {},
      runtime: {
        fetchProviders: async () => esmDescriptor(["first", "failing"]),
        importModule: async (url) => ({
          default: {
            routes: url.includes("/first/")
              ? [firstRoute]
              : [replacementRoute, route("FailRoute")],
          },
        }),
        loadStyle: async () => undefined,
      },
    });

    expect(
      router.getRoutes().find((item) => item.name === "SharedRoute")?.path
    ).toBe("/first");
    expect(stores.uiPlugins().get("first")?.status).toBe("registered");
    expect(stores.uiPlugins().get("failing")?.status).toBe("failed");
  });

  it("restores a replaced route through the real Vue Router", async () => {
    const router = createVueRouter({
      history: createMemoryHistory(),
      routes: [route("SharedRoute", "/first")],
    });

    await setupUiPluginRuntime({
      app: createApp(RootComponent),
      router,
      platform: "console",
      setupComponents: vi.fn(),
      registeredFormKitInputs: {},
      runtime: {
        fetchProviders: async () => esmDescriptor(["failing"]),
        importModule: async () => ({
          default: {
            routes: [
              route("SharedRoute", "/replacement"),
              { path: 123 } as unknown as RouteRecordRaw,
            ],
          },
        }),
        loadStyle: async () => undefined,
      },
    });

    expect(router.resolve({ name: "SharedRoute" }).path).toBe("/first");
    expect(stores.uiPlugins().get("failing")?.status).toBe("failed");
  });

  it("rejects an un-restorable nested route conflict before mutating the router", async () => {
    const parent = route("", "/parent");
    const nested = route("NestedRoute", "/nested");
    parent.children = [nested];
    const { router, addRoute } = createStatefulRouter([parent]);

    await setupUiPluginRuntime({
      app: createApp(RootComponent),
      router,
      platform: "console",
      setupComponents: vi.fn(),
      registeredFormKitInputs: {},
      runtime: {
        fetchProviders: async () => esmDescriptor(["conflict"]),
        importModule: async () => ({
          default: { routes: [route("NestedRoute", "/replacement")] },
        }),
        loadStyle: async () => undefined,
      },
    });

    expect(addRoute).not.toHaveBeenCalled();
    expect(
      router.getRoutes().find((item) => item.name === "NestedRoute")?.path
    ).toBe("/nested");
    expect(stores.uiPlugins().get("conflict")?.status).toBe("failed");
  });

  it("attributes a delayed route chunk failure after successful registration", async () => {
    const lazyError = new Error("lazy chunk failed");
    const lazyRoute = route("LazyRoute");
    lazyRoute.component = () => Promise.reject(lazyError);
    const module: PluginModule = { routes: [lazyRoute] };
    const { router } = createRouter();

    await setupUiPluginRuntime({
      app: createApp(RootComponent),
      router,
      platform: "console",
      setupComponents: vi.fn(),
      registeredFormKitInputs: {},
      runtime: {
        fetchProviders: async () => esmDescriptor(["lazy"]),
        importModule: async () => ({ default: module }),
        loadStyle: async () => undefined,
      },
    });

    expect(stores.uiPlugins().get("lazy")?.status).toBe("registered");
    await expect(
      (lazyRoute.component as () => Promise<unknown>)()
    ).rejects.toThrow("lazy chunk failed");
    expect(stores.uiPlugins().get("lazy")?.status).toBe("failed");
    expect(usePluginModuleStore().diagnostics).toEqual([
      expect.objectContaining({ name: "lazy", stage: "chunk" }),
    ]);
    expect(mocks.toastError).not.toHaveBeenCalled();
  });

  it("attributes an asynchronous global component failure", async () => {
    const providerComponent = {};
    const module: PluginModule = {
      components: { ProviderComponent: providerComponent },
    };
    const app = createApp(RootComponent);
    const { router } = createRouter();

    await setupUiPluginRuntime({
      app,
      router,
      platform: "uc",
      setupComponents: vi.fn(),
      registeredFormKitInputs: {},
      runtime: {
        fetchProviders: async () => esmDescriptor(["component-provider"]),
        importModule: async () => ({ default: module }),
        loadStyle: async () => undefined,
      },
    });

    const instance = {
      $: { type: providerComponent },
    } as Parameters<NonNullable<typeof app.config.errorHandler>>[1];
    app.config.errorHandler?.(
      new Error("async component failed"),
      instance,
      "async component loader"
    );

    expect(stores.uiPlugins().get("component-provider")?.status).toBe("failed");
    expect(usePluginModuleStore().diagnostics).toEqual([
      expect.objectContaining({
        name: "component-provider",
        stage: "chunk",
      }),
    ]);
  });

  it("rejects an ESM entry without a default PluginModule export", async () => {
    const { router, addRoute } = createRouter();

    const modules = await setupUiPluginRuntime({
      app: createApp(RootComponent),
      router,
      platform: "console",
      setupComponents: vi.fn(),
      registeredFormKitInputs: {},
      runtime: {
        fetchProviders: async () => esmDescriptor(["invalid-export"]),
        importModule: async () => ({ named: {} }),
        loadStyle: async () => undefined,
      },
    });

    expect(modules).toEqual([]);
    expect(addRoute).not.toHaveBeenCalled();
    expect(stores.uiPlugins().get("invalid-export")?.status).toBe("failed");
    expect(usePluginModuleStore().diagnostics).toEqual([
      expect.objectContaining({
        name: "invalid-export",
        stage: "export",
      }),
    ]);
  });

  it("keeps legacy providers without a UI module as compatible no-ops", async () => {
    const { router } = createRouter();
    const descriptor: UiPluginProviderDescriptor = {
      version: "legacy",
      styles: [],
      legacy: {
        script:
          "/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.js?v=legacy",
      },
      registrations: [registration("backend-only")],
      providers: [],
      invalid: [],
    };

    await setupUiPluginRuntime({
      app: createApp(RootComponent),
      router,
      platform: "console",
      setupComponents: vi.fn(),
      registeredFormKitInputs: {},
      runtime: {
        fetchProviders: async () => descriptor,
        loadScript: async () => undefined,
        loadStyle: async () => undefined,
      },
    });

    expect(stores.uiPlugins().get("backend-only")?.status).toBe("registered");
    expect(usePluginModuleStore().diagnostics).toEqual([]);
    expect(mocks.toastError).not.toHaveBeenCalled();
  });

  it("contains descriptor discovery failure and still initializes core components", async () => {
    const setupComponents = vi.fn();
    const { router } = createRouter();

    await expect(
      setupUiPluginRuntime({
        app: createApp(RootComponent),
        router,
        platform: "uc",
        setupComponents,
        registeredFormKitInputs: {},
        runtime: {
          fetchProviders: async () => {
            throw new Error("descriptor failed");
          },
        },
      })
    ).resolves.toEqual([]);

    expect(setupComponents).toHaveBeenCalledWith();
    expect(mocks.toastError).toHaveBeenCalledTimes(1);
  });
});

function mixedDescriptor(): UiPluginProviderDescriptor {
  return {
    version: "g1",
    styles: [
      providerStyle("legacy-plugin", "g1"),
      providerStyle("esm-b", "g1"),
    ],
    legacy: {
      script: "/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.js?v=g1",
    },
    registrations: [
      registration("legacy-plugin"),
      registration("esm-b"),
      registration("invalid"),
      registration("esm-a"),
    ],
    providers: [esmProvider("esm-a", "g1"), esmProvider("esm-b", "g1")],
    invalid: [
      {
        ...registration("invalid"),
        reason: "manifest invalid",
      },
    ],
  };
}

function esmDescriptor(names: string[]): UiPluginProviderDescriptor {
  return {
    version: "version",
    styles: names.map((name) => providerStyle(name)),
    legacy: {
      script:
        "/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.js?v=version",
    },
    registrations: names.map(registration),
    providers: names.map((name) => esmProvider(name)),
    invalid: [],
  };
}

function registration(name: string) {
  return {
    name,
    type: "plugin" as const,
    version: "1.0.0",
  };
}

function esmProvider(name: string, version = "version") {
  return {
    ...registration(name),
    entry: `/plugins/${name}/assets/ui/main.js?v=${version}`,
  };
}

function providerStyle(name: string, version = "version") {
  return {
    ...registration(name),
    href: `/plugins/${name}/assets/ui/style.css?v=${version}`,
  };
}

function pluginModuleWithRoute(name: string): PluginModule {
  return { routes: [route(name)] };
}

function route(name: string, path = `/${name}`): RouteRecordRaw {
  return {
    path,
    ...(name ? { name } : {}),
    component: {},
  } as RouteRecordRaw;
}

function createRouter(
  addRouteImplementation: (route: RouteRecordRaw) => () => void = () => vi.fn()
) {
  const addRoute = vi.fn(addRouteImplementation);
  const router = {
    addRoute,
    removeRoute: vi.fn(),
    getRoutes: vi.fn(() => []),
    onError: vi.fn(() => vi.fn()),
  } as unknown as Router;
  return { router, addRoute };
}

function createStatefulRouter(
  initialRoutes: RouteRecordRaw[] = [],
  failingRouteName?: string
) {
  let routes = flattenRoutes(initialRoutes);
  const addRoute = vi.fn(
    (
      parentOrRoute: string | symbol | RouteRecordRaw,
      child?: RouteRecordRaw
    ) => {
      const route = child || (parentOrRoute as RouteRecordRaw);
      if (route.name === failingRouteName) {
        throw new Error("route commit failed");
      }
      if (route.name) {
        routes = routes.filter((item) => item.name !== route.name);
      }
      routes.push(route);
      if (child) {
        const parent = routes.find((item) => item.name === parentOrRoute);
        if (parent) {
          parent.children = [...(parent.children || []), child];
        }
      }
      return () => {
        routes = routes.filter((item) => item !== route);
      };
    }
  );
  const router = {
    addRoute,
    removeRoute: vi.fn((name: string | symbol) => {
      routes = routes.filter((item) => item.name !== name);
    }),
    getRoutes: vi.fn(() => routes),
    onError: vi.fn(() => vi.fn()),
  } as unknown as Router;
  return { router, addRoute };
}

function flattenRoutes(routes: RouteRecordRaw[]): RouteRecordRaw[] {
  return routes.flatMap((route) => [
    route,
    ...flattenRoutes(route.children || []),
  ]);
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((promiseResolve, promiseReject) => {
    resolve = promiseResolve;
    reject = promiseReject;
  });
  return { promise, resolve, reject };
}
