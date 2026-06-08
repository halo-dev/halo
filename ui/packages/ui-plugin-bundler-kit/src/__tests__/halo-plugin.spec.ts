import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { afterEach, describe, expect, it, vi } from "vitest";
import {
  getHaloPluginBundleLocation,
  getHaloPluginManifest,
  getHaloThemeAssetPublicPath,
  getHaloThemeManifest,
  getHaloThemeModuleName,
  getManifestName,
} from "../utils/halo-plugin";

const tempDirs: string[] = [];

afterEach(() => {
  vi.restoreAllMocks();
  while (tempDirs.length > 0) {
    const tempDir = tempDirs.pop();
    if (tempDir) {
      fs.rmSync(tempDir, { recursive: true, force: true });
    }
  }
});

describe("halo manifest utilities", () => {
  it("reads plugin manifests", () => {
    const manifestPath = writeManifest([
      "metadata:",
      "  name: plugin-a",
      "spec:",
      "  requires: '>=2.25.0'",
      "",
    ]);

    const manifest = getHaloPluginManifest(manifestPath);

    expect(getManifestName(manifest)).toBe("plugin-a");
    expect(manifest.spec.requires).toBe(">=2.25.0");
  });

  it("reads theme manifests and derives theme bundle values", () => {
    const manifestPath = writeManifest(["metadata:", "  name: theme-a", ""]);

    const manifest = getHaloThemeManifest(manifestPath);

    expect(getManifestName(manifest)).toBe("theme-a");
    expect(getHaloThemeModuleName(manifest)).toBe("theme:theme-a");
    expect(getHaloThemeAssetPublicPath(manifest)).toBe(
      "/themes/theme-a/ui-plugin/assets/"
    );
  });

  it("selects ui bundle location for plugins requiring Halo 2.25 or newer", () => {
    expect(
      getHaloPluginBundleLocation({
        metadata: { name: "plugin-a" },
        spec: { requires: ">=2.25.0" },
      } as never)
    ).toBe("ui");
  });

  it("falls back to console bundle location for older or missing requirements", () => {
    expect(
      getHaloPluginBundleLocation({
        metadata: { name: "plugin-a" },
        spec: { requires: ">=2.24.0" },
      } as never)
    ).toBe("console");
    expect(
      getHaloPluginBundleLocation({
        metadata: { name: "plugin-a" },
        spec: {},
      } as never)
    ).toBe("console");
  });

  it("warns and falls back to console bundle location for invalid requirements", () => {
    const warn = vi.spyOn(console, "warn").mockImplementation(() => undefined);

    expect(
      getHaloPluginBundleLocation({
        metadata: { name: "plugin-a" },
        spec: { requires: "not semver" },
      } as never)
    ).toBe("console");
    expect(warn).toHaveBeenCalledWith(
      '[ui-plugin-bundler-kit] Invalid semver range in plugin manifest "spec.requires": "not semver". ' +
        'Falling back to "console" bundle location.'
    );
  });
});

function writeManifest(lines: string[]) {
  const tempDir = fs.mkdtempSync(
    path.join(os.tmpdir(), "halo-ui-plugin-manifest-")
  );
  tempDirs.push(tempDir);
  const manifestPath = path.join(tempDir, "manifest.yaml");
  fs.writeFileSync(manifestPath, lines.join("\n"));
  return manifestPath;
}
