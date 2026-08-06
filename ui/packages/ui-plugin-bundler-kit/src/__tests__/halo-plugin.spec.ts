import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { afterEach, describe, expect, it, vi } from "vite-plus/test";
import {
  getHaloPluginBundleLocation,
  getHaloPluginManifest,
  getHaloThemeAssetPublicPath,
  getHaloThemeManifest,
  getHaloThemeModuleName,
  getManifestName,
  selectProviderFormat,
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

  it.each(["2.26.0", ">=2.26.0", ">=3.2.1"])(
    "automatically selects ESM for simple target %s",
    (requires) => {
      expect(selectProviderFormat({ requires })).toMatchObject({
        format: "esm",
        reason: "automatic",
      });
    }
  );

  it.each(["2.25.9", ">=2.25.0"])(
    "automatically selects IIFE for older simple target %s",
    (requires) => {
      expect(selectProviderFormat({ requires })).toMatchObject({
        format: "iife",
        reason: "automatic",
      });
    }
  );

  it.each([undefined, "*", ">=2.26.0 <3", ">=2.26.0 & <3", "latest"])(
    "falls back to IIFE for unsupported automatic target %s",
    (requires) => {
      expect(selectProviderFormat({ requires })).toMatchObject({
        format: "iife",
        reason: "automatic-fallback",
        warnings: [expect.stringContaining("using IIFE output")],
      });
    }
  );

  it("keeps explicit format semantics separate from automatic parsing", () => {
    expect(
      selectProviderFormat({ format: "iife", requires: ">=3.0.0" })
    ).toEqual({ format: "iife", reason: "explicit", warnings: [] });
    expect(
      selectProviderFormat({
        format: "esm",
        requires: "*",
        targetHaloVersion: "2.26.0-beta.1",
      })
    ).toMatchObject({
      format: "esm",
      targetHaloVersion: "2.26.0-beta.1",
    });
    expect(() =>
      selectProviderFormat({ format: "esm", requires: "*" })
    ).toThrow("targetHaloVersion");
  });

  it("warns but preserves explicit ESM for an older metadata target", () => {
    expect(
      selectProviderFormat({ format: "esm", requires: ">=2.25.0" })
    ).toMatchObject({
      format: "esm",
      warnings: [expect.stringContaining("predates ESM UI provider support")],
    });
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
