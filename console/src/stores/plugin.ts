import { defineStore } from "pinia";
import type { PluginModule as PluginModuleRaw } from "@halo-dev/console-shared";
import type { Plugin } from "@halo-dev/api-client";

export interface PluginModule extends PluginModuleRaw {
  extension: Plugin;
}

interface PluginStoreState {
  pluginModules: PluginModule[];
}

export const usePluginModuleStore = defineStore("plugin", {
  state: (): PluginStoreState => ({
    pluginModules: [],
  }),
  actions: {
    registerPluginModule(pluginModule: PluginModule) {
      this.pluginModules.push(pluginModule);
    },
  },
});
