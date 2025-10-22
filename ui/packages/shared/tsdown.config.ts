import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "tsdown";

export default defineConfig({
  entry: ["./src/index.ts"],
  format: ["esm", "iife"],
  external: ["vue", "vue-router"],
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
