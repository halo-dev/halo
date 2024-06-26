import {
  VueQueryPlugin,
  type VueQueryPluginOptions,
} from "@tanstack/vue-query";
import type { App } from "vue";

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
