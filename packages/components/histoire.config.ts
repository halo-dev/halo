import { defineConfig } from "histoire";
import type { UserConfig } from "vite";
import { HstVue } from "@histoire/plugin-vue";

export default defineConfig({
  setupFile: "./src/histoire.setup.ts",
  plugins: [HstVue()],
  vite: {
    plugins: [
      {
        name: "disable-lib-plugin",
        config(config: UserConfig) {
          if (!config || !config.build || !config.build.rollupOptions) {
            return;
          }
          config.build.lib = false;
          config.build.rollupOptions.external = [];
        },
      },
    ],
  },
});
