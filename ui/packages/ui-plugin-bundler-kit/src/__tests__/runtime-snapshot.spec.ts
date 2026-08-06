import path from "node:path";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vite-plus/test";
import {
  HALO_HOST_RUNTIME_SNAPSHOTS,
  SHARED_PACKAGE_ROOTS,
  resolveSharedPackage,
  selectHaloHostRuntimeSnapshot,
  validateHaloHostRuntimeSnapshot,
} from "../runtime-snapshot";

const uiRoot = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  "../../../.."
);

describe("Halo host runtime snapshot", () => {
  it("contains exactly the ten supported roots", () => {
    const snapshot = HALO_HOST_RUNTIME_SNAPSHOTS[0];

    expect(Object.keys(snapshot.packages)).toEqual(SHARED_PACKAGE_ROOTS);
    expect(Object.keys(snapshot.packages)).not.toContain("@vueuse/core");
    expect(Object.keys(snapshot.packages)).not.toContain("@formkit/inputs");
    expect(Object.keys(snapshot.packages)).not.toContain("@tiptap/core");
    expect(Object.keys(snapshot.packages)).not.toContain("prosemirror-state");
  });

  it("records actual host versions and runtime exports", async () => {
    const snapshot = HALO_HOST_RUNTIME_SNAPSHOTS[0];

    await Promise.all(
      SHARED_PACKAGE_ROOTS.map(async (root) => {
        const resolved = await resolveSharedPackage(root, uiRoot);
        const runtimeModule = await import(root);

        expect(snapshot.packages[root].version, root).toBe(resolved.version);
        expect(snapshot.packages[root].exports, root).toEqual(
          Object.keys(runtimeModule)
            .filter((name) => /^[$A-Z_a-z][$\w]*$/.test(name))
            .sort()
        );
      })
    );
  }, 15_000);

  it("records host facts without accepted provider ranges", () => {
    const snapshot = HALO_HOST_RUNTIME_SNAPSHOTS[0];

    for (const entry of Object.values(snapshot.packages)) {
      expect(entry).not.toHaveProperty("range");
    }
  });

  it("reuses the latest eligible sparse snapshot", () => {
    const selected = selectHaloHostRuntimeSnapshot("3.2.9");

    expect(selected.snapshot.haloVersion).toBe("2.26.0");
    expect(selected.reusedOlderSnapshot).toBe(true);
  });

  it("fails with a diagnostic when no snapshot is eligible", () => {
    expect(() => selectHaloHostRuntimeSnapshot("2.25.9")).toThrow(
      "No ESM host runtime snapshot is available for Halo 2.25.9"
    );
  });

  it("rejects missing and extra shared roots", () => {
    const snapshot = structuredClone(HALO_HOST_RUNTIME_SNAPSHOTS[0]);
    delete (snapshot.packages as Partial<typeof snapshot.packages>).vue;
    (snapshot.packages as Record<string, unknown>)["@vueuse/core"] = {};

    expect(() => validateHaloHostRuntimeSnapshot(snapshot)).toThrow(
      "Host runtime snapshot must expose exactly"
    );
  });
});
