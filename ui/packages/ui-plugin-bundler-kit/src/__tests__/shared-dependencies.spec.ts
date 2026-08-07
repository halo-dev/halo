import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { HALO_HOST_RUNTIME_SNAPSHOTS } from "../runtime-snapshot";
import {
  SharedDependencyValidator,
  parseImports,
} from "../shared-dependencies";

const uiRoot = path.resolve(
  path.dirname(fileURLToPath(import.meta.url)),
  "../../../.."
);
const tempDirs: string[] = [];

afterEach(() => {
  while (tempDirs.length > 0) {
    const tempDir = tempDirs.pop();
    if (tempDir) {
      fs.rmSync(tempDir, { recursive: true, force: true });
    }
  }
});

describe("shared dependency validation", () => {
  it("extracts static, side-effect, re-export, and dynamic import specifiers", () => {
    expect(
      parseImports(`
        import axios, { AxiosError as Error } from "axios";
        import * as Vue from "vue";
        import "pinia";
        export { ref } from "vue";
        const router = import("vue-router");
      `)
    ).toEqual(["axios", "vue", "pinia", "vue", "vue-router"]);
  });

  it("recognizes minified imports without relying on whitespace", () => {
    expect(
      parseImports('import{ref as r}from"vue";export{defineStore}from"pinia"')
    ).toEqual(["vue", "pinia"]);
  });

  it("ignores import-like text in comments and strings", () => {
    expect(
      parseImports(`
        // import { notAHostExport } from "vue";
        /* export * from "vue-router/internal"; */
        const example = 'import "pinia/private"';
        const dynamic = 'import("axios/private")';
        import { ref } from "vue";
      `)
    ).toEqual(["vue"]);
  });

  it("resolves an exports-only transitive dependency relative to its importer", async () => {
    const providerRoot = fs.mkdtempSync(
      path.join(os.tmpdir(), "halo-transitive-shared-")
    );
    tempDirs.push(providerRoot);
    const virtualRoot = path.join(
      providerRoot,
      "node_modules/.pnpm/vueuse-router/node_modules"
    );
    const importer = path.join(virtualRoot, "@vueuse/router/index.mjs");
    const dependencyRoot = path.join(virtualRoot, "vue-router");
    const installedDependencyRoot = path.join(
      providerRoot,
      "node_modules/.pnpm/vue-router@4.2.5/node_modules/vue-router"
    );
    fs.mkdirSync(path.dirname(importer), { recursive: true });
    fs.mkdirSync(installedDependencyRoot, { recursive: true });
    fs.symlinkSync(installedDependencyRoot, dependencyRoot, "dir");
    fs.writeFileSync(importer, 'import "vue-router";\n');
    fs.writeFileSync(
      path.join(installedDependencyRoot, "package.json"),
      JSON.stringify({
        name: "vue-router",
        version: "4.2.5",
        exports: {
          ".": {
            browser: "./dist/browser.mjs",
            import: "./dist/index.mjs",
          },
        },
      })
    );
    fs.mkdirSync(path.join(installedDependencyRoot, "dist"));
    fs.writeFileSync(
      path.join(installedDependencyRoot, "dist/browser.mjs"),
      "export {};\n"
    );
    fs.writeFileSync(
      path.join(installedDependencyRoot, "dist/index.mjs"),
      "export {};\n"
    );
    const snapshot = structuredClone(HALO_HOST_RUNTIME_SNAPSHOTS[0]);
    snapshot.packages["vue-router"].version = "4.2.5";
    const validator = new SharedDependencyValidator({
      snapshot,
      providerRoot,
    });

    await validator.validateSource(
      'import "vue-router";',
      `${importer}?compiled`
    );

    expect(validator.getBuildReport().summary).toContain("vue-router  4.2.5");
  });

  it("reports installed shared versions without validating imported names", async () => {
    const validator = new SharedDependencyValidator({
      snapshot: HALO_HOST_RUNTIME_SNAPSHOTS[0],
      providerRoot: uiRoot,
    });

    await expect(
      validator.validateSource(
        'import { notAHostExport } from "vue"; import axios from "axios";',
        "src/index.ts"
      )
    ).resolves.toBeUndefined();
    expect(validator.getValidatedRoots()).toEqual(["vue", "axios"]);
    expect(validator.getBuildReport().warning).toBeUndefined();
  });

  it("rejects shared package subpaths", async () => {
    const validator = new SharedDependencyValidator({
      snapshot: HALO_HOST_RUNTIME_SNAPSHOTS[0],
      providerRoot: uiRoot,
    });

    await expect(
      validator.validateSource(
        'import x from "vue/dist/vue.esm.js";',
        "index.ts"
      )
    ).rejects.toThrow("Unsupported shared dependency subpath");
  });

  it("reports only a newer-provider version note", async () => {
    const snapshot = structuredClone(HALO_HOST_RUNTIME_SNAPSHOTS[0]);
    snapshot.packages.vue.version = "3.2.0";
    const validator = new SharedDependencyValidator({
      snapshot,
      providerRoot: uiRoot,
    });

    await validator.validateSource(
      `
        import * as Vue from "vue";
        import { Editor } from "@halo-dev/richtext-editor";
        import { Extension } from "@tiptap/core";
      `,
      path.join(uiRoot, "src/index.ts")
    );

    const report = validator.getBuildReport();
    expect(report.warning).toContain("provider is newer; best-effort");
    expect(report.warning).not.toContain("Namespace or dynamic imports");
    expect(report.warning).not.toContain("editor identity");
  });

  it("does not warn for an older provider version in the same major", async () => {
    const snapshot = structuredClone(HALO_HOST_RUNTIME_SNAPSHOTS[0]);
    snapshot.packages.vue.version = "3.99.0";
    const validator = new SharedDependencyValidator({
      snapshot,
      providerRoot: uiRoot,
    });

    await validator.validateSource('import { ref } from "vue";', "index.ts");

    expect(validator.getBuildReport().warning).toBeUndefined();
  });

  it("warns but does not reject a different provider major", async () => {
    const snapshot = structuredClone(HALO_HOST_RUNTIME_SNAPSHOTS[0]);
    snapshot.packages.pinia.version = "2.3.1";
    const validator = new SharedDependencyValidator({
      snapshot,
      providerRoot: uiRoot,
    });

    await expect(
      validator.validateSource(
        'import { defineStore } from "pinia";',
        "index.ts"
      )
    ).resolves.toBeUndefined();
    const warning = validator.getBuildReport().warning;
    expect(warning).toContain("different major");
    expect(warning).not.toContain("provider is newer");
  });

  it("does not turn unavailable package metadata into a policy failure", async () => {
    const providerRoot = fs.mkdtempSync(
      path.join(os.tmpdir(), "halo-missing-shared-")
    );
    tempDirs.push(providerRoot);
    const validator = new SharedDependencyValidator({
      snapshot: HALO_HOST_RUNTIME_SNAPSHOTS[0],
      providerRoot,
    });

    await expect(
      validator.validateSource('import { ref } from "vue";', "src/index.ts")
    ).resolves.toBeUndefined();
    expect(validator.getValidatedRoots()).toEqual([]);
  });
});
