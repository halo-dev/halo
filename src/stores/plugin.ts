import { defineStore } from "pinia";

export const usePluginStore = defineStore("plugin", {
  state: () => ({
    plugins: [],
  }),
  actions: {
    registerPlugin(plugin) {
      // @ts-ignore
      this.plugins.push(plugin);
    },
  },
});
