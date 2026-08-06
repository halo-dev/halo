import type { PluginModule } from "@halo-dev/ui-shared";
import { stores } from "@halo-dev/ui-shared";
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vite-plus/test";
import { createApp } from "vue";
import type { Router, RouteRecordRaw } from "vue-router";
import { usePluginModuleStore } from "@/stores/plugin";
import {
  setupUiPluginRuntime,
  type UiPluginProviderDescriptor,
} from "./setupModules";

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
      "/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.css?v=g1",
      "/plugins/esm-a/assets/ui/esm-a.css?v=g1",
      "/plugins/esm-b/assets/ui/esm-b.css?v=g1",
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

  it("preserves stylesheet insertion order and isolates a provider style failure", async () => {
    const styleA = deferred<unknown>();
    const styleB = deferred<unknown>();
    const loadOrder: string[] = [];
    const loadStyle = vi.fn((url: string) => {
      loadOrder.push(url);
      return url.includes("/a.css") ? styleA.promise : styleB.promise;
    });
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
    styleB.resolve(undefined);
    styleA.reject(new Error("a style failed"));
    const modules = await setup;

    expect(loadOrder).toEqual([
      "/plugins/a/assets/ui/a.css?v=version",
      "/plugins/b/assets/ui/b.css?v=version",
    ]);
    expect(modules.map((module) => module.name)).toEqual(["b"]);
    expect(stores.uiPlugins().get("a")?.status).toBe("failed");
    expect(stores.uiPlugins().get("b")?.status).toBe("registered");
    expect(usePluginModuleStore().diagnostics).toEqual([
      expect.objectContaining({ name: "a", stage: "style" }),
    ]);
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
      legacy: {
        script:
          "/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.js?v=legacy",
        style:
          "/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.css?v=legacy",
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
    legacy: {
      script: "/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.js?v=g1",
      style: "/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.css?v=g1",
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
    legacy: {
      script:
        "/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.js?v=version",
      style:
        "/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.css?v=version",
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
    styles: [`/plugins/${name}/assets/ui/${name}.css?v=${version}`],
  };
}

function pluginModuleWithRoute(name: string): PluginModule {
  return { routes: [route(name)] };
}

function route(name: string): RouteRecordRaw {
  return {
    path: `/${name}`,
    name,
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

function deferred<T>() {
  let resolve!: (value: T) => void;
  let reject!: (reason?: unknown) => void;
  const promise = new Promise<T>((promiseResolve, promiseReject) => {
    resolve = promiseResolve;
    reject = promiseReject;
  });
  return { promise, resolve, reject };
}
