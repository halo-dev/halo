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

    assertEsmOutput(path.join(providerRoot, "build/dist"));
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

    assertEsmOutput(path.join(providerRoot, "dist"));
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
    ).rejects.toThrow("would bypass Halo inventory validation");
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
    'export const value = "lazy";\n'
  );
  fs.writeFileSync(
    path.join(providerRoot, "src/style.css"),
    ".esm-provider { color: red; }\n"
  );
  return providerRoot;
}

function assertEsmOutput(outputRoot: string) {
  const manifest = JSON.parse(
    fs.readFileSync(path.join(outputRoot, "ui-plugin.json"), "utf8")
  );
  expect(manifest).toEqual({
    format: "esm",
    entry: "./main.js",
    styles: expect.arrayContaining([expect.stringMatching(/\.css$/)]),
  });
  const entry = fs.readFileSync(path.join(outputRoot, "main.js"), "utf8");
  expect(entry).toMatch(/from\s*["']vue["']/);
  expect(entry).toMatch(/export\s*(?:default|\{)/);
  expect(
    fs
      .readdirSync(path.join(outputRoot, "chunks"))
      .some((file) => file.endsWith(".js"))
  ).toBe(true);
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
