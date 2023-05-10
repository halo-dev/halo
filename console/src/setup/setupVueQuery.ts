import type { App } from "vue";
import {
  VueQueryPlugin,
  type VueQueryPluginOptions,
} from "@tanstack/vue-query";

const networkMode = import.meta.env.PROD ? "online" : "always";

const options: VueQueryPluginOptions = {
  queryClientConfig: {
    defaultOptions: {
      queries: {
        refetchOnWindowFocus: false,
        networkMode,
      },
      mutations: {
        networkMode,
      },
    },
  },
};

export function setupVueQuery(app: App) {
  app.use(VueQueryPlugin, options);
}
