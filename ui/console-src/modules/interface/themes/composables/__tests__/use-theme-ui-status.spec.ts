import type { Theme, UiPluginResources } from "@halo-dev/api-client";
import { stores, type UiPluginsHostStore } from "@halo-dev/ui-shared";
import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it } from "vite-plus/test";
import { ref, type Ref } from "vue";
import { usePluginModuleStore } from "@/stores/plugin";
import { useThemeUiStatus } from "../use-theme-ui-status";

const theme = {
  metadata: { name: "theme-foo" },
  spec: { version: "1.0.0" },
} as Theme;

function createUi(kind: string): Ref<UiPluginResources> {
  return ref({ kind } as UiPluginResources);
}

describe("useThemeUiStatus", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  function seedRegistration(version = "1.0.0") {
    const uiPluginsStore = stores.uiPlugins() as unknown as UiPluginsHostStore;
    uiPluginsStore._seed([
      { name: "theme:theme-foo", type: "theme", version, status: "pending" },
    ]);
    return uiPluginsStore;
  }

  it("reports none and invalid kinds without touching stores", () => {
    expect(useThemeUiStatus(ref(theme), createUi("none")).loadState.value).toBe(
      "none"
    );
    expect(
      useThemeUiStatus(ref(theme), createUi("invalid")).loadState.value
    ).toBe("invalid");
  });

  it("reports not-loaded when the theme has resources but no registration", () => {
    const { loadState, moduleSummary } = useThemeUiStatus(
      ref(theme),
      createUi("esm")
    );
    expect(loadState.value).toBe("not-loaded");
    expect(moduleSummary.value).toBeUndefined();
  });

  it("exposes module declarations only for the registered matching version", () => {
    const uiPluginsStore = seedRegistration();
    const pluginModuleStore = usePluginModuleStore();
    pluginModuleStore.registerPluginModule("theme:theme-foo", {
      routes: [{ path: "/foo", name: "Foo" }],
    });

    const { loadState, versionMismatch, moduleSummary } = useThemeUiStatus(
      ref(theme),
      createUi("esm")
    );
    expect(loadState.value).toBe("pending");
    expect(moduleSummary.value).toBeUndefined();

    uiPluginsStore._setStatus("theme:theme-foo", "registered");
    expect(loadState.value).toBe("registered");
    expect(versionMismatch.value).toBe(false);
    expect(moduleSummary.value?.consoleRoutes).toHaveLength(1);
  });

  it("hides module declarations when the loaded version differs", () => {
    const uiPluginsStore = seedRegistration("0.9.0");
    uiPluginsStore._setStatus("theme:theme-foo", "registered");
    usePluginModuleStore().registerPluginModule("theme:theme-foo", {
      components: { FooCard: {} },
    });

    const { loadState, versionMismatch, moduleSummary } = useThemeUiStatus(
      ref(theme),
      createUi("esm")
    );
    expect(loadState.value).toBe("registered");
    expect(versionMismatch.value).toBe(true);
    expect(moduleSummary.value).toBeUndefined();
  });

  it("surfaces load diagnostics for the theme", () => {
    const uiPluginsStore = seedRegistration();
    uiPluginsStore._setStatus("theme:theme-foo", "failed");
    const pluginModuleStore = usePluginModuleStore();
    pluginModuleStore.recordDiagnostic({
      name: "theme:theme-foo",
      type: "theme",
      stage: "entry",
      message: "boom",
    });
    pluginModuleStore.recordDiagnostic({
      name: "plugin-other",
      type: "plugin",
      stage: "entry",
      message: "unrelated",
    });

    const { loadState, diagnostics } = useThemeUiStatus(
      ref(theme),
      createUi("legacy")
    );
    expect(loadState.value).toBe("failed");
    expect(diagnostics.value).toHaveLength(1);
    expect(diagnostics.value[0].message).toBe("boom");
  });
});
