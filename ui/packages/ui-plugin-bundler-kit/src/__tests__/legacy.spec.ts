import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import type { Plugin } from "vite-plus";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { HaloUIPluginBundlerKit } from "../legacy";

const originalCwd = process.cwd();
const tempDirs: string[] = [];

afterEach(() => {
  process.chdir(originalCwd);
  while (tempDirs.length > 0) {
    const tempDir = tempDirs.pop();
    if (tempDir) {
      fs.rmSync(tempDir, { recursive: true, force: true });
    }
  }
});

describe("HaloUIPluginBundlerKit", () => {
  it("keeps legacy default output directories", () => {
    const uiDir = setupPluginProject("legacy-plugin");
    process.chdir(uiDir);

    const plugin = HaloUIPluginBundlerKit();

    expect(resolveLegacyConfig(plugin, "development").build).toMatchObject({
      outDir: "../build/resources/main/console",
      emptyOutDir: true,
      lib: {
        entry: "src/index.ts",
        name: "legacy-plugin",
        formats: ["iife"],
      },
    });
    expect(resolveLegacyConfig(plugin, "production").build).toMatchObject({
      outDir: "../src/main/resources/console",
    });
  });

  it("applies string outDir override for every mode", () => {
    const uiDir = setupPluginProject("legacy-plugin");
    process.chdir(uiDir);

    const plugin = HaloUIPluginBundlerKit({ outDir: "custom" });

    expect(resolveLegacyConfig(plugin, "development").build).toMatchObject({
      outDir: "custom",
    });
    expect(resolveLegacyConfig(plugin, "production").build).toMatchObject({
      outDir: "custom",
    });
  });

  it("applies mode-specific outDir overrides", () => {
    const uiDir = setupPluginProject("legacy-plugin");
    process.chdir(uiDir);

    const plugin = HaloUIPluginBundlerKit({
      outDir: {
        dev: "dev-dist",
        prod: "prod-dist",
      },
    });

    expect(resolveLegacyConfig(plugin, "development").build).toMatchObject({
      outDir: "dev-dist",
    });
    expect(resolveLegacyConfig(plugin, "production").build).toMatchObject({
      outDir: "prod-dist",
    });
  });

  it("uses custom manifest path", () => {
    const projectRoot = createTempDir();
    const manifestPath = path.join(projectRoot, "plugin.yaml");
    fs.writeFileSync(
      manifestPath,
      ["metadata:", "  name: custom-legacy-plugin", "spec:", ""].join("\n")
    );

    const plugin = HaloUIPluginBundlerKit({ manifestPath });

    expect(resolveLegacyConfig(plugin, "production").build).toMatchObject({
      lib: {
        name: "custom-legacy-plugin",
      },
    });
  });
});

function resolveLegacyConfig(plugin: Plugin, mode: string) {
  if (typeof plugin.config !== "function") {
    throw new Error("Expected plugin config hook");
  }
  return plugin.config(
    {},
    {
      command: "build",
      mode,
      isSsrBuild: false,
      isPreview: false,
    }
  );
}

function setupPluginProject(name: string) {
  const projectRoot = createTempDir();
  const uiDir = path.join(projectRoot, "ui");
  fs.mkdirSync(path.join(projectRoot, "src/main/resources"), {
    recursive: true,
  });
  fs.mkdirSync(uiDir, { recursive: true });
  fs.writeFileSync(
    path.join(projectRoot, "src/main/resources/plugin.yaml"),
    ["metadata:", `  name: ${name}`, "spec:", ""].join("\n")
  );
  return uiDir;
}

function createTempDir() {
  const tempDir = fs.mkdtempSync(
    path.join(os.tmpdir(), "halo-ui-plugin-legacy-")
  );
  tempDirs.push(tempDir);
  return tempDir;
}
