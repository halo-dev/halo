import { defineConfig } from "vite-plus";
import { UserConfig } from "vite-plus/pack";

const sharedConfig: UserConfig = {
  entry: ["./entry/index.ts"],
  deps: {
    neverBundle: ["axios"],
    alwaysBundle: ["qs"],
    onlyBundle: false,
  },
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

export default defineConfig({
  pack: [
    {
      ...sharedConfig,
      format: "esm",
    },
    {
      ...sharedConfig,
      format: "iife",
      minify: true,
    },
  ],
});
