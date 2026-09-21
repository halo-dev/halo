import path from "node:path";
import { fileURLToPath } from "node:url";
import { describe, expect, it } from "vite-plus/test";
import packageJson from "../../package.json";
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
const currentSnapshot = HALO_HOST_RUNTIME_SNAPSHOTS.find(
  (snapshot) => snapshot.haloVersion === packageJson.version
);
if (!currentSnapshot) {
  throw new Error(`Missing Halo ${packageJson.version} host runtime snapshot.`);
}
const selectionSnapshots = ["2.26.0", "2.27.0"].map((haloVersion) => ({
  ...currentSnapshot,
  haloVersion,
}));

describe("Halo host runtime snapshot", () => {
  it("contains exactly the ten supported roots", () => {
    for (const snapshot of HALO_HOST_RUNTIME_SNAPSHOTS) {
      expect(Object.keys(snapshot.packages)).toEqual(SHARED_PACKAGE_ROOTS);
      expect(Object.keys(snapshot.packages)).not.toContain("@vueuse/core");
      expect(Object.keys(snapshot.packages)).not.toContain("@formkit/inputs");
      expect(Object.keys(snapshot.packages)).not.toContain("@tiptap/core");
      expect(Object.keys(snapshot.packages)).not.toContain("prosemirror-state");
    }
  });

  it("records actual host versions and runtime exports", async () => {
    await Promise.all(
      SHARED_PACKAGE_ROOTS.map(async (root) => {
        const resolved = await resolveSharedPackage(root, uiRoot);
        const runtimeModule = await import(root);

        expect(currentSnapshot.packages[root].version, root).toBe(
          resolved.version
        );
        expect(currentSnapshot.packages[root].exports, root).toEqual(
          Object.keys(runtimeModule)
            .filter(
              (name) => /^[$A-Z_a-z][$\w]*$/.test(name) && name !== "__esModule"
            )
            .sort()
        );
      })
    );
    expect(currentSnapshot.packages.vue.exports).not.toContain("__esModule");
  }, 15_000);

  it("records host facts without accepted provider ranges", () => {
    for (const snapshot of HALO_HOST_RUNTIME_SNAPSHOTS) {
      for (const entry of Object.values(snapshot.packages)) {
        expect(entry).not.toHaveProperty("range");
      }
    }
  });

  it("reuses the latest eligible sparse snapshot", () => {
    const selected = selectHaloHostRuntimeSnapshot("3.2.9", selectionSnapshots);

    expect(selected.snapshot.haloVersion).toBe("2.27.0");
    expect(selected.reusedOlderSnapshot).toBe(true);
  });

  it("distinguishes same-core prereleases from newer prereleases", () => {
    expect(
      selectHaloHostRuntimeSnapshot("2.27.0-beta.1", selectionSnapshots)
        .reusedOlderSnapshot
    ).toBe(false);
    expect(
      selectHaloHostRuntimeSnapshot("2.28.0-beta.1", selectionSnapshots)
        .reusedOlderSnapshot
    ).toBe(true);
  });

  it("fails with a diagnostic when no snapshot is eligible", () => {
    expect(() =>
      selectHaloHostRuntimeSnapshot("2.25.9", selectionSnapshots)
    ).toThrow("No ESM host runtime snapshot is available for Halo 2.25.9");
  });

  it("rejects missing and extra shared roots", () => {
    const snapshot = structuredClone(currentSnapshot);
    delete (snapshot.packages as Partial<typeof snapshot.packages>).vue;
    (snapshot.packages as Record<string, unknown>)["@vueuse/core"] = {};

    expect(() => validateHaloHostRuntimeSnapshot(snapshot)).toThrow(
      "Host runtime snapshot must expose exactly"
    );
  });
});
