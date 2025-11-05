import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "tsdown";

export default defineConfig({
  entry: ["./src/index.ts"],
  format: ["esm", "iife"],
  external: ["vue", "vue-router", "pinia", "@halo-dev/api-client"],
  noExternal: ["mitt"],
  outputOptions: {
    globals: {
      vue: "Vue",
      "vue-router": "VueRouter",
      pinia: "Pinia",
      "@halo-dev/api-client": "HaloApiClient",
    },
  },
  platform: "browser",
  globalName: "HaloConsoleShared",
  tsconfig: "./tsconfig.app.json",
  alias: {
    "@": fileURLToPath(new URL("./src", import.meta.url)),
  },
  minify: true,
  exports: true,
  dts: {
    tsgo: true,
  },
});
