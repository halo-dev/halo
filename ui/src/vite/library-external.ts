import crypto from "node:crypto";
import fs from "node:fs";
import path from "node:path";
import { viteExternalsPlugin as ViteExternals } from "vite-plugin-externals";
import {
  viteStaticCopy as ViteStaticCopy,
  type Target,
} from "vite-plugin-static-copy";
import type { HtmlTagDescriptor, Plugin } from "vite-plus";

type LibraryTarget = Omit<Target, "rename" | "src"> & {
  src: string;
  rename: string;
};

interface SharedInventoryEntry {
  exports: string[];
  runtime: {
    bridge: string;
    global: string;
  };
}

interface SharedInventory {
  packages: Record<string, SharedInventoryEntry>;
}

const ESM_RUNTIME_PUBLIC_PATH = "/ui-assets/esm-runtime";

/**
 * It copies the external libraries to the `assets` folder, and injects the script tags into the HTML
 *
 * @param {string} command
 * @returns An array of plugins
 */
export const setupLibraryExternal = (
  command?: string,
  devServerPort = 3000
) => {
  // test command doesn't need to setup library external.
  if (command === "test") {
    return [];
  }

  const isProduction = command === "build";
  const sharedInventory = readSharedInventory();
  const runtimeBridges = createRuntimeBridges(sharedInventory);

  // TODO(Halo 3): Remove after legacy IIFE UI provider support ends.
  const libraryTargets: LibraryTarget[] = [
    {
      src: `./node_modules/vue/dist/vue.global${
        isProduction ? ".prod" : ""
      }.js`,
      dest: "vue",
      rename: `vue.[hash].js`,
    },
    {
      src: `./node_modules/vue-router/dist/vue-router.global${
        isProduction ? ".prod" : ""
      }.js`,
      dest: "vue-router",
      rename: `vue-router.[hash].js`,
    },
    {
      src: `./node_modules/pinia/dist/pinia.iife.prod.js`,
      dest: "pinia",
      rename: `pinia.[hash].js`,
    },
    {
      src: "./node_modules/axios/dist/axios.min.js",
      dest: "axios",
      rename: `axios.[hash].js`,
    },
    {
      src: `./node_modules/vue-demi/lib/index.iife.js`,
      dest: "vue-demi",
      rename: `vue-demi.[hash].js`,
    },
    {
      src: "./node_modules/@vueuse/shared/dist/index.iife.min.js",
      dest: "vueuse",
      rename: `vueuse.shared.[hash].js`,
    },
    {
      src: "./node_modules/@vueuse/core/dist/index.iife.min.js",
      dest: "vueuse",
      rename: `vueuse.core.[hash].js`,
    },
    {
      src: "./node_modules/@vueuse/components/dist/index.iife.min.js",
      dest: "vueuse",
      rename: `vueuse.components.[hash].js`,
    },
    {
      src: "./node_modules/@vueuse/router/dist/index.iife.min.js",
      dest: "vueuse",
      rename: `vueuse.router.[hash].js`,
    },
    {
      src: "./node_modules/@halo-dev/components/dist/index.iife.js",
      dest: "components",
      rename: `components.[hash].js`,
    },
    {
      src: "./node_modules/@halo-dev/api-client/dist/index.iife.js",
      dest: "api-client",
      rename: `api-client.[hash].js`,
    },
    {
      src: "./node_modules/@halo-dev/ui-shared/dist/index.iife.js",
      dest: "ui-shared",
      rename: `ui-shared.[hash].js`,
    },
    // TODO(Halo 3): Remove after legacy IIFE UI provider support ends.
    {
      src: "./node_modules/@halo-dev/console-shared/index.js",
      dest: "console-shared",
      rename: `console-shared.[hash].js`,
    },
    {
      src: "./node_modules/@halo-dev/richtext-editor/dist/index.iife.js",
      dest: "editor",
      rename: `editor.[hash].js`,
    },
  ];

  const staticTargets = libraryTargets.map((target) => {
    return {
      ...target,
      dest: `ui-assets/${target.dest}`,
      rename: target.rename.replace("[hash]", computeLibraryHash(target.src)),
    };
  });

  const injectTags = staticTargets
    .map((target) => {
      return {
        injectTo: "head",
        tag: "script",
        attrs: {
          src: `/${target.dest}/${target.rename}`,
          type: "text/javascript",
          "vite-ignore": true,
          ...(!isProduction ? { crossorigin: "" } : {}),
        },
      };
    })
    .filter(Boolean) as HtmlTagDescriptor[];

  return [
    ViteExternals({
      vue: "Vue",
      "vue-router": "VueRouter",
      pinia: "Pinia",
      axios: "axios",
      "@halo-dev/ui-shared": "HaloUiShared",
      "@halo-dev/components": "HaloComponents",
      "@vueuse/core": "VueUse",
      "@vueuse/components": "VueUse",
      "@vueuse/router": "VueUse",
      "vue-demi": "VueDemi",
      "@halo-dev/richtext-editor": "RichTextEditor",
      "@halo-dev/api-client": "HaloApiClient",
    }),
    ViteStaticCopy({
      targets: staticTargets.map((target) => ({
        ...target,
        rename: {
          name: target.rename,
          stripBase: true,
        },
      })),
    }),
    createEsmRuntimePlugin(runtimeBridges, isProduction, devServerPort),
    createInjectExternalTagsPlugin(injectTags),
  ];
};

export function createGlobalBridgeSource(entry: SharedInventoryEntry) {
  const runtime = "__haloSharedRuntime";
  const staticExports = entry.exports
    .filter((exportName) => exportName !== "default")
    .map(
      (exportName) =>
        `export const ${exportName} = ${runtime}[${JSON.stringify(exportName)}];`
    );
  const defaultExport = entry.exports.includes("default")
    ? [`export default ${runtime}.default ?? ${runtime};`]
    : [];
  return [
    "// TODO(Halo 3): Remove after legacy IIFE UI provider support ends.",
    `const ${runtime} = globalThis[${JSON.stringify(entry.runtime.global)}];`,
    `if (!${runtime}) throw new Error(${JSON.stringify(`Halo shared runtime global ${entry.runtime.global} is unavailable.`)});`,
    ...staticExports,
    ...defaultExport,
    "",
  ].join("\n");
}

function readSharedInventory() {
  const inventoryPath = path.resolve(
    import.meta.dirname,
    "../../packages/ui-plugin-bundler-kit/src/inventories/halo-2.26.0.json"
  );
  return JSON.parse(fs.readFileSync(inventoryPath, "utf8")) as SharedInventory;
}

function createRuntimeBridges(inventory: SharedInventory) {
  return Object.fromEntries(
    Object.entries(inventory.packages).map(([specifier, entry]) => {
      const source = createGlobalBridgeSource(entry);
      const hash = crypto
        .createHash("sha256")
        .update(source)
        .digest("hex")
        .slice(0, 8);
      return [
        specifier,
        {
          fileName: `${entry.runtime.bridge}.${hash}.mjs`,
          source,
        },
      ];
    })
  );
}

function createEsmRuntimePlugin(
  bridges: Record<string, { fileName: string; source: string }>,
  isProduction: boolean,
  devServerPort: number
): Plugin {
  const importMap = {
    imports: Object.fromEntries(
      Object.entries(bridges).map(([specifier, bridge]) => [
        specifier,
        `${isProduction ? "" : `http://localhost:${devServerPort}`}${ESM_RUNTIME_PUBLIC_PATH}/${bridge.fileName}`,
      ])
    ),
  };
  return {
    name: "halo:esm-shared-runtime",
    enforce: "pre",
    buildStart() {
      if (isProduction) {
        for (const bridge of Object.values(bridges)) {
          this.emitFile({
            type: "asset",
            fileName: `ui-assets/esm-runtime/${bridge.fileName}`,
            source: bridge.source,
          });
        }
      }
    },
    configureServer(server) {
      server.middlewares.use((request, response, next) => {
        const requestPath = request.url?.split("?", 1)[0];
        const bridge = Object.values(bridges).find(
          (candidate) =>
            `${ESM_RUNTIME_PUBLIC_PATH}/${candidate.fileName}` === requestPath
        );
        if (!bridge) {
          next();
          return;
        }
        response.statusCode = 200;
        response.setHeader("Content-Type", "text/javascript; charset=utf-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.end(bridge.source);
      });
    },
    transformIndexHtml: {
      order: "pre",
      handler() {
        return [
          {
            tag: "script",
            attrs: { type: "importmap" },
            children: JSON.stringify(importMap),
            injectTo: "head-prepend",
          },
        ];
      },
    },
  };
}

function createInjectExternalTagsPlugin(tags: HtmlTagDescriptor[]): Plugin {
  return {
    name: "halo:inject-external-library-tags",
    enforce: "pre",
    transformIndexHtml: {
      order: "pre",
      handler() {
        return tags;
      },
    },
  };
}

function computeLibraryHash(file: string) {
  const content = fs.readFileSync(path.resolve(process.cwd(), file), "utf8");
  return crypto.createHash("md5").update(content).digest("hex").substring(0, 8);
}
