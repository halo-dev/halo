import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { afterEach, describe, expect, it, vi } from "vite-plus/test";
import { HALO_SHARED_INVENTORIES } from "../inventory";
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
      { specifier: "vue", names: ["ref"] },
      { specifier: "pinia", names: [] },
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

  it("resolves a transitive shared dependency relative to its importer", () => {
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
    fs.mkdirSync(path.dirname(importer), { recursive: true });
    fs.mkdirSync(dependencyRoot, { recursive: true });
    fs.writeFileSync(importer, 'import "vue-router";\n');
    fs.writeFileSync(
      path.join(dependencyRoot, "package.json"),
      JSON.stringify({
        name: "vue-router",
        version: "4.2.5",
        module: "index.mjs",
      })
    );
    fs.writeFileSync(path.join(dependencyRoot, "index.mjs"), "export {};\n");
    const inventory = structuredClone(HALO_SHARED_INVENTORIES[0]);
    inventory.packages["vue-router"].version = "4.2.5";
    const validator = new SharedDependencyValidator({
      inventory,
      providerRoot,
      warn: vi.fn(),
    });

    expect(() =>
      validator.validateSource('import "vue-router";', `${importer}?compiled`)
    ).not.toThrow();
  });

  it("validates actual package versions and static exports", () => {
    const validator = new SharedDependencyValidator({
      inventory: HALO_SHARED_INVENTORIES[0],
      providerRoot: uiRoot,
      warn: vi.fn(),
    });

    expect(() =>
      validator.validateSource(
        'import { ref } from "vue"; import axios from "axios";',
        "src/index.ts"
      )
    ).not.toThrow();
    expect(validator.getValidatedRoots()).toEqual(["vue", "axios"]);
  });

  it("fails closed for unsupported exports, deep roots, and ranges", () => {
    const inventory = structuredClone(HALO_SHARED_INVENTORIES[0]);
    inventory.packages.vue.range = ">=3.2 <3.4";
    const rangeValidator = new SharedDependencyValidator({
      inventory,
      providerRoot: uiRoot,
      warn: vi.fn(),
    });
    expect(() =>
      rangeValidator.validateSource('import { ref } from "vue";', "index.ts")
    ).toThrow("outside Halo 2.26.0's accepted range");

    const validator = new SharedDependencyValidator({
      inventory: HALO_SHARED_INVENTORIES[0],
      providerRoot: uiRoot,
      warn: vi.fn(),
    });
    expect(() =>
      validator.validateSource(
        'import { notAHostExport } from "vue";',
        "index.ts"
      )
    ).toThrow("unsupported vue export");
    expect(() =>
      validator.validateSource(
        'import x from "vue/dist/vue.esm.js";',
        "index.ts"
      )
    ).toThrow("Unsupported shared dependency subpath");
  });

  it("warns for namespace, forward-version, and editor identity cases", () => {
    const warn = vi.fn();
    const inventory = structuredClone(HALO_SHARED_INVENTORIES[0]);
    inventory.packages.vue.version = "3.2.0";
    const validator = new SharedDependencyValidator({
      inventory,
      providerRoot: uiRoot,
      warn,
    });

    validator.validateSource(
      `
        import * as Vue from "vue";
        import { Editor } from "@halo-dev/richtext-editor";
        import { Extension } from "@tiptap/core";
      `,
      "index.ts"
    );

    expect(warn).toHaveBeenCalledWith(expect.stringContaining("namespace"));
    expect(warn).toHaveBeenCalledWith(
      expect.stringContaining("newer than Halo 2.26.0's host")
    );
    expect(warn).toHaveBeenCalledWith(
      expect.stringContaining("editor identity is best-effort")
    );
  });
});
