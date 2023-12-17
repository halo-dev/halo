import { defineStore } from "pinia";
import type { PluginModule } from "@halo-dev/console-shared";
import { computed, ref } from "vue";

export const usePluginModuleStore = defineStore("plugin", () => {
  const pluginModuleMap = ref<Record<string, PluginModule>>({});

  function registerPluginModule(name: string, pluginModule: PluginModule) {
    pluginModuleMap.value[name] = pluginModule;
  }

  const pluginModules = computed(() => {
    return Object.values(pluginModuleMap.value);
  });

  return { pluginModuleMap, pluginModules, registerPluginModule };
});
