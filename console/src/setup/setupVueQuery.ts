import type { App } from "vue";
import {
  VueQueryPlugin,
  type VueQueryPluginOptions,
} from "@tanstack/vue-query";

const options: VueQueryPluginOptions = {
  queryClientConfig: {
    defaultOptions: {
      queries: {
        refetchOnWindowFocus: false,
        networkMode: "always",
      },
      mutations: {
        networkMode: "always",
      },
    },
  },
};

export function setupVueQuery(app: App) {
  app.use(VueQueryPlugin, options);
}
