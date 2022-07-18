import { defineStore } from "pinia";
import type { Plugin } from "@halo-dev/api-client";

interface PluginStoreState {
  plugins: Plugin[];
}

export const usePluginStore = defineStore("plugin", {
  state: (): PluginStoreState => ({
    plugins: [],
  }),
  actions: {
    registerPlugin(plugin: Plugin) {
      this.plugins.push(plugin);
    },
  },
});
