import { defineStore } from "pinia";
import type { PluginModule } from "@halo-dev/console-shared";

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
