import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";
import {
  createRsbuild,
  type ConfigParams,
  type RsbuildConfig,
} from "@rsbuild/core";
import { build as viteBuild, type ConfigEnv, type UserConfig } from "vite";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { rsbuildConfig } from "../rsbuild";
import { viteConfig } from "../vite";

const originalCwd = process.cwd();
const uiRoot = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  "../../../.."
);
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

describe("ESM provider builds", () => {
  it("builds a plugin with Vite and emits the minimal manifest", async () => {
    const providerRoot = setupProviderProject("plugin");
    process.chdir(providerRoot);
    const config = resolveViteConfig(viteConfig({ vite: {} }));

    await viteBuild({
      ...config,
      root: providerRoot,
      configFile: false,
      logLevel: "silent",
    });

    assertEsmOutput(
      path.join(providerRoot, "build/dist"),
      "/plugins/esm-plugin/assets/ui/"
    );
  });

  it("builds a theme with Rsbuild using the equivalent contract", async () => {
    const providerRoot = setupProviderProject("theme");
    process.chdir(providerRoot);
    const config = resolveRsbuildConfig(
      rsbuildConfig({ provider: "theme", rsbuild: {} })
    );
    const rsbuild = await createRsbuild({
      cwd: providerRoot,
      rsbuildConfig: config,
    });

    await rsbuild.build();

    assertEsmOutput(
      path.join(providerRoot, "dist"),
      "/themes/esm-theme/ui-plugin/assets/"
    );
  });

  it("preserves explicit IIFE output without an ESM manifest", async () => {
    const providerRoot = setupProviderProject("plugin");
    process.chdir(providerRoot);
    const config = resolveViteConfig(viteConfig({ format: "iife", vite: {} }));

    await viteBuild({
      ...config,
      root: providerRoot,
      configFile: false,
      logLevel: "silent",
    });

    const outputRoot = path.join(providerRoot, "build/dist");
    expect(fs.existsSync(path.join(outputRoot, "main.js"))).toBe(true);
    expect(fs.existsSync(path.join(outputRoot, "ui-plugin.json"))).toBe(false);
  });

  it("preserves explicit IIFE theme output without an ESM manifest", async () => {
    const providerRoot = setupProviderProject("theme");
    process.chdir(providerRoot);
    const config = resolveRsbuildConfig(
      rsbuildConfig({ provider: "theme", format: "iife", rsbuild: {} })
    );
    const rsbuild = await createRsbuild({
      cwd: providerRoot,
      rsbuildConfig: config,
    });

    await rsbuild.build();

    expect(fs.existsSync(path.join(providerRoot, "dist/main.js"))).toBe(true);
    expect(fs.existsSync(path.join(providerRoot, "dist/ui-plugin.json"))).toBe(
      false
    );
  });

  it("rejects a Vite alias that bypasses the shared contract", async () => {
    const providerRoot = setupProviderProject("plugin");
    process.chdir(providerRoot);
    const config = resolveViteConfig(
      viteConfig({
        vite: {
          resolve: {
            alias: {
              vue: path.join(providerRoot, "src/lazy.ts"),
            },
          },
        },
      })
    );

    await expect(
      viteBuild({
        ...config,
        root: providerRoot,
        configFile: false,
        logLevel: "silent",
      })
    ).rejects.toThrow("would bypass Halo snapshot validation");
  });

  it("rejects a Vite external that Halo does not provide", async () => {
    const providerRoot = setupProviderProject("plugin");
    process.chdir(providerRoot);
    const config = resolveViteConfig(
      viteConfig({
        vite: {
          build: {
            rollupOptions: {
              external: ["@vueuse/core"],
            },
          },
        },
      })
    );

    await expect(
      viteBuild({
        ...config,
        root: providerRoot,
        configFile: false,
        logLevel: "silent",
      })
    ).rejects.toThrow("cannot externalize @vueuse/core");
  });

  it("rejects a Vite override that merges async CSS into the entry", async () => {
    const providerRoot = setupProviderProject("plugin");
    process.chdir(providerRoot);
    const config = resolveViteConfig(
      viteConfig({
        vite: {
          build: { cssCodeSplit: false },
        },
      })
    );

    await expect(
      viteBuild({
        ...config,
        root: providerRoot,
        configFile: false,
        logLevel: "silent",
      })
    ).rejects.toThrow("build.cssCodeSplit to remain enabled");
  });

  it("rejects an absolute Vite base for relocatable ESM output", async () => {
    const providerRoot = setupProviderProject("plugin");
    process.chdir(providerRoot);
    const config = resolveViteConfig(
      viteConfig({
        vite: {
          base: "/plugins/esm-plugin/assets/ui/",
        },
      })
    );

    await expect(
      viteBuild({
        ...config,
        root: providerRoot,
        configFile: false,
        logLevel: "silent",
      })
    ).rejects.toThrow("relative Vite base");
  });

  it("rejects an absolute Rsbuild public path for relocatable ESM output", async () => {
    const providerRoot = setupProviderProject("theme");
    process.chdir(providerRoot);
    const config = resolveRsbuildConfig(
      rsbuildConfig({
        provider: "theme",
        rsbuild: {
          output: {
            assetPrefix: "/themes/esm-theme/ui-plugin/assets/",
          },
        },
      })
    );

    await expect(
      (async () => {
        const rsbuild = await createRsbuild({
          cwd: providerRoot,
          rsbuildConfig: config,
        });
        await rsbuild.build();
      })()
    ).rejects.toThrow("automatic Rsbuild public path");
  });

  it("rejects an Rsbuild entry filename override", () => {
    const providerRoot = setupProviderProject("theme");
    process.chdir(providerRoot);

    expect(() =>
      resolveRsbuildConfig(
        rsbuildConfig({
          provider: "theme",
          rsbuild: {
            output: { filename: { js: "custom.js" } },
          },
        })
      )
    ).toThrow("owns output.filename.js");
  });

  it("rejects conflicting Rsbuild module output settings", () => {
    const providerRoot = setupProviderProject("theme");
    process.chdir(providerRoot);

    expect(() =>
      resolveRsbuildConfig(
        rsbuildConfig({
          provider: "theme",
          rsbuild: {
            tools: {
              rspack: {
                output: { iife: true },
              },
            },
          },
        })
      )
    ).toThrow("requires Rspack output.iife to be false");
  });

  it("rejects arbitrary Rsbuild externals", () => {
    const providerRoot = setupProviderProject("theme");
    process.chdir(providerRoot);

    expect(() =>
      resolveRsbuildConfig(
        rsbuildConfig({
          provider: "theme",
          rsbuild: {
            output: { externals: { lodash: "lodash" } },
          },
        })
      )
    ).toThrow("cannot externalize dependencies that Halo does not provide");
  });
});

function setupProviderProject(provider: "plugin" | "theme") {
  const projectRoot = fs.mkdtempSync(
    path.join(os.tmpdir(), "halo-esm-provider-")
  );
  tempDirs.push(projectRoot);
  const providerRoot = path.join(
    projectRoot,
    provider === "plugin" ? "ui" : "ui-plugin"
  );
  fs.mkdirSync(path.join(providerRoot, "src"), { recursive: true });
  fs.symlinkSync(
    path.join(uiRoot, "node_modules"),
    path.join(providerRoot, "node_modules")
  );
  if (provider === "plugin") {
    fs.mkdirSync(path.join(projectRoot, "src/main/resources"), {
      recursive: true,
    });
    fs.writeFileSync(
      path.join(projectRoot, "src/main/resources/plugin.yaml"),
      [
        "metadata:",
        "  name: esm-plugin",
        "spec:",
        "  requires: '>=2.26.0'",
        "",
      ].join("\n")
    );
  } else {
    fs.writeFileSync(
      path.join(projectRoot, "theme.yaml"),
      [
        "metadata:",
        "  name: esm-theme",
        "spec:",
        "  requires: '>=2.26.0'",
        "",
      ].join("\n")
    );
  }
  fs.writeFileSync(
    path.join(providerRoot, "src/index.ts"),
    `
      import { ref } from "vue";
      import { createRouter } from "vue-router";
      import { defineStore } from "pinia";
      import axios from "axios";
      import { FormKit } from "@formkit/vue";
      import { getNode } from "@formkit/core";
      import { stores } from "@halo-dev/ui-shared";
      import { VButton } from "@halo-dev/components";
      import { axiosInstance } from "@halo-dev/api-client";
      import { Editor } from "@halo-dev/richtext-editor";
      import { refAutoReset } from "@vueuse/core";
      import "./style.css";

      export const loadAsync = () => import("./lazy");
      export default {
        marker: [ref, createRouter, defineStore, axios, FormKit, getNode, stores.uiPlugins().isEnabled("fixture-dependency"), VButton, axiosInstance, Editor, refAutoReset]
      };
    `
  );
  fs.writeFileSync(
    path.join(providerRoot, "src/lazy.ts"),
    'import "./lazy.css";\nexport const value = "lazy";\n'
  );
  fs.writeFileSync(
    path.join(providerRoot, "src/style.css"),
    '.esm-provider { color: red; background-image: url("./asset.svg"); }\n'
  );
  fs.writeFileSync(
    path.join(providerRoot, "src/lazy.css"),
    ".esm-provider-lazy { color: blue; }\n"
  );
  fs.writeFileSync(
    path.join(providerRoot, "src/asset.svg"),
    `<svg xmlns="http://www.w3.org/2000/svg"><text>${"asset".repeat(5_000)}</text></svg>`
  );
  return providerRoot;
}

function assertEsmOutput(outputRoot: string, providerPublicPath: string) {
  const manifest = JSON.parse(
    fs.readFileSync(path.join(outputRoot, "ui-plugin.json"), "utf8")
  );
  expect(manifest).toEqual({
    format: "esm",
    entry: "./main.js",
    style: expect.stringMatching(/\.css$/),
  });
  expect(manifest).not.toHaveProperty("styles");
  const cssFiles = findFiles(outputRoot, (file) => file.endsWith(".css"));
  expect(cssFiles).toContain(manifest.style.replace(/^\.\//, ""));
  expect(cssFiles).toEqual(
    expect.arrayContaining([expect.stringMatching(/chunks|assets/)])
  );
  expect(cssFiles).toHaveLength(2);
  const asyncStyle = cssFiles.find(
    (file) => file !== manifest.style.replace(/^\.\//, "")
  );
  expect(asyncStyle).toBeDefined();
  const entryCss = fs.readFileSync(
    path.join(outputRoot, manifest.style.replace(/^\.\//, "")),
    "utf8"
  );
  expect(entryCss).toContain("url(");
  expect(entryCss).not.toContain(providerPublicPath);
  const entry = fs.readFileSync(path.join(outputRoot, "main.js"), "utf8");
  expect(entry).toMatch(/from\s*["']vue["']/);
  expect(entry).toMatch(/export\s*(?:default|\{)/);
  expect(entry).not.toContain(providerPublicPath);
  expect(entry).toContain("import.meta.url");
  expect(entry).toContain(asyncStyle);
  expect(entry.split("\n").filter(Boolean).length).toBeLessThanOrEqual(2);
  expect(findFiles(outputRoot, (file) => file.endsWith(".svg"))).toHaveLength(
    1
  );
  expect(
    fs
      .readdirSync(path.join(outputRoot, "chunks"))
      .some((file) => file.endsWith(".js"))
  ).toBe(true);
  for (const file of findFiles(
    outputRoot,
    (file) => file.endsWith(".js") || file.endsWith(".css")
  )) {
    expect(fs.readFileSync(path.join(outputRoot, file), "utf8")).not.toContain(
      providerPublicPath
    );
  }
}

function findFiles(root: string, predicate: (file: string) => boolean) {
  return fs.readdirSync(root, { recursive: true }).filter((file) => {
    const relative = String(file);
    return (
      predicate(relative) && fs.statSync(path.join(root, relative)).isFile()
    );
  });
}

function resolveViteConfig(config: unknown) {
  return (config as (env: ConfigEnv) => UserConfig)({
    command: "build",
    mode: "production",
    isSsrBuild: false,
    isPreview: false,
  });
}

function resolveRsbuildConfig(config: unknown) {
  return (config as (env: ConfigParams) => RsbuildConfig)({
    envMode: "production",
  } as ConfigParams);
}
