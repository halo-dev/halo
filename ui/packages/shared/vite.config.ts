import { fileURLToPath } from "node:url";
import { defineConfig } from "vite-plus";

export default defineConfig({
  pack: {
    entry: ["./src/index.ts"],
    format: ["esm", "iife"],
    deps: {
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
      tsgo: true,
    },
  },
});
