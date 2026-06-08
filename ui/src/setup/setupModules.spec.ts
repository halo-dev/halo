import type { PluginModule } from "@halo-dev/ui-shared";
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { App } from "vue";
import type { Router, RouteRecordRaw } from "vue-router";
import { usePluginModuleStore } from "@/stores/plugin";
import {
  notifyPluginLoadError,
  setupPluginStyles,
  setupUiPluginRuntime,
} from "./setupModules";

const mocks = vi.hoisted(() => ({
  loadStyle: vi.fn(),
  toastError: vi.fn(),
  useScriptTag: vi.fn(),
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

vi.mock("@/utils/load-style", () => ({
  loadStyle: mocks.loadStyle,
}));

vi.mock("@vueuse/core", () => ({
  useScriptTag: mocks.useScriptTag,
}));

describe("setupPluginModules", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
    delete (window as unknown as Record<string, unknown>).enabledUiPlugins;
    delete (window as unknown as Record<string, unknown>)["plugin-one"];
    delete (window as unknown as Record<string, unknown>)["theme:earth"];
  });

  it("registers plugin and active theme modules", async () => {
    const pluginRoute = {
      path: "/plugin",
      name: "PluginRoute",
      component: {},
    } as RouteRecordRaw;
    const themeRoute = {
      path: "/theme",
      name: "ThemeRoute",
      component: {},
    } as RouteRecordRaw;
    const pluginInput = {
      type: "input",
      schema: [],
    };
    const themeInput = {
      type: "input",
      schema: [],
    };
    const pluginModule: PluginModule = {
      formkit: {
        inputs: {
          "plugin-input": pluginInput,
        },
      },
      routes: [pluginRoute],
    } as PluginModule;
    const themeModule: PluginModule = {
      formkit: {
        inputs: {
          "theme-input": themeInput,
        },
      },
      routes: [themeRoute],
    } as PluginModule;
    mocks.useScriptTag.mockImplementation((src: string) => ({
      load: vi.fn(async () => {
        if (src.includes("/ui-plugins/-/bundle.js")) {
          (window as unknown as Record<string, unknown>).enabledUiPlugins = [
            { name: "plugin-one", type: "plugin", version: "1.0.0" },
            {
              name: "theme:earth",
              type: "theme",
              themeName: "earth",
              version: "1.0.0",
            },
          ];
          (window as unknown as Record<string, unknown>)["plugin-one"] =
            pluginModule;
          (window as unknown as Record<string, unknown>)["theme:earth"] =
            themeModule;
        }
      }),
    }));
    mocks.loadStyle.mockResolvedValue(undefined);
    const app = { component: vi.fn() } as unknown as App;
    const router = {
      getRoutes: vi.fn(() => []),
      addRoute: vi.fn(),
      removeRoute: vi.fn(),
    } as unknown as Router;
    const setupComponents = vi.fn();

    await setupUiPluginRuntime({
      app,
      router,
      platform: "console",
      setupComponents,
      registeredFormKitInputs: {},
    });

    const store = usePluginModuleStore();
    expect(store.pluginModuleMap["plugin-one"]).toBe(pluginModule);
    expect(store.pluginModuleMap["theme:earth"]).toBe(themeModule);
    expect(setupComponents).toHaveBeenCalledWith({
      formkitInputs: {
        "plugin-input": pluginInput,
      },
    });
    expect(router.addRoute).toHaveBeenCalledWith(pluginRoute);
    expect(router.addRoute).toHaveBeenCalledWith(themeRoute);
    expect(mocks.loadStyle).toHaveBeenCalledWith(
      expect.stringContaining("/ui-plugins/-/bundle.css")
    );
  });

  it("keeps startup errors contained when theme style loading fails", async () => {
    mocks.useScriptTag.mockImplementation(() => ({
      load: vi.fn(async () => {
        (window as unknown as Record<string, unknown>).enabledUiPlugins = [];
      }),
    }));
    mocks.loadStyle.mockRejectedValueOnce(new Error("style failed"));

    await expect(setupPluginStyles()).rejects.toThrow("style failed");
    notifyPluginLoadError(new Error("style failed"));

    expect(mocks.toastError).toHaveBeenCalledWith(
      "core.plugin.loader.toast.style_load_failed"
    );
  });
});
