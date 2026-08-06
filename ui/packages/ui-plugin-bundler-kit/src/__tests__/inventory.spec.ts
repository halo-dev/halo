import path from "node:path";
import { fileURLToPath } from "node:url";
import { satisfies } from "semver";
import { describe, expect, it } from "vite-plus/test";
import {
  HALO_SHARED_INVENTORIES,
  SHARED_PACKAGE_ROOTS,
  resolveSharedPackage,
  selectHaloSharedInventory,
  validateHaloSharedInventory,
} from "../inventory";

const uiRoot = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  "../../../.."
);

describe("Halo shared dependency inventory", () => {
  it("contains exactly the ten supported roots", () => {
    const inventory = HALO_SHARED_INVENTORIES[0];

    expect(Object.keys(inventory.packages)).toEqual(SHARED_PACKAGE_ROOTS);
    expect(Object.keys(inventory.packages)).not.toContain("@vueuse/core");
    expect(Object.keys(inventory.packages)).not.toContain("@formkit/inputs");
    expect(Object.keys(inventory.packages)).not.toContain("@tiptap/core");
    expect(Object.keys(inventory.packages)).not.toContain("prosemirror-state");
  });

  it("records actual host versions and runtime exports", async () => {
    const inventory = HALO_SHARED_INVENTORIES[0];

    await Promise.all(
      SHARED_PACKAGE_ROOTS.map(async (root) => {
        const resolved = await resolveSharedPackage(root, uiRoot);
        const runtimeModule = await import(root);

        expect(inventory.packages[root].version, root).toBe(resolved.version);
        expect(inventory.packages[root].exports, root).toEqual(
          Object.keys(runtimeModule)
            .filter((name) => /^[$A-Z_a-z][$\w]*$/.test(name))
            .sort()
        );
      })
    );
  }, 15_000);

  it("uses manually reviewed ranges without admitting prereleases", () => {
    const inventory = HALO_SHARED_INVENTORIES[0];

    for (const entry of Object.values(inventory.packages)) {
      expect(satisfies(entry.version, entry.range)).toBe(true);
    }
    expect(satisfies("3.5.0-beta.1", inventory.packages.vue.range)).toBe(false);
  });

  it("reuses the latest eligible sparse inventory", () => {
    const selected = selectHaloSharedInventory("3.2.9");

    expect(selected.inventory.haloVersion).toBe("2.26.0");
    expect(selected.reusedOlderInventory).toBe(true);
  });

  it("fails with a diagnostic when no inventory is eligible", () => {
    expect(() => selectHaloSharedInventory("2.25.9")).toThrow(
      "No ESM shared dependency inventory is available for Halo 2.25.9"
    );
  });

  it("rejects missing and extra shared roots", () => {
    const inventory = structuredClone(HALO_SHARED_INVENTORIES[0]);
    delete (inventory.packages as Partial<typeof inventory.packages>).vue;
    (inventory.packages as Record<string, unknown>)["@vueuse/core"] = {};

    expect(() => validateHaloSharedInventory(inventory)).toThrow(
      "Inventory must expose exactly"
    );
  });
});
