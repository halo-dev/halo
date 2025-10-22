import {
  VueQueryPlugin,
  type VueQueryPluginOptions,
} from "@tanstack/vue-query";
import type { App } from "vue";

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
