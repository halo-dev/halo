import { createPinia, setActivePinia } from "pinia";
import { beforeEach, describe, expect, it } from "vite-plus/test";
import { stores, type UiPluginsHostStore } from ".";

describe("stores.uiPlugins", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });

  it("reactively exposes enabled and registration state", () => {
    const store = stores.uiPlugins() as UiPluginsHostStore;
    store._seed([
      {
        name: "plugin-search",
        type: "plugin",
        version: "1.2.3",
        status: "pending",
      },
      {
        name: "theme-earth",
        type: "theme",
        version: "2.0.0",
        status: "failed",
      },
    ]);

    expect(store.isEnabled("plugin-search")).toBe(true);
    expect(store.isRegistered("plugin-search")).toBe(false);
    expect(store.get("theme-earth")).toMatchObject({ status: "failed" });

    store._setStatus("plugin-search", "registered");

    expect(store.isRegistered("plugin-search")).toBe(true);
    expect(store.registrations).toHaveLength(2);
  });

  it("replaces stale snapshot metadata when seeded again", () => {
    const store = stores.uiPlugins() as UiPluginsHostStore;
    store._seed([
      {
        name: "old-plugin",
        type: "plugin",
        version: "1.0.0",
        status: "pending",
      },
    ]);

    store._seed([]);

    expect(store.isEnabled("old-plugin")).toBe(false);
  });
});
