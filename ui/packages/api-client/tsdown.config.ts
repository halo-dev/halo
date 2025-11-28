import { defineConfig, type UserConfig } from "tsdown";

const sharedConfig: UserConfig = {
  entry: ["./entry/index.ts"],
  external: ["axios"],
  noExternal: ["qs"],
  outputOptions: {
    globals: {
      axios: "axios",
    },
  },
  platform: "browser",
  globalName: "HaloApiClient",
  tsconfig: "./tsconfig.json",
  exports: true,
  dts: {
    tsgo: true,
  },
};

export default defineConfig([
  {
    ...sharedConfig,
    format: "esm",
  },
  {
    ...sharedConfig,
    format: "iife",
    minify: true,
  },
]);
