import { defineConfig } from "tsdown";

export default defineConfig({
  entry: ["./entry/index.ts"],
  format: ["esm", "iife"],
  external: ["axios"],
  // TODO: It seems not working
  noExternal: ["qs"],
  outputOptions: {
    globals: {
      axios: "axios",
    },
  },
  platform: "neutral",
  globalName: "HaloApiClient",
  tsconfig: "./tsconfig.json",
  minify: true,
  exports: true,
});
