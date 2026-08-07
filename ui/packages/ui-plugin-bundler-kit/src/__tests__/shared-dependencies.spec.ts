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
  it("extracts named, default, namespace, side-effect, and dynamic imports", () => {
    expect(
      parseImports(`
        import axios, { AxiosError as Error } from "axios";
        import * as Vue from "vue";
        import "pinia";
        export { ref } from "vue";
        const router = import("vue-router");
      `)
    ).toEqual([
      { specifier: "axios", names: ["default", "AxiosError"] },
      { specifier: "vue", names: "namespace" },
      { specifier: "pinia", names: [] },
      { specifier: "vue", names: ["ref"] },
      { specifier: "vue-router", names: "namespace" },
    ]);
  });

  it("does not parse exported function bodies as re-export clauses", () => {
    expect(
      parseImports(`
        import * as Vue from "vue";
        export function set(target, key, value) {
          target[key] = value;
        }
        export * from "vue";
      `)
    ).toEqual([
      { specifier: "vue", names: "namespace" },
      { specifier: "vue", names: "namespace" },
    ]);
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
    ).toEqual([{ specifier: "vue", names: ["ref"] }]);
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

    await expect(
      validator.validateSource('import "vue-router";', `${importer}?compiled`)
    ).resolves.toBeUndefined();
  });

  it("validates actual package versions and static exports", async () => {
    const validator = new SharedDependencyValidator({
      snapshot: HALO_HOST_RUNTIME_SNAPSHOTS[0],
      providerRoot: uiRoot,
    });

    await expect(
      validator.validateSource(
        'import { ref } from "vue"; import axios from "axios";',
        "src/index.ts"
      )
    ).resolves.toBeUndefined();
    expect(validator.getValidatedRoots()).toEqual(["vue", "axios"]);
    expect(validator.getBuildReport().warning).toBeUndefined();
  });

  it("fails closed for unsupported exports and deep roots", async () => {
    const validator = new SharedDependencyValidator({
      snapshot: HALO_HOST_RUNTIME_SNAPSHOTS[0],
      providerRoot: uiRoot,
    });
    await expect(
      validator.validateSource(
        'import { notAHostExport } from "vue";',
        "index.ts"
      )
    ).rejects.toThrow("unsupported vue export");
    await expect(
      validator.validateSource(
        'import x from "vue/dist/vue.esm.js";',
        "index.ts"
      )
    ).rejects.toThrow("Unsupported shared dependency subpath");
  });

  it("groups version, namespace, and editor compatibility notes", async () => {
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
    await validator.validateSource(
      'import * as Vue from "vue";',
      path.join(
        uiRoot,
        "node_modules/.pnpm/vue-demi@0.14.10/node_modules/vue-demi/lib/index.mjs?compiled"
      )
    );
    await validator.validateSource(
      'import * as Vue from "vue";',
      path.join(
        uiRoot,
        "node_modules/.pnpm/vue-demi@0.14.10/node_modules/vue-demi/lib/index.mjs?compiled"
      )
    );

    const report = validator.getBuildReport();
    expect(report.summary).toContain(
      "package                    provider  Halo host"
    );
    expect(report.summary).toContain("vue");
    expect(report.warning).toContain("Version differences");
    expect(report.warning).toContain("provider is newer; best-effort");
    expect(report.warning).toContain(
      "Namespace or dynamic imports not fully checked"
    );
    expect(report.warning).toContain("- src/index.ts");
    expect(report.warning).toContain("- vue-demi/lib/index.mjs");
    expect(report.warning?.match(/vue-demi\/lib\/index\.mjs/g)).toHaveLength(1);
    expect(report.warning).toContain("editor identity is best-effort");
    expect(report.warning).not.toContain(uiRoot);
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
    expect(validator.getBuildReport().warning).toContain("different major");
  });
});
