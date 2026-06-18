import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import type { ConfigParams, RsbuildConfig } from "@rsbuild/core";
import type { ConfigEnv, UserConfig } from "vite-plus";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { rsbuildConfig } from "../rsbuild";
import { viteConfig } from "../vite";

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

describe("provider defaults", () => {
  it("keeps plugin provider defaults when provider is omitted", () => {
    const uiDir = setupPluginProject();
    process.chdir(uiDir);

    const vite = resolveViteConfig(viteConfig({ vite: {} }), "development");
    expect(vite.base).toBeUndefined();
    expect(vite.build?.outDir).toBe("../build/resources/main/ui");
    expect(vite.build?.lib).toMatchObject({
      entry: "src/index.ts",
      name: "fake-plugin",
      formats: ["iife"],
      cssFileName: "style",
    });

    const rsbuild = resolveRsbuildConfig(
      rsbuildConfig({ rsbuild: {} }),
      "development"
    );
    expect(rsbuild.output?.distPath?.root).toBe("../build/resources/main/ui");
    expect(rsbuild.tools?.rspack?.output?.publicPath).toBe(
      "/plugins/fake-plugin/assets/ui/"
    );
    expect(rsbuild.tools?.rspack?.output?.library).toMatchObject({
      type: "window",
      export: "default",
      name: "fake-plugin",
    });

    const productionVite = resolveViteConfig(viteConfig({ vite: {} }));
    expect(productionVite.build?.outDir).toBe("./build/dist");

    const productionRsbuild = resolveRsbuildConfig(
      rsbuildConfig({ rsbuild: {} })
    );
    expect(productionRsbuild.output?.distPath?.root).toBe("./build/dist");
  });

  it("keeps plugin provider defaults when provider is explicit", () => {
    const uiDir = setupPluginProject();
    process.chdir(uiDir);

    const vite = resolveViteConfig(
      viteConfig({ provider: "plugin", vite: {} }),
      "development"
    );
    expect(vite.build?.outDir).toBe("../build/resources/main/ui");

    const rsbuild = resolveRsbuildConfig(
      rsbuildConfig({ provider: "plugin", rsbuild: {} }),
      "development"
    );
    expect(rsbuild.tools?.rspack?.output?.publicPath).toBe(
      "/plugins/fake-plugin/assets/ui/"
    );
  });

  it("uses custom manifest paths for plugin and theme providers", () => {
    const projectRoot = createTempDir();
    const pluginManifestPath = path.join(projectRoot, "custom-plugin.yaml");
    const themeManifestPath = path.join(projectRoot, "custom-theme.yaml");
    fs.writeFileSync(
      pluginManifestPath,
      [
        "metadata:",
        "  name: custom-plugin",
        "spec:",
        "  requires: '>=2.25.0'",
        "",
      ].join("\n")
    );
    fs.writeFileSync(
      themeManifestPath,
      ["metadata:", "  name: custom-theme", ""].join("\n")
    );

    const pluginConfig = resolveViteConfig(
      viteConfig({
        manifestPath: pluginManifestPath,
        vite: {},
      })
    );
    expect(pluginConfig.build?.lib).toMatchObject({
      name: "custom-plugin",
    });

    const themeConfig = resolveRsbuildConfig(
      rsbuildConfig({
        provider: "theme",
        manifestPath: themeManifestPath,
        rsbuild: {},
      })
    );
    expect(themeConfig.tools?.rspack?.output?.publicPath).toBe(
      "/themes/custom-theme/ui-plugin/assets/"
    );
    expect(themeConfig.tools?.rspack?.output?.library).toMatchObject({
      name: "theme:custom-theme",
    });
  });

  it("generates Vite theme provider defaults", () => {
    const uiPluginDir = setupThemeProject();
    process.chdir(uiPluginDir);

    const config = resolveViteConfig(
      viteConfig({ provider: "theme", vite: {} })
    );

    expect(config.base).toBe("/themes/earth/ui-plugin/assets/");
    expect(config.build?.outDir).toBe("dist");
    expect(config.build?.lib).toMatchObject({
      entry: "src/index.ts",
      name: "theme:earth",
      formats: ["iife"],
      cssFileName: "style",
    });
    expect(config.build?.rollupOptions?.external).toContain("vue");
    expect(config.build?.rollupOptions?.output).toMatchObject({
      globals: expect.objectContaining({
        vue: "Vue",
      }),
      extend: true,
    });
  });

  it("generates Rsbuild theme provider defaults", () => {
    const uiPluginDir = setupThemeProject();
    process.chdir(uiPluginDir);

    const config = resolveRsbuildConfig(
      rsbuildConfig({ provider: "theme", rsbuild: {} })
    );

    expect(config.output?.distPath?.root).toBe("dist");
    expect(config.output?.filename?.js).toBeDefined();
    expect(config.output?.filename?.css).toBeDefined();
    expect(config.output?.externals).toMatchObject({
      vue: "Vue",
    });
    expect(config.tools?.rspack?.output?.publicPath).toBe(
      "/themes/earth/ui-plugin/assets/"
    );
    expect(config.tools?.rspack?.output?.library).toMatchObject({
      type: "window",
      export: "default",
      name: "theme:earth",
    });
  });

  it("merges user config after provider defaults", () => {
    const uiPluginDir = setupThemeProject();
    process.chdir(uiPluginDir);

    const vite = resolveViteConfig(
      viteConfig({
        provider: "theme",
        vite: {
          base: "/custom/",
          build: {
            outDir: "custom-dist",
          },
        },
      })
    );
    expect(vite.base).toBe("/custom/");
    expect(vite.build?.outDir).toBe("custom-dist");

    const rsbuild = resolveRsbuildConfig(
      rsbuildConfig({
        provider: "theme",
        rsbuild: {
          output: {
            distPath: {
              root: "custom-dist",
            },
          },
          tools: {
            rspack: {
              output: {
                publicPath: "/custom/",
              },
            },
          },
        },
      })
    );
    expect(rsbuild.output?.distPath?.root).toBe("custom-dist");
    expect(rsbuild.tools?.rspack?.output?.publicPath).toBe("/custom/");
  });

  it("merges function user config after provider defaults", () => {
    const uiPluginDir = setupThemeProject();
    process.chdir(uiPluginDir);

    const vite = resolveViteConfig(
      viteConfig({
        provider: "theme",
        vite: ({ mode }) => ({
          define: {
            __MODE__: JSON.stringify(mode),
          },
        }),
      })
    );
    expect(vite.define).toMatchObject({
      "process.env.NODE_ENV": "'production'",
      __MODE__: '"production"',
    });

    const rsbuild = resolveRsbuildConfig(
      rsbuildConfig({
        provider: "theme",
        rsbuild: ({ envMode }) => ({
          output: {
            filename: {
              js: `custom-${envMode}.js`,
            },
          },
        }),
      })
    );
    expect(rsbuild.output?.filename?.js).toBe("custom-production.js");
  });
});

function setupPluginProject() {
  const projectRoot = createTempDir();
  const uiDir = path.join(projectRoot, "ui");
  fs.mkdirSync(path.join(projectRoot, "src/main/resources"), {
    recursive: true,
  });
  fs.mkdirSync(uiDir, { recursive: true });
  fs.writeFileSync(
    path.join(projectRoot, "src/main/resources/plugin.yaml"),
    [
      "metadata:",
      "  name: fake-plugin",
      "spec:",
      "  requires: '>=2.25.0'",
      "",
    ].join("\n")
  );
  return uiDir;
}

function setupThemeProject() {
  const projectRoot = createTempDir();
  const uiPluginDir = path.join(projectRoot, "ui-plugin");
  fs.mkdirSync(uiPluginDir, { recursive: true });
  fs.writeFileSync(
    path.join(projectRoot, "theme.yaml"),
    ["metadata:", "  name: earth", ""].join("\n")
  );
  return uiPluginDir;
}

function createTempDir() {
  const tempDir = fs.mkdtempSync(
    path.join(os.tmpdir(), "halo-ui-plugin-bundler-kit-")
  );
  tempDirs.push(tempDir);
  return tempDir;
}

function resolveViteConfig(config: unknown, mode = "production") {
  return (config as (env: ConfigEnv) => UserConfig)({
    command: "build",
    mode,
    isSsrBuild: false,
    isPreview: false,
  });
}

function resolveRsbuildConfig(config: unknown, envMode = "production") {
  return (config as (env: ConfigParams) => RsbuildConfig)({
    envMode,
  } as ConfigParams);
}
