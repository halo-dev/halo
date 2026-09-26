import { fileURLToPath } from "node:url";
import { defineConfig } from "vite-plus";

export default defineConfig({
  test: {
    // Vitest v4 compatibility: preserve mock call history.
    // Remove after tests no longer rely on calls from setup or earlier tests.
    // https://viteplus.dev/guide/vitest-v5#remove-unneeded-compatibility-settings
    // https://vitest.dev/guide/migration/#clearmocks-is-enabled-by-default
    clearMocks: false,
  },
  pack: {
    entry: ["./src/index.ts"],
    format: ["esm", "iife"],
    deps: {
      resolveDepSubpath: true,
      neverBundle: ["vue", "vue-router", "pinia", "@halo-dev/api-client"],
      alwaysBundle: ["mitt"],
      onlyBundle: false,
    },
    outputOptions: {
      globals: {
        vue: "Vue",
        "vue-router": "VueRouter",
        pinia: "Pinia",
        "@halo-dev/api-client": "HaloApiClient",
      },
    },
    platform: "browser",
    globalName: "HaloUiShared",
    tsconfig: "./tsconfig.app.json",
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
    minify: true,
    exports: true,
    dts: {
      generator: "tsgo",
    },
  },
});
